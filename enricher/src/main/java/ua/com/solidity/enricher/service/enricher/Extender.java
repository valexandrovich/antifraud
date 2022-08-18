package ua.com.solidity.enricher.service.enricher;

import static ua.com.solidity.enricher.util.Chooser.chooseNotBlank;
import static ua.com.solidity.enricher.util.Chooser.chooseNotNull;
import static ua.com.solidity.enricher.util.Regex.INN_FORMAT_REGEX;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.YAddress;
import ua.com.solidity.db.entities.YAltCompany;
import ua.com.solidity.db.entities.YAltPerson;
import ua.com.solidity.db.entities.YCAddress;
import ua.com.solidity.db.entities.YCTag;
import ua.com.solidity.db.entities.YCompany;
import ua.com.solidity.db.entities.YCompanyRelation;
import ua.com.solidity.db.entities.YCompanyRelationCompany;
import ua.com.solidity.db.entities.YCompanyRole;
import ua.com.solidity.db.entities.YEmail;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.db.entities.YPassport;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.entities.YPhone;
import ua.com.solidity.db.entities.YTag;
import ua.com.solidity.db.repositories.YPassportRepository;
import ua.com.solidity.db.repositories.YPersonRepository;

@Component
@RequiredArgsConstructor
public class Extender {

    private final YPersonRepository ypr;
    private final YPassportRepository yPassportRepository;

    public void addAltPerson(YPerson person, String lastName, String firstName,
                             String patName, String language,
                             ImportSource source) {
        Optional<YAltPerson> altPersonOptional = person.getAltPeople()
                .parallelStream()
                .filter(p -> (Objects.equals(p.getLastName(), lastName)
                        && Objects.equals(p.getFirstName(), firstName)
                        && Objects.equals(p.getPatName(), patName)))
                .findAny();
        YAltPerson altPerson = altPersonOptional.orElseGet(YAltPerson::new);
        addSource(altPerson.getImportSources(), source);

        altPerson.setFirstName(chooseNotNull(altPerson.getFirstName(), firstName));
        altPerson.setLastName(chooseNotNull(altPerson.getLastName(), lastName));
        altPerson.setPatName(chooseNotNull(altPerson.getPatName(), patName));
        altPerson.setLanguage(chooseNotNull(altPerson.getLanguage(), language));

        if (altPersonOptional.isEmpty()) {
            altPerson.setPerson(person);
            person.getAltPeople().add(altPerson);
        }
    }


    public void addAltCompany(YCompany company, String name, String language,
                              ImportSource source) {
        Optional<YAltCompany> altCompanyOptional = company.getAltCompanies()
                .parallelStream()
                .filter(p -> (Objects.equals(p.getCompany().getName(), name)))
                .findAny();
        YAltCompany altCompany = altCompanyOptional.orElseGet(YAltCompany::new);
        addSource(altCompany.getImportSources(), source);

        altCompany.setName(chooseNotNull(altCompany.getName(), name));
        altCompany.setLanguage(chooseNotNull(altCompany.getLanguage(), language));

        if (altCompanyOptional.isEmpty()) {
            altCompany.setCompany(company);
            company.getAltCompanies().add(altCompany);
        }
    }

