package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.enricher.service.validator.Validator.isEqualsPerson;
import static ua.com.solidity.enricher.util.Base.CONTRAGENT;
import static ua.com.solidity.enricher.util.Chooser.chooseNotBlank;
import static ua.com.solidity.enricher.util.Chooser.chooseNotNull;
import static ua.com.solidity.enricher.util.LogUtil.logFinish;
import static ua.com.solidity.enricher.util.LogUtil.logStart;
import static ua.com.solidity.enricher.util.Regex.INN_REGEX;
import static ua.com.solidity.enricher.util.Regex.PASS_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.StringFormatUtil.importedRecords;
import static ua.com.solidity.enricher.util.StringStorage.DOMESTIC_PASSPORT;
import static ua.com.solidity.enricher.util.StringStorage.ENRICHER;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ua.com.solidity.common.StatusChanger;
import ua.com.solidity.common.UtilString;
import ua.com.solidity.db.entities.Contragent;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.YAddress;
import ua.com.solidity.db.entities.YAltPerson;
import ua.com.solidity.db.entities.YEmail;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.db.entities.YPassport;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.entities.YPhone;
import ua.com.solidity.db.entities.YTag;
import ua.com.solidity.db.repositories.ContragentRepository;
import ua.com.solidity.db.repositories.ImportSourceRepository;
import ua.com.solidity.db.repositories.TagTypeRepository;
import ua.com.solidity.db.repositories.YINNRepository;
import ua.com.solidity.db.repositories.YPassportRepository;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.enricher.service.MonitoringNotificationService;

@CustomLog
@Service
@RequiredArgsConstructor
public class ContragentEnricher implements Enricher {

	private final Extender extender;
	private final YPersonRepository ypr;
	private final ContragentRepository cr;
	private final YINNRepository yir;
	private final MonitoringNotificationService emnService;
	private final ImportSourceRepository isr;
	private final YPassportRepository yPassportRepository;
    private final TagTypeRepository tagTypeRepository;

	@Value("${otp.enricher.page-size}")
	private Integer pageSize;
	@Value("${statuslogger.rabbitmq.name}")
	private String queueName;
	private boolean findCopy;

