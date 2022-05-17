package ua.com.solidity.enricher.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Service;
import ua.com.solidity.common.UtilString;
import ua.com.solidity.common.Utils;
import ua.com.solidity.db.entities.BaseDrfo;
import ua.com.solidity.db.entities.BaseElections;
import ua.com.solidity.db.entities.BaseFodb;
import ua.com.solidity.db.entities.BasePassports;
import ua.com.solidity.db.entities.StatusLogger;
import ua.com.solidity.db.entities.YAddress;
import ua.com.solidity.db.entities.YAltPerson;
import ua.com.solidity.db.entities.YEmail;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.db.entities.YPassport;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.entities.YPhone;
import ua.com.solidity.db.entities.YTag;
import ua.com.solidity.db.repositories.YINNRepository;
import ua.com.solidity.db.repositories.YPassportRepository;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.enricher.entities.Contragent;
import ua.com.solidity.enricher.model.EnricherRequest;
import ua.com.solidity.enricher.repository.BaseDrfoRepository;
import ua.com.solidity.enricher.repository.BaseElectionsRepository;
import ua.com.solidity.enricher.repository.BaseFodbRepository;
import ua.com.solidity.enricher.repository.BasePassportsRepository;
import ua.com.solidity.enricher.repository.ContragentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Service
@EntityScan(basePackages = {"ua.com.solidity.db.entities", "ua.com.solidity.enricher.entities"})
@EnableJpaRepositories(basePackages = {"ua.com.solidity.db.repositories", "ua.com.solidity.enricher.repository"})
public class EnricherServiceImpl implements EnricherService {
	private final BaseDrfoRepository bdr;
	private final BaseElectionsRepository ber;
	private final BaseFodbRepository bfr;
	private final BasePassportsRepository bpr;
	private final YPersonRepository ypr;
	private final ContragentRepository cr;
	private final YINNRepository yir;
	private final YPassportRepository yPassportRepository;
	private final EnricherMonitoringNotificationService emnService;

	private static final String BASE_DRFO = "base_drfo";
	private static final String BASE_ELECTIONS = "base_elections";
	private static final String BASE_FODB = "base_fodb";
	private static final String BASE_PASSPORTS = "base_passports";
	private static final String CONTRAGENT = "contragent";
	private static final String ENRICHER = "ENRICHER";
	private static final String RECORDS = "records";

	private static final String NUMBER_REGEX = "^[0-9]+$";

	@Value("${otp.enricher.page-size}")
	private Integer pageSize;
	@Value("${statuslogger.rabbitmq.name}")
	private String queueName;
	private boolean cachedCopy;

	private String importedRecords(long num) {
		return String.format("Imported %d records", num);
	}

	private void logStart(String table) {
		log.info("Data from {} are being transferred to OLAP zone", table);
	}

	private void logFinish(String table, Long rows) {
		log.info("Imported {} records from {}", rows, table);
	}

	@Override
	public void enrich(EnricherRequest er) {
		log.info("Received request to enrich {} revision {}", er.getTable(), er.getRevision());
		switch (er.getTable()) {
			case BASE_DRFO:
				baseDrfoEnrich(er.getRevision());
				break;
			case BASE_ELECTIONS:
				baseElectionsEnrich(er.getRevision());
				break;
			case BASE_FODB:
				baseFodbEnrich(er.getRevision());
				break;
			case BASE_PASSPORTS:
				basePassportsEnrich(er.getRevision());
				break;
			case CONTRAGENT:
				contragentEnrich(er.getRevision());
				break;
//            case "physical_person":
//               physicalPersonEnrich(er.getRevision());
//                break;
			default:
				log.warn("Ignoring unsupported {} enrichment", er.getTable());
		}
	}