    public YPerson addPassport(YPassport ypassport, Set<YPerson> personSet,
                               ImportSource source, YPerson person, Set<YPerson> savedPeople,
                               Set<YPassport> passports) {
        YPassport passport;
        Set<YINN> inns = person.getInns().parallelStream().filter(Objects::nonNull).collect(Collectors.toSet());
        Set<YAltPerson> altPeople = person.getAltPeople().parallelStream().filter(Objects::nonNull).collect(Collectors.toSet());
        Set<YCompanyRelation> companyRelations = person.getCompanyRelations().parallelStream().filter(Objects::nonNull).collect(Collectors.toSet());
        Set<ImportSource> importSources = person.getImportSources().parallelStream().filter(Objects::nonNull).collect(Collectors.toSet());

        List<YPerson> yPersonList = personSet.parallelStream()
                .filter(p -> p.getPassports().contains(ypassport))
                .collect(Collectors.toList());
        yPersonList.addAll(savedPeople.parallelStream()
                .filter(p -> p.getPassports()
                        .contains(ypassport))
                .collect(Collectors.toList()));

        Optional<YPassport> optionalYPassport = yPersonList.parallelStream()
                .flatMap(p -> p.getPassports().parallelStream())
                .filter(p -> Objects.equals(p.getSeries(), ypassport.getSeries())
                        && Objects.equals(p.getNumber(), ypassport.getNumber())
                        && Objects.equals(p.getType(), ypassport.getType())).findAny();

        if (optionalYPassport.isEmpty()) optionalYPassport = passports.parallelStream()
                .filter(p -> Objects.equals(p.getSeries(), ypassport.getSeries())
                        && Objects.equals(p.getNumber(), ypassport.getNumber())
                        && Objects.equals(p.getType(), ypassport.getType())).findAny();

        passport = optionalYPassport.orElseGet(YPassport::new);
        addSource(passport.getImportSources(), source);
        passport.setSeries(chooseNotBlank(passport.getSeries(), ypassport.getSeries()));
        passport.setNumber(ypassport.getNumber());
        passport.setAuthority(chooseNotBlank(passport.getAuthority(), ypassport.getAuthority()));
        passport.setIssued(chooseNotNull(passport.getIssued(), ypassport.getIssued()));
        passport.setEndDate(chooseNotNull(passport.getEndDate(), ypassport.getEndDate()));
        passport.setRecordNumber(chooseNotBlank(passport.getRecordNumber(), ypassport.getRecordNumber()));
        passport.setType(ypassport.getType());
        passport.setValidity(ypassport.getValidity());

        boolean isFindPerson = false;
        Optional<YPerson> optionalYPerson = Optional.empty();
        if (!yPersonList.isEmpty()) {
            YPerson finalPerson = person;
            isFindPerson = yPersonList.parallelStream().anyMatch(p -> isEqualsPerson(p, finalPerson));
            optionalYPerson = yPersonList.parallelStream().filter(p -> isEqualsPerson(p, finalPerson) || p.getAltPeople().stream().anyMatch(alt -> isEqualsPersonAndAltPerson(alt, finalPerson))).findAny();
        }

        if (optionalYPerson.isPresent()) {
            YPerson findPerson = optionalYPerson.get();
            if (isFindPerson) {
                findPerson.setLastName(chooseNotNull(findPerson.getLastName(), person.getLastName()));
                findPerson.setFirstName(chooseNotNull(findPerson.getFirstName(), person.getFirstName()));
                findPerson.setPatName(chooseNotNull(findPerson.getPatName(), person.getPatName()));
                findPerson.setBirthdate(chooseNotNull(findPerson.getBirthdate(), person.getBirthdate()));
            }
            altPeople.forEach(p -> p.setPerson(findPerson));
            inns.forEach(i -> i.setPerson(findPerson));
            companyRelations.forEach(c -> c.setPerson(findPerson));
            findPerson.getAltPeople().addAll(altPeople);
            findPerson.getInns().addAll(inns);
            findPerson.getImportSources().addAll(importSources);
            findPerson.getCompanyRelations().addAll(companyRelations);

            person = findPerson;
        } else if (optionalYPassport.isPresent() && passport.getId() == null) {
            yPassportRepository.save(passport);
            passports.add(passport);
        }
        if (StringUtils.isBlank(person.getLastName()) && StringUtils.isBlank(person.getFirstName())
                && StringUtils.isBlank(person.getPatName()) && inns.isEmpty()) {
            yPassportRepository.save(passport);
            passports.add(passport);
        }
        person.getPassports().add(passport);
        personSet.remove(person);
        passports.add(passport);
        return person;
    }