	@Override
	public void enrich(UUID portion) {
		logStart(CONTRAGENT);

		StatusChanger statusChanger = new StatusChanger(portion, CONTRAGENT, ENRICHER);
		long[] counter = new long[1];

		Pageable pageRequest = PageRequest.of(0, pageSize);
		Page<Contragent> onePage = cr.findAllByPortionId(portion, pageRequest);
		long count = cr.countAllByPortionId(portion);
		statusChanger.newStage(null, "enriching", count, null);
		ImportSource source = isr.findImportSourceByName("dwh");

		while (!onePage.isEmpty()) {
			pageRequest = pageRequest.next();
			Set<YPerson> personSet = new HashSet<>();
			Set<YINN> yinnList = new HashSet<>();

			onePage.forEach(r -> {
				Optional<YINN> yinnSavedOptional;
				Optional<YINN> yinnCachedOptional;
				String identifyCode = r.getIdentifyCode();

				if (StringUtils.isNotBlank(identifyCode) && identifyCode.matches(INN_REGEX)) {// We skip person without inn
					long inn = Long.parseLong(identifyCode);
					yinnSavedOptional = yir.findByInn(inn);
					yinnCachedOptional = yinnList.stream().filter(i -> i.getInn() == inn).findAny();
					boolean cached = false;
					boolean saved = false;

					YPerson person = null;
					if (yinnSavedOptional.isPresent()) {
						UUID personId = yinnSavedOptional.get().getPerson().getId();
						Optional<YPerson> ypersonOptional = ypr.findWithInnsAndPassportsAndTagsAndPhonesAndAddressesAndAltPeopleAndEmailsAndImportSourcesById(personId);
						person = ypersonOptional.orElseGet(() -> null);
						saved = true;
					}
					if (yinnCachedOptional.isPresent()) {
						if (saved) {
							UUID personId = yinnSavedOptional.get().getPerson().getId();
							Optional<YPerson> ypersonOptional = ypr.findWithInnsAndPassportsAndTagsAndPhonesAndAddressesAndAltPeopleAndEmailsAndImportSourcesById(personId);
							person = ypersonOptional.orElseGet(() -> null);
						} else {
							person = yinnCachedOptional.get().getPerson();
						}
						cached = true;
					}
					if (person == null) person = new YPerson(UUID.randomUUID());
					extender.addSource(person.getImportSources(), source);

					List<String> splitPersonNames = Arrays.asList(r.getName().split("[ ]+"));
					String splitPatronymic = splitPersonNames.size() == 3 ? splitPersonNames.get(2) : null;

					if (!StringUtils.isBlank(r.getClientPatronymicName()))
						person.setPatName(UtilString.toUpperCase(chooseNotBlank(person.getPatName(), r.getClientPatronymicName())));
					else
						person.setPatName(UtilString.toUpperCase(chooseNotBlank(person.getPatName(), splitPatronymic)));
					person.setLastName(UtilString.toUpperCase(chooseNotBlank(person.getLastName(), r.getClientLastName())));

					if (!StringUtils.isBlank(r.getClientName()))
						person.setFirstName(UtilString.toUpperCase(r.getClientName()));
					else if (splitPersonNames.size() == 3) {
						String firstName = splitPersonNames.get(1).equalsIgnoreCase(r.getClientLastName()) ? splitPersonNames.get(0) : splitPersonNames.get(1);
						person.setFirstName(UtilString.toUpperCase(firstName));
					}

					person.setBirthdate(chooseNotNull(person.getBirthdate(), r.getClientBirthday()));

					YINN yinn = null;
					if (yinnSavedOptional.isPresent()) yinn = yinnSavedOptional.get();
					if (yinnCachedOptional.isPresent()) yinn = yinnCachedOptional.get();
					if (yinn == null) yinn = new YINN();
					extender.addSource(yinn.getImportSources(), source);
					if (!cached && !saved) {
						yinn.setInn(Long.parseLong(identifyCode));
						yinn.setPerson(person);
						yinn.getPerson().getInns().add(yinn);
					}

					String passportNo = r.getPassportNo();
					String passportSerial = UtilString.toUpperCase(r.getPassportSerial());
					Integer number;
					if (StringUtils.isNotBlank(passportNo) && passportNo.matches(PASS_NUMBER_REGEX)) {
						number = Integer.parseInt(passportNo);
						List<YPerson> personListWithSamePassport = personSet.stream()
								.filter(yperson -> {
									return yperson.getPassports()
											.stream()
											.anyMatch(passport -> {
												return Objects.equals(passport.getNumber(), number) &&
														Objects.equals(passport.getSeries(), passportSerial) &&
														Objects.equals(passport.getType(), DOMESTIC_PASSPORT);
											});
								})
								.collect(Collectors.toList());

						Optional<YPassport> passportOptional = yPassportRepository.findByTypeAndNumberAndSeries(DOMESTIC_PASSPORT, number, passportSerial);
						passportOptional.ifPresent(passport -> personListWithSamePassport.addAll(ypr.findByPassportsContains(passport)));

						if (passportOptional.isEmpty() && !personListWithSamePassport.isEmpty())
							passportOptional = personListWithSamePassport.get(0).getPassports().parallelStream().filter(pas -> Objects.equals(pas.getType(), DOMESTIC_PASSPORT)
									&& Objects.equals(pas.getSeries(), passportSerial)
									&& Objects.equals(pas.getNumber(), number)).findAny();

						YPassport passport = passportOptional.orElseGet(YPassport::new);
						extender.addSource(passport.getImportSources(), source);
						passport.setSeries(UtilString.toUpperCase(passportSerial));
						passport.setNumber(number);
						passport.setAuthority(UtilString.toUpperCase(chooseNotBlank(passport.getAuthority(), r.getPassportIssuePlace())));
						passport.setIssued(chooseNotNull(passport.getIssued(), r.getPassportIssueDate()));
						passport.setEndDate(chooseNotNull(passport.getEndDate(), r.getPassportEndDate()));
						passport.setRecordNumber(null);
						passport.setValidity(true);
						passport.setType(DOMESTIC_PASSPORT);

						Optional<YPerson> optionalYPerson = Optional.empty();
						if (!personListWithSamePassport.isEmpty()) {
							YPerson finalPerson = person;
							optionalYPerson = personListWithSamePassport.parallelStream().filter(p -> isEqualsPerson(p, finalPerson)).findAny();
						}

						if (optionalYPerson.isPresent()) {
							YPerson findPerson = optionalYPerson.get();
							findPerson.setLastName(chooseNotNull(findPerson.getLastName(), person.getLastName()));
							findPerson.setFirstName(chooseNotNull(findPerson.getFirstName(), person.getFirstName()));
							findPerson.setPatName(chooseNotNull(findPerson.getPatName(), person.getPatName()));
							findPerson.setBirthdate(chooseNotNull(findPerson.getBirthdate(), person.getBirthdate()));

							person = findPerson;
							findCopy = true;
						} else if (passportOptional.isPresent() && passport.getId() == null)
							yPassportRepository.save(passport);
						person.getPassports().add(passport);
						extender.addSource(passport.getImportSources(), source);

						if (person.getId() != null) {
							person = ypr.findWithInnsAndPassportsAndTagsAndPhonesAndAddressesAndAltPeopleAndEmailsAndImportSourcesById(person.getId()).orElse(person);
						}

					}

					YTag tag = new YTag();
                    tag.setTagType(tagTypeRepository.findByCode("RC")
                            .orElseThrow(() -> new RuntimeException("Not found tag with code: " + "RC")));
					tag.setSource(CONTRAGENT);
					tag.setPerson(person);
					extender.addSource(tag.getImportSources(), source);
					person.getTags().add(tag);

					YPerson finalPerson = person;
					Stream.of(r.getPhones(), r.getMobilePhone(), r.getPhoneHome()).forEach(phone -> {
						if (phone != null) {
							String phoneCleaned = phone.replaceAll("[^0-9]+", "");
							if (StringUtils.isNotBlank(phoneCleaned)) {
								Optional<YPhone> yphoneOptional = finalPerson.getPhones()
										.parallelStream()
										.filter(yphone -> yphone.getPhone().equals(phoneCleaned))
										.findFirst();

								YPhone yPhone = yphoneOptional.orElseGet(YPhone::new);
								extender.addSource(yPhone.getImportSources(), source);
								yPhone.setPhone(phoneCleaned);

								if (yphoneOptional.isEmpty()) {
									yPhone.setPerson(finalPerson);
									finalPerson.getPhones().add(yPhone);
								}
							}
						}
					});

					YPerson finalPerson1 = person;
					Stream.of(r.getAddress(), r.getBirthplace()).forEach(address -> {
						if (StringUtils.isNotBlank(address)) {
							Optional<YAddress> yAddressOptional = finalPerson1.getAddresses()
									.parallelStream()
									.filter(yAddress -> yAddress.getAddress().equalsIgnoreCase(address))
									.findAny();

							YAddress yAddress = yAddressOptional.orElseGet(YAddress::new);
							extender.addSource(yAddress.getImportSources(), source);
							yAddress.setAddress(UtilString.toUpperCase(address.toUpperCase()));

							if (yAddressOptional.isEmpty()) {
								yAddress.setPerson(finalPerson1);
								finalPerson1.getAddresses().add(yAddress);
							}
						}
					});

					if (!StringUtils.isBlank(r.getAlternateName())) {
						List<String> splitAltPersonNames = Arrays.asList(r.getAlternateName().split("[ ]+"));

						String[] names = new String[3];
						for (int i = 0; i < splitAltPersonNames.size(); i++) {
							if (i == 2) {
								StringBuilder sb = new StringBuilder(splitAltPersonNames.get(2));
								for (int j = 3; j < splitAltPersonNames.size(); j++) {
									sb.append(' ').append(splitAltPersonNames.get(j));
								}
								names[2] = sb.toString();
								break;
							}
							names[i] = splitAltPersonNames.get(i);
						}
						Optional<YAltPerson> altPersonOptional = person.getAltPeople().parallelStream()
								.filter(yAltPerson -> StringUtils.equalsIgnoreCase(yAltPerson.getFirstName(), names[0]) &&
										StringUtils.equalsIgnoreCase(yAltPerson.getLastName(), names[1]) &&
										StringUtils.equalsIgnoreCase(yAltPerson.getPatName(), names[2]))
								.findFirst();

						YAltPerson newAltPerson = altPersonOptional.orElseGet(YAltPerson::new);
						extender.addSource(newAltPerson.getImportSources(), source);
						newAltPerson.setFirstName(UtilString.toUpperCase(names[0]));
						newAltPerson.setLastName(UtilString.toUpperCase(names[1]));
						newAltPerson.setPatName(UtilString.toUpperCase(names[2]));
						newAltPerson.setLanguage("EN");

						if (altPersonOptional.isEmpty()) {
							newAltPerson.setPerson(person);
							person.getAltPeople().add(newAltPerson);
						}
					}

					if (!StringUtils.isBlank(r.getEmail())) {
						Optional<YEmail> emailOptional = person.getEmails()
								.parallelStream()
								.filter(e -> StringUtils.equalsIgnoreCase(r.getEmail(), e.getEmail()))
								.findAny();
						YEmail email = emailOptional.orElseGet(YEmail::new);
						extender.addSource(email.getImportSources(), source);
						email.setEmail(UtilString.toLowerCase(r.getEmail()));

						if (emailOptional.isEmpty()) {
							email.setPerson(person);
							person.getEmails().add(email);
						}
					}
					if (!cached) {
						personSet.add(person);
						yinnList.add(yinn);
						counter[0]++;
					}
					statusChanger.addProcessedVolume(1);
				}
			});

			ypr.saveAll(personSet);
			emnService.enrichMonitoringNotification(personSet);


			onePage = cr.findAllByPortionId(portion, pageRequest);
		}

		logFinish(CONTRAGENT, counter[0]);
		statusChanger.complete(importedRecords(counter[0]));
	}
}