	public void baseDrfoEnrich(UUID revision) {
		logStart(BASE_DRFO);

		LocalDateTime startTime = LocalDateTime.now();
		StatusLogger statusLogger = new StatusLogger(revision, 0L, "%",
		                                             BASE_DRFO, ENRICHER, startTime, null, null);
		Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));

		long[] counter = new long[1];

		Pageable pageRequest = PageRequest.of(0, pageSize);
		Page<BaseDrfo> onePage = bdr.findAllByRevision(revision, pageRequest);

		while (!onePage.isEmpty()) {
			pageRequest = pageRequest.next();
			List<YPerson> personList = new ArrayList<>();
			Set<YINN> yinnList = new HashSet<>();

			onePage.forEach(r -> {
				String lastName = UtilString.toUpperCase(r.getLastName());
				String firstName = UtilString.toUpperCase(r.getFirstName());
				String patName = UtilString.toUpperCase(r.getPatName());

				YPerson person = addPerson(yinnList, personList, r.getInn(), null, null,
				                           lastName, firstName, patName, r.getBirthdate());

				Set<String> addresses = new HashSet<>();
				if (StringUtils.isBlank(r.getAllAddresses()))
					addresses = Arrays.stream(r.getAllAddresses().split(" Адрес "))
							.map(UtilString::toUpperCase).collect(Collectors.toSet());
				if (StringUtils.isBlank(r.getResidenceAddress()))
					addresses.add(r.getResidenceAddress().substring(11).toUpperCase());
				if (!StringUtils.isBlank(r.getAddress())) addresses.add(r.getAddress().toUpperCase());
				if (!StringUtils.isBlank(r.getAddress2())) addresses.add(r.getAddress2().toUpperCase());

				addAddresses(person, addresses);

				addAltPerson(person, lastName, firstName, patName);

				if (!cachedCopy) {
					personList.add(person);
					counter[0]++;
				}
			});

			ypr.saveAll(personList);
			emnService.enrichMonitoringNotification(personList);

			onePage = bdr.findAllByRevision(revision, pageRequest);

			statusLogger = new StatusLogger(revision, counter[0], RECORDS,
			                                BASE_DRFO, ENRICHER, startTime, null, null);
			Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
		}

		logFinish(BASE_DRFO, counter[0]);

		statusLogger = new StatusLogger(revision, 100L, "%",
		                                BASE_DRFO, ENRICHER, startTime, LocalDateTime.now(),
		                                importedRecords(counter[0]));
		Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
	}

	public void baseElectionsEnrich(UUID revision) {
		logStart(BASE_ELECTIONS);

		LocalDateTime startTime = LocalDateTime.now();
		StatusLogger statusLogger = new StatusLogger(revision, 0L, "%",
		                                             BASE_ELECTIONS, ENRICHER, startTime, null, null);
		Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));

		long[] counter = new long[1];

		Pageable pageRequest = PageRequest.of(0, pageSize);
		Page<BaseElections> onePage = ber.findAllByRevision(revision, pageRequest);

		while (!onePage.isEmpty()) {
			pageRequest = pageRequest.next();
			List<YPerson> personList = new ArrayList<>();

			onePage.forEach(r -> {
				final String[] fio = r.getFio().split(" ");
				String fistName = UtilString.toUpperCase(fio[1]);
				String lastName = UtilString.toUpperCase(fio[0]);
				String patName = UtilString.toUpperCase(fio[2]);

				YPerson person = addPerson(new HashSet<>(), personList, null, null, null,
				                           lastName, fistName, patName, r.getBirthdate());

				if (!StringUtils.isBlank(r.getAddress())) {
					String[] partAddress = r.getAddress().split(", ");
					StringBuilder sbAddress = new StringBuilder();
					for (int i = partAddress.length - 1; i > 0; i--) {
						sbAddress.append(partAddress[i].toUpperCase()).append(", ");
					}
					sbAddress.append(partAddress[0].toUpperCase());

					Set<String> addresses = new HashSet<>();
					addresses.add(sbAddress.toString());

					addAddresses(person, addresses);
				}

				if (!cachedCopy) {
					personList.add(person);
					counter[0]++;
				}
			});

			ypr.saveAll(personList);
			emnService.enrichMonitoringNotification(personList);

			onePage = ber.findAllByRevision(revision, pageRequest);

			statusLogger = new StatusLogger(revision, counter[0], RECORDS,
			                                BASE_ELECTIONS, ENRICHER, startTime, null, null);
			Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
		}

		logFinish(BASE_ELECTIONS, counter[0]);

		statusLogger = new StatusLogger(revision, 100L, "%",
		                                BASE_ELECTIONS, ENRICHER, startTime,
		                                LocalDateTime.now(),
		                                importedRecords(counter[0]));
		Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
	}

	public void baseFodbEnrich(UUID revision) {
		logStart(BASE_FODB);

		LocalDateTime startTime = LocalDateTime.now();
		StatusLogger statusLogger = new StatusLogger(revision, 0L, "%",
		                                             BASE_FODB, ENRICHER, startTime, null, null);
		Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));

		long[] counter = new long[1];

		Pageable pageRequest = PageRequest.of(0, pageSize);
		Page<BaseFodb> onePage = bfr.findAllByRevision(revision, pageRequest);

		while (!onePage.isEmpty()) {
			pageRequest = pageRequest.next();
			List<YPerson> personList = new ArrayList<>();
			Set<YINN> yinnList = new HashSet<>();

			onePage.forEach(r -> {
				String code = r.getInn();

				String lastName = UtilString.toUpperCase(r.getLastNameUa());
				String firstName = UtilString.toUpperCase(r.getFirstNameUa());
				String patName = UtilString.toUpperCase(r.getMiddleNameUa());

				Long inn = null;
				if (!StringUtils.isBlank(code) && code.matches(NUMBER_REGEX)) inn = Long.parseLong(code);

				YPerson person = addPerson(yinnList, personList, inn, null, null,
				                           lastName, firstName, patName, r.getBirthdate());

				Set<String> addresses = new HashSet<>();
				StringBuilder laString = new StringBuilder();
				if (!StringUtils.isBlank(r.getLiveRegion())) {
					laString.append(r.getLiveRegion().toUpperCase()).append(" ОБЛАСТЬ");
				}
				if (!StringUtils.isBlank(r.getLiveCounty())) {
					laString.append(", ").append(r.getLiveCounty().toUpperCase()).append(" Р-Н");
				}
				laString.append(", ").append(UtilString.toUpperCase(r.getLiveCityType()))
						.append(". ").append(UtilString.toUpperCase(r.getLiveCityUa()));
				laString.append(", ").append(UtilString.toUpperCase(r.getLiveStreetType())).append(" ")
						.append(UtilString.toUpperCase(r.getLiveStreet()));
				if (!StringUtils.isBlank(r.getLiveBuildingNumber()) && !r.getLiveBuildingNumber().equals("0")) {
					laString.append(" ").append(r.getLiveBuildingNumber().toUpperCase());
				}
				if (!StringUtils.isBlank(r.getLiveBuildingApartment()) && !r.getLiveBuildingApartment().equals("0")) {
					laString.append(", КВ. ").append(r.getLiveBuildingApartment().toUpperCase());
				}
				addresses.add(laString.toString());

				addAddresses(person, addresses);

				addAltPerson(person, lastName, firstName, patName);

				if (!cachedCopy) {
					personList.add(person);
					counter[0]++;
				}
			});

			ypr.saveAll(personList);
			emnService.enrichMonitoringNotification(personList);

			onePage = bfr.findAllByRevision(revision, pageRequest);

			statusLogger = new StatusLogger(revision, counter[0], RECORDS,
			                                BASE_FODB, ENRICHER, startTime, null, null);
			Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
		}

		logFinish(BASE_FODB, counter[0]);

		statusLogger = new StatusLogger(revision, 100L, "%",
		                                BASE_FODB, ENRICHER, startTime, LocalDateTime.now(),
		                                importedRecords(counter[0]));
		Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
	}

	public void basePassportsEnrich(UUID revision) {
		logStart(BASE_PASSPORTS);

		LocalDateTime startTime = LocalDateTime.now();
		StatusLogger statusLogger = new StatusLogger(revision, 0L, "%",
		                                             BASE_PASSPORTS, ENRICHER, startTime, null, null);
		Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));

		long[] counter = new long[1];

		Pageable pageRequest = PageRequest.of(0, pageSize);
		Page<BasePassports> onePage = bpr.findAllByRevision(revision, pageRequest);

		while (!onePage.isEmpty()) {
			pageRequest = pageRequest.next();
			List<YPerson> personList = new ArrayList<>();
			Set<YINN> yinnList = new HashSet<>();

			onePage.forEach(r -> {
				if (!StringUtils.isBlank(r.getLastName())) {                          //skip person without lastName
					String code = r.getInn();

					String lastName = UtilString.toUpperCase(r.getLastName());
					String firstName = UtilString.toUpperCase(r.getFirstName());
					String patName = UtilString.toUpperCase(r.getMiddleName());

					Long inn = null;
					if (!StringUtils.isBlank(code) && code.matches(NUMBER_REGEX)) inn = Long.parseLong(code);

					YPerson person = addPerson(yinnList, personList, inn, r.getSerial(), r.getPassId(),
					                           lastName, firstName, patName, r.getBirthdate());

					String passportNo = r.getPassId();
					String passportSerial = r.getSerial();
					Integer number;
					if (!StringUtils.isBlank(passportNo) && passportNo.matches(NUMBER_REGEX)
							&& !StringUtils.isBlank(passportSerial)) {
						number = Integer.parseInt(passportNo);
						Optional<YPassport> passportOptional = person.getPassports()
								.parallelStream()
								.filter(p -> Objects.equals(p.getNumber(), number) && Objects.equals(p.getSeries(), passportSerial))
								.findAny();
						YPassport passport = passportOptional.orElseGet(YPassport::new);
						passport.setSeries(passportSerial);
						passport.setNumber(number);
						passport.setAuthority(null);
						passport.setIssued(null);
						passport.setEndDate(null);
						passport.setRecordNumber(null);
						passport.setValidity(true);
						passport.setType("UA_DOMESTIC");

						if (passportOptional.isEmpty()) {
							passport.setPerson(person);
							person.getPassports().add(passport);
						}
					}

					addAltPerson(person, lastName, firstName, patName);

					if (!cachedCopy) {
						personList.add(person);
						counter[0]++;
					}
				}
			});

			ypr.saveAll(personList);
			emnService.enrichMonitoringNotification(personList);

			onePage = bpr.findAllByRevision(revision, pageRequest);

			statusLogger = new StatusLogger(revision, counter[0], RECORDS,
			                                BASE_PASSPORTS, ENRICHER, startTime, null, null);
			Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
		}

		logFinish(BASE_PASSPORTS, counter[0]);

		statusLogger = new StatusLogger(revision, 100L, "%",
		                                BASE_PASSPORTS, ENRICHER, startTime, LocalDateTime.now(),
		                                importedRecords(counter[0]));
		Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
	}

	public void contragentEnrich(UUID revision) {
		logStart(CONTRAGENT);

		LocalDateTime startTime = LocalDateTime.now();
		StatusLogger statusLogger = new StatusLogger(revision, 0L, "%",
		                                             CONTRAGENT, ENRICHER, startTime, null, null);
		Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));

		long[] counter = new long[1];

		Pageable pageRequest = PageRequest.of(0, pageSize);
		Page<Contragent> onePage = cr.findAllByRevision(revision, pageRequest);

		while (!onePage.isEmpty()) {
			pageRequest = pageRequest.next();
			List<YPerson> personList = new ArrayList<>();
			Set<YINN> yinnList = new HashSet<>();

			onePage.forEach(r -> {
				Optional<YINN> yinnSavedOptional;
				Optional<YINN> yinnCachedOptional;
				String identifyCode = r.getIdentifyCode();

				if (!StringUtils.isBlank(identifyCode) && identifyCode.matches(NUMBER_REGEX)) {// We skip person without inn
					long inn = Long.parseLong(identifyCode);
					yinnSavedOptional = yir.findByInn(inn);
					yinnCachedOptional = yinnList.stream().filter(i -> i.getInn() == inn).findAny();
					boolean cached = false;
					boolean saved = false;

					YPerson person = null;
					if (yinnSavedOptional.isPresent()) {
						person = yinnSavedOptional.get().getPerson();
						saved = true;
					}
					if (yinnCachedOptional.isPresent()) {
						person = yinnCachedOptional.get().getPerson();
						cached = true;
					}
					if (person == null) person = new YPerson(UUID.randomUUID());

					List<String> splitPersonNames = Arrays.asList(r.getName().split("[ ]+"));
					String splitPatronymic = splitPersonNames.size() == 3 ? splitPersonNames.get(2) : null;

					if (!StringUtils.isBlank(r.getClientPatronymicName()))
						person.setPatName(chooseNotBlank(person.getPatName(), r.getClientPatronymicName()));
					else person.setPatName(chooseNotBlank(person.getPatName(), splitPatronymic));
					person.setLastName(chooseNotBlank(person.getLastName(), r.getClientLastName()));

					if (!StringUtils.isBlank(r.getClientName())) person.setFirstName(r.getClientName());
					else if (splitPersonNames.size() == 3) {
						person.setFirstName(splitPersonNames.get(1).equals(r.getClientLastName()) ? splitPersonNames.get(0) : splitPersonNames.get(1));
					}

					person.setBirthdate(chooseNotNull(person.getBirthdate(), r.getClientBirthday()));

					YINN yinn = null;
					if (yinnSavedOptional.isPresent()) yinn = yinnSavedOptional.get();
					if (yinnCachedOptional.isPresent()) yinn = yinnCachedOptional.get();
					if (yinn == null) yinn = new YINN();
					if (!cached && !saved) {
						yinn.setInn(Long.parseLong(identifyCode));
						yinn.setPerson(person);
						yinn.getPerson().getInns().add(yinn);
					}

					String passportNo = r.getPassportNo();
					String passportSerial = r.getPassportSerial();
					Integer number;
					if (!StringUtils.isBlank(passportNo) && passportNo.matches(NUMBER_REGEX)) {
						number = Integer.parseInt(passportNo);
						Optional<YPassport> passportOptional = person.getPassports()
								.stream()
								.filter(p -> Objects.equals(p.getNumber(), number) && Objects.equals(p.getSeries(), passportSerial))
								.findAny();
						YPassport passport = passportOptional.orElseGet(YPassport::new);
						passport.setSeries(passportSerial);
						passport.setNumber(number);
						passport.setAuthority(chooseNotBlank(passport.getAuthority(), r.getPassportIssuePlace()));
						passport.setIssued(chooseNotNull(passport.getIssued(), r.getPassportIssueDate()));
						passport.setEndDate(chooseNotNull(passport.getEndDate(), r.getPassportEndDate()));
						passport.setRecordNumber(null);
						passport.setValidity(true);
						passport.setType("UA_DOMESTIC");

						if (passportOptional.isEmpty()) {
							passport.setPerson(person);
							person.getPassports().add(passport);
						}
					}

					Optional<YTag> tagPlaceOptional = person.getTags()
							.stream()
							.filter(t -> Objects.equals(t.getName(), r.getWorkplace()))
							.findAny();
					if (!StringUtils.isBlank(r.getWorkplace())) {
						YTag tag = tagPlaceOptional.orElseGet(YTag::new);
						tag.setName(r.getWorkplace());
						if (tag.getAsOf() == null) tag.setAsOf(LocalDate.now());
						tag.setSource(CONTRAGENT);

						if (tagPlaceOptional.isEmpty()) {
							tag.setPerson(person);
							person.getTags().add(tag);
						}
					}

					Optional<YTag> tagPositionOptional = person.getTags()
							.stream()
							.filter(t -> Objects.equals(t.getName(), r.getWorkPosition()))
							.findAny();
					if (!StringUtils.isBlank(r.getWorkPosition())) {
						YTag tag = tagPositionOptional.orElseGet(YTag::new);
						tag.setName(r.getWorkPosition());
						if (tag.getAsOf() == null) tag.setAsOf(LocalDate.now());
						tag.setSource(CONTRAGENT);

						if (tagPositionOptional.isEmpty()) {
							tag.setPerson(person);
							person.getTags().add(tag);
						}
					}

					Set<String> phones = person.getPhones()
							.stream()
							.map(p -> p.getPhone().replaceAll("[^0-9]+", ""))
							.collect(Collectors.toSet());
					YPerson finalPerson = person;
					Stream.of(r.getPhones(), r.getMobilePhone(), r.getPhoneHome()).forEach(c -> {
						if (c != null) {
							String phoneCleaned = c.replaceAll("[^0-9]+", "");
							if (!StringUtils.isBlank(phoneCleaned) && !phones.contains(phoneCleaned)) {
								YPhone phone = new YPhone();
								phone.setPhone(phoneCleaned);

								phone.setPerson(finalPerson);
								finalPerson.getPhones().add(phone);
							}
						}
					});

					Set<String> addresses = person.getAddresses()
							.stream()
							.map(YAddress::getAddress)
							.collect(Collectors.toSet());
					YPerson finalPerson1 = person;
					Stream.of(r.getAddress(), r.getBirthplace()).forEach(c -> {
						if (!StringUtils.isBlank(c) && !addresses.contains(c)) {
							YAddress address = new YAddress();
							address.setAddress(c);

							address.setPerson(finalPerson1);
							finalPerson1.getAddresses().add(address);
						}
					});

					if (!StringUtils.isBlank(r.getAlternateName())) {
						YAltPerson newAltPerson = new YAltPerson();
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
						Optional<YAltPerson> altPersonOptional = person.getAltPeople().stream()
								.filter(p -> Objects.equals(p.getFirstName(), names[0]) &&
										Objects.equals(p.getLastName(), names[1]) &&
										Objects.equals(p.getPatName(), names[2]))
								.findAny();

						if (altPersonOptional.isEmpty()) {
							newAltPerson.setFirstName(UtilString.toUpperCase(names[0]));
							newAltPerson.setLastName(UtilString.toUpperCase(names[1]));
							newAltPerson.setPatName(UtilString.toUpperCase(names[2]));
							newAltPerson.setLanguage("EN");

							newAltPerson.setPerson(person);
							person.getAltPeople().add(newAltPerson);
						}
					}

					if (!StringUtils.isBlank(r.getEmail())) {
						Optional<YEmail> emailOptional = person.getEmails()
								.stream()
								.filter(e -> r.getEmail().equals(e.getEmail()))
								.findAny();
						if (emailOptional.isEmpty()) {
							YEmail email = new YEmail();
							email.setEmail(r.getEmail());

							email.setPerson(person);
							person.getEmails().add(email);
						}
					}
					if (!cached) {
						personList.add(person);
						yinnList.add(yinn);
						counter[0]++;
					}
				}

			});

			ypr.saveAll(personList);
			emnService.enrichMonitoringNotification(personList);


			onePage = cr.findAllByRevision(revision, pageRequest);

			statusLogger = new StatusLogger(revision, counter[0], RECORDS,
			                                CONTRAGENT, ENRICHER, startTime, null, null);
			Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
		}

		logFinish(CONTRAGENT, counter[0]);

		statusLogger = new StatusLogger(revision, 100L, "%",
		                                CONTRAGENT, ENRICHER, startTime, LocalDateTime.now(),
		                                importedRecords(counter[0]));
		Utils.sendRabbitMQMessage(queueName, Utils.objectToJsonString(statusLogger));
	}

	private <T> T chooseNotNull(T first, T second) {
		return second != null ? second : first;
	}

	private String chooseNotBlank(String first, String second) {
		return !StringUtils.isBlank(second) ? second : first;
	}

	private void addAltPerson(YPerson person, String lastName, String firstName, String patName) {
		if (!(Objects.equals(person.getLastName(), lastName)
				&& Objects.equals(person.getFirstName(), firstName)
				&& Objects.equals(person.getPatName(), patName))) {
			Optional<YAltPerson> altPersonOptional = person.getAltPeople()
					.parallelStream()
					.filter(p -> Objects.equals(p.getPerson().getLastName(), lastName)
							&& Objects.equals(p.getPerson().getFirstName(), firstName)
							&& Objects.equals(p.getPerson().getPatName(), patName))
					.findAny();
			YAltPerson altPerson = altPersonOptional.orElseGet(YAltPerson::new);
			altPerson.setFirstName(firstName);
			altPerson.setLastName(lastName);
			altPerson.setPatName(patName);
			altPerson.setLanguage("UA");

			if (altPersonOptional.isEmpty()) {
				altPerson.setPerson(person);
				person.getAltPeople().add(altPerson);
			}
		}
	}

	private void addAddresses(YPerson person, Set<String> addresses) {
		addresses.forEach(a -> {
			Optional<YAddress> addressOptional = person.getAddresses()
					.stream()
					.filter(adr -> Objects.equals(adr.getAddress(), a))
					.findAny();
			YAddress address = addressOptional.orElseGet(YAddress::new);
			address.setAddress(chooseNotBlank(address.getAddress(), a));

			if (addressOptional.isEmpty()) {
				address.setPerson(person);
				person.getAddresses().add(address);
			}
		});
	}

	private YPerson addPerson(Set<YINN> yinnList, List<YPerson> personList, Long inn,
	                          String seriesPassport, String numberPassport,
	                          String lastName, String firstName, String patName, LocalDate birthDate) {

		boolean find = false;
		cachedCopy = false;

		YPerson person = null;
		YINN yinn = null;

		if (inn != null) {
			Optional<YINN> yinnCachedOptional = yinnList.parallelStream()
					.filter(i -> Objects.equals(i.getInn(), inn)).findAny();
			if (yinnCachedOptional.isPresent()) {
				person = yinnCachedOptional.get().getPerson();
				yinn = yinnCachedOptional.get();
				cachedCopy = true;
				find = true;
			}

			if (!find) {
				Optional<YINN> yinnSavedOptional = yir.findByInn(inn);
				if (yinnSavedOptional.isPresent()) {
					person = yinnSavedOptional.get().getPerson();
					yinn = yinnSavedOptional.get();
					find = true;
				}
			}
		}

		if (!StringUtils.isBlank(seriesPassport) && !StringUtils.isBlank(numberPassport)
				&& numberPassport.matches(NUMBER_REGEX)) {
			if (!find) {
				Optional<YPerson> yPassportCachedOptional = personList.parallelStream().filter(p -> p.getPassports().stream()
						.anyMatch(pas -> Objects.equals(pas.getNumber(), Integer.parseInt(numberPassport))
								&& Objects.equals(pas.getSeries(), seriesPassport))).findAny();
				if (yPassportCachedOptional.isPresent()) {
					person = yPassportCachedOptional.get();
					cachedCopy = true;
					find = true;
				}
			}

			if (!find) {
				Optional<YPassport> yPassportSavedOptional = yPassportRepository.findByNumberAndSeries(Integer
						                                                                                       .parseInt(numberPassport), seriesPassport);
				if (yPassportSavedOptional.isPresent()) {
					person = yPassportSavedOptional.get().getPerson();
					find = true;
				}
			}
		}

		if (!StringUtils.isBlank(firstName) && !StringUtils.isBlank(lastName)
				&& !StringUtils.isBlank(patName) && birthDate != null) {
			if (!find) {
				List<YPerson> yPersonCachedList = personList.parallelStream().filter(p -> Objects.equals(p.getLastName(), lastName)
								&& Objects.equals(p.getFirstName(), firstName)
								&& Objects.equals(p.getPatName(), patName)
								&& Objects.equals(birthDate, p.getBirthdate()))
						.collect(Collectors.toList());
				if (yPersonCachedList.size() == 1) {
					person = yPersonCachedList.get(0);
					cachedCopy = true;
					find = true;
				}
			}

			if (!find) {
				List<YPerson> yPersonSavedList = ypr.findByLastNameAndFirstNameAndPatNameAndBirthdate(lastName,
				                                                                                      firstName, patName, birthDate);
				if (yPersonSavedList.size() == 1) {
					person = yPersonSavedList.get(0);
				}
			}
		}

		if (person == null) {
			person = new YPerson(UUID.randomUUID());
			if (!StringUtils.isBlank(lastName)) person.setLastName(lastName.toUpperCase());
			if (!StringUtils.isBlank(firstName)) person.setFirstName(firstName.toUpperCase());
			if (!StringUtils.isBlank(patName)) person.setPatName(patName.toUpperCase());
		}

		if (yinn == null && inn != null) {
			yinn = new YINN();
			yinn.setInn(inn);
			yinn.setPerson(person);
			yinn.getPerson().getInns().add(yinn);
			yinnList.add(yinn);
		}

		person.setBirthdate(chooseNotNull(person.getBirthdate(), birthDate));

		return person;
	}
}