    public YPerson addInn(Long inn, Set<YPerson> personSet,
                          ImportSource source, YPerson person,
                          Set<YINN> inns, Set<YPerson> savedPeople) {

        Optional<YPerson> findPerson = personSet.parallelStream().filter(p -> p.getInns()
                .parallelStream().anyMatch(i -> Objects.equals(i.getInn(), inn))).findAny();
        if (findPerson.isEmpty())
            findPerson = savedPeople.parallelStream().filter(p -> p.getInns()
                    .parallelStream().anyMatch(i -> Objects.equals(i.getInn(), inn))).findAny();

        Optional<YINN> optionalYINN = Optional.empty();
        if (findPerson.isPresent())
            optionalYINN = findPerson.get().getInns().parallelStream()
                    .filter(i -> Objects.equals(i.getInn(), inn)).findAny();

        if (optionalYINN.isEmpty())
            optionalYINN = inns.parallelStream().filter(i -> Objects.equals(i.getInn(), inn)).findAny();

        YINN yinn = optionalYINN.orElseGet(YINN::new);

        addSource(yinn.getImportSources(), source);

        yinn.setInn(chooseNotNull(yinn.getInn(), inn));

        if (findPerson.isPresent() && isEqualsPerson(findPerson.get(), person)) {
            YPerson oldPerson = findPerson.get();
            oldPerson.setLastName(chooseNotNull(oldPerson.getLastName(), person.getLastName()));
            oldPerson.setFirstName(chooseNotNull(oldPerson.getFirstName(), person.getFirstName()));
            oldPerson.setPatName(chooseNotNull(oldPerson.getPatName(), person.getPatName()));
            oldPerson.setBirthdate(chooseNotNull(oldPerson.getBirthdate(), person.getBirthdate()));

            person = oldPerson;
        } else if (findPerson.isPresent()
                && StringUtils.isNotBlank(person.getLastName())
                && StringUtils.isNotBlank(person.getFirstName())
                && StringUtils.isNotBlank(person.getPatName())
                && StringUtils.isNotBlank(findPerson.get().getLastName())
                && StringUtils.isNotBlank(findPerson.get().getFirstName())
                && StringUtils.isNotBlank(findPerson.get().getPatName())) {
            YPerson oldPerson = findPerson.get();
            addAltPerson(oldPerson, person.getLastName(), person.getFirstName(), person.getPatName(), "UA", source);
            person = oldPerson;
        }

        if (optionalYINN.isEmpty()) {
            person.getInns().add(yinn);
            yinn.setPerson(person);
        }
        personSet.remove(person);
        return person;
    }

    public boolean isEqualsPerson(YPerson person, YPerson newPerson) {
        if (StringUtils.isBlank(person.getLastName())
                && StringUtils.isBlank(person.getFirstName())
                && StringUtils.isBlank(person.getPatName())
                && StringUtils.isBlank(newPerson.getLastName())
                && StringUtils.isBlank(newPerson.getFirstName())
                && StringUtils.isBlank(newPerson.getPatName()))
            return false;
        return !(newPerson != null
                && ((StringUtils.isNotBlank(person.getLastName())
                && StringUtils.isNotBlank(newPerson.getLastName())
                && !person.getLastName().equals(newPerson.getLastName()))
                || (StringUtils.isNotBlank(person.getFirstName())
                && StringUtils.isNotBlank(newPerson.getFirstName())
                && !person.getFirstName().equals(newPerson.getFirstName()))
                || (StringUtils.isNotBlank(person.getPatName())
                && StringUtils.isNotBlank(newPerson.getPatName())
                && !person.getPatName().equals(newPerson.getPatName()))
                || (person.getBirthdate() != null && newPerson.getBirthdate() != null
                && !person.getBirthdate().equals(newPerson.getBirthdate()))));
    }

    public boolean isEqualsPersonAndAltPerson(YAltPerson person, YPerson newPerson) {
        return !(newPerson != null && person != null
                && ((StringUtils.isNotBlank(person.getLastName())
                && StringUtils.isNotBlank(newPerson.getLastName())
                && !person.getLastName().equals(newPerson.getLastName()))
                || (StringUtils.isNotBlank(person.getFirstName())
                && StringUtils.isNotBlank(newPerson.getFirstName())
                && !person.getFirstName().equals(newPerson.getFirstName()))
                || (StringUtils.isNotBlank(person.getPatName())
                && StringUtils.isNotBlank(newPerson.getPatName())
                && !person.getPatName().equals(newPerson.getPatName()))));
    }

    public void addAddresses(YPerson person, Set<YAddress> addresses, ImportSource source) {
        addresses.forEach(a -> {
            Optional<YAddress> addressOptional = person.getAddresses()
                    .parallelStream()
                    .filter(adr -> Objects.equals(adr, a))
                    .findAny();
            YAddress address = addressOptional.orElseGet(YAddress::new);
            addSource(address.getImportSources(), source);
            address.setAddress(chooseNotBlank(address.getAddress(), a.getAddress()));

            address.setPerson(person);
            person.getAddresses().add(address);
        });
    }

    public void addCAddresses(YCompany company, Set<YCAddress> addresses, ImportSource source) {
        addresses.forEach(a -> {
            Optional<YCAddress> addressOptional = company.getAddresses()
                    .parallelStream()
                    .filter(adr -> Objects.equals(adr, a))
                    .findAny();
            YCAddress address = addressOptional.orElseGet(YCAddress::new);
            addSource(address.getImportSources(), source);
            address.setAddress(chooseNotBlank(address.getAddress(), a.getAddress()));

            address.setCompany(company);
            company.getAddresses().add(address);
        });
    }

    public void addPhones(YPerson person, Set<YPhone> phones, ImportSource source) {
        phones.forEach(p -> {
            Optional<YPhone> phonesOptional = person.getPhones()
                    .parallelStream()
                    .filter(ph -> Objects.equals(ph, p))
                    .findAny();
            YPhone phone = phonesOptional.orElseGet(YPhone::new);
            addSource(phone.getImportSources(), source);
            phone.setPhone(chooseNotBlank(phone.getPhone(), p.getPhone()));

            phone.setPerson(person);
            person.getPhones().add(phone);
        });
    }

    public void addEmails(YPerson person, Set<YEmail> emails, ImportSource source) {
        emails.forEach(e -> {
            Optional<YEmail> emailOptional = person.getEmails()
                    .parallelStream()
                    .filter(em -> Objects.equals(em, e))
                    .findAny();
            YEmail email = emailOptional.orElseGet(YEmail::new);
            addSource(email.getImportSources(), source);
            email.setEmail(chooseNotBlank(email.getEmail(), e.getEmail()));

            email.setPerson(person);
            person.getEmails().add(email);
        });
    }

    public void addTags(YPerson person, Set<YTag> tags, ImportSource source) {
        tags.forEach(t -> {
            Optional<YTag> tagOptional = person.getTags()
                    .parallelStream()
                    .filter(tg -> Objects.equals(tg.getTagType(), t.getTagType())
                            && Objects.equals(tg.getAsOf(), t.getAsOf())
                            && Objects.equals(tg.getUntil(), t.getUntil())).findAny();
            YTag tag = tagOptional.orElseGet(YTag::new);
            addSource(tag.getImportSources(), source);

            if (t.getTagType() != null) {
                tag.setTagType(chooseNotNull(tag.getTagType(), t.getTagType()));
                tag.setAsOf(chooseNotNull(tag.getAsOf(), t.getAsOf()));
                tag.setUntil(chooseNotNull(tag.getUntil(), t.getUntil()));
                tag.setSource(chooseNotBlank(tag.getSource(), t.getSource()));

                tag.setPerson(person);
                person.getTags().add(tag);
            }
        });
    }

    public void addTags(YCompany company, Set<YCTag> cTags, ImportSource source) {
        cTags.forEach(t -> {
            Optional<YCTag> tagOptional = company.getTags()
                    .parallelStream()
                    .filter(tg -> Objects.equals(tg.getTagType(), t.getTagType())
                            && Objects.equals(tg.getAsOf(), t.getAsOf())
                            && Objects.equals(tg.getUntil(), t.getUntil())).findAny();
            YCTag tag = tagOptional.orElseGet(YCTag::new);
            addSource(tag.getImportSources(), source);

            if (t.getTagType() != null) {
                tag.setTagType(chooseNotNull(tag.getTagType(), t.getTagType()));
                tag.setAsOf(chooseNotNull(tag.getAsOf(), t.getAsOf()));
                tag.setUntil(chooseNotNull(tag.getUntil(), t.getUntil()));
                tag.setSource(chooseNotBlank(tag.getSource(), t.getSource()));

                tag.setCompany(company);
                company.getTags().add(tag);
            }
        });
    }

    public YPerson addPerson(Set<YPerson> personSet, YPerson person, ImportSource source, boolean fullUnload) {
        boolean find = false;
        Set<YINN> inns = person.getInns().parallelStream().filter(Objects::nonNull).collect(Collectors.toSet());
        Set<YPassport> passports = person.getPassports().parallelStream().filter(Objects::nonNull).collect(Collectors.toSet());
        Set<YAltPerson> altPeople = person.getAltPeople().parallelStream().filter(Objects::nonNull).collect(Collectors.toSet());
        Set<YCompanyRelation> companyRelations = person.getCompanyRelations().parallelStream().filter(Objects::nonNull).collect(Collectors.toSet());
        Set<ImportSource> importSources = person.getImportSources().parallelStream().filter(Objects::nonNull).collect(Collectors.toSet());

        if (person.getId() == null) {
            YPerson yPerson = person;
            List<YPerson> yPersonCachedList = personSet.parallelStream().filter(p -> isEqualsPerson(p, yPerson))
                    .collect(Collectors.toList());
            if (yPersonCachedList.size() == 1) {
                person = yPersonCachedList.get(0);
                find = true;
            }
            if (!find) {
                List<YPerson> yPersonSavedList = ypr.findByLastNameAndFirstNameAndPatNameAndBirthdate(person.getLastName(),
                        person.getFirstName(), person.getPatName(), person.getBirthdate());
                if (yPersonSavedList.size() == 1) {
                    if (fullUnload)
                        person = ypr.findWithInnsAndPassportsAndTagsAndPhonesAndAddressesAndAltPeopleAndEmailsAndImportSourcesById(yPersonSavedList.get(0).getId())
                                .orElse(yPersonSavedList.get(0));
                    else person = ypr.findForBaseEnricherById(yPersonSavedList.get(0).getId())
                            .orElse(yPersonSavedList.get(0));
                }
            }

            if (!StringUtils.isBlank(person.getLastName()))
                person.setLastName(chooseNotNull(person.getLastName(), person.getLastName().toUpperCase()));
            if (!StringUtils.isBlank(person.getFirstName()))
                person.setFirstName(chooseNotNull(person.getFirstName(), person.getFirstName().toUpperCase()));
            if (!StringUtils.isBlank(person.getPatName()))
                person.setPatName(chooseNotNull(person.getPatName(), person.getPatName().toUpperCase()));
            person.setBirthdate(chooseNotNull(person.getBirthdate(), person.getBirthdate()));
        }
        if (person.getId() == null) person.setId(UUID.randomUUID());
        addSource(person.getImportSources(), source);

        if (!person.getInns().isEmpty() && person.getBirthdate() == null) addBirthdayByInn(person);

        YPerson finalPerson = person;
        inns.forEach(i -> i.setPerson(finalPerson));
        altPeople.forEach(p -> p.setPerson(finalPerson));
        companyRelations.forEach(c -> c.setPerson(finalPerson));
        person.getInns().addAll(inns);
        person.getPassports().addAll(passports);
        person.getAltPeople().addAll(altPeople);
        person.getCompanyRelations().addAll(companyRelations);
        person.getImportSources().addAll(importSources);

        personSet.remove(person);
        if (StringUtils.isNotBlank(person.getLastName()) || StringUtils.isNotBlank(person.getFirstName())
                || StringUtils.isNotBlank(person.getPatName()) || !person.getInns().isEmpty())
            personSet.add(person);
        return person;
    }

    public void addCompanyRelation(YPerson person, YCompany company, YCompanyRole role, ImportSource source) {
        Optional<YCompanyRelation> yCompanyRelationOptional = person.getCompanyRelations().parallelStream()
                .filter(mr -> Objects.equals(mr.getCompany(), company)
                        && Objects.equals(mr.getPerson(), person)
                        && Objects.equals(mr.getRole(), role)).findAny();

        YCompanyRelation yCompanyRelation = yCompanyRelationOptional.orElseGet(YCompanyRelation::new);
        yCompanyRelation.setCompany(company);
        yCompanyRelation.setPerson(person);
        yCompanyRelation.setRole(role);
        person.getCompanyRelations().add(yCompanyRelation);
    }

    public void addCompanyRelation(YCompany companyCreator, YCompany company, YCompanyRole role, ImportSource source) {
        Optional<YCompanyRelationCompany> yCompanyRelationOptional = companyCreator.getCompanyRelationsWithCompanies().parallelStream()
                .filter(mr -> Objects.equals(mr.getCompany(), company)
                        && Objects.equals(mr.getCompanyCreator(), companyCreator)
                        && Objects.equals(mr.getRole(), role)).findAny();

        YCompanyRelationCompany yCompanyRelation = yCompanyRelationOptional.orElseGet(YCompanyRelationCompany::new);
        yCompanyRelation.setCompany(company);
        yCompanyRelation.setCompanyCreator(companyCreator);
        yCompanyRelation.setRole(role);
        companyCreator.getCompanyRelationsWithCompanies().add(yCompanyRelation);
    }

    public LocalDate stringToDate(String date) {
        LocalDate localDate = null;
        if (!StringUtils.isBlank(date)) {
            localDate = LocalDate.of(Integer.parseInt(date.substring(6)),
                    Integer.parseInt(date.substring(3, 5)),
                    Integer.parseInt(date.substring(0, 2)));
        }
        return localDate;
    }

    public YCompany addCompany(Set<YCompany> companySet,
                               ImportSource source,
                               YCompany company,
                               Set<YCompany> companies) {
        YCompany yCompany;
        Optional<YCompany> optionalYCompany = companySet.parallelStream()
                .filter(c -> Objects.equals(c.getEdrpou(), company.getEdrpou())
                        || Objects.equals(c.getPdv(), company.getPdv())).findAny();
        if (optionalYCompany.isEmpty())
            optionalYCompany = companies.parallelStream()
                    .filter(c -> Objects.equals(c.getEdrpou(), company.getEdrpou())
                            || Objects.equals(c.getPdv(), company.getPdv())).findAny();

        yCompany = optionalYCompany.orElseGet(YCompany::new);
        if (yCompany.getId() == null) yCompany.setId(UUID.randomUUID());
        else if (StringUtils.isNotBlank(yCompany.getName()) && StringUtils.isNotBlank(company.getName())
                && !yCompany.getName().equals(company.getName()))
            addAltCompany(yCompany, company.getName(), "UA", source);

        addSource(yCompany.getImportSources(), source);

        yCompany.setEdrpou(chooseNotNull(yCompany.getEdrpou(), company.getEdrpou()));
        yCompany.setPdv(chooseNotNull(yCompany.getPdv(), company.getPdv()));
        yCompany.setName(chooseNotBlank(yCompany.getName(), company.getName()));
        yCompany.setState(chooseNotNull(yCompany.getState(), company.getState()));

        companySet.add(yCompany);
        return yCompany;
    }

    public void addSource(Set<ImportSource> sources, ImportSource source) {
        if (source != null) sources.add(source);
    }

    private static final LocalDate START_DATE = LocalDate.of(1900, 1, 1);

    private void addBirthdayByInn(YPerson person) {
        Optional<YINN> optionalYINN = person.getInns().parallelStream().findFirst();
        if (optionalYINN.isPresent()) {
            String inn = String.format(INN_FORMAT_REGEX, optionalYINN.get().getInn());
            LocalDate birthDay = LocalDate.ofEpochDay(Long.parseLong(inn.substring(0, 5)) + START_DATE.toEpochDay() - 1L);
            person.setBirthdate(birthDay);
        }
    }
}
