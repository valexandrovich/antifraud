package ua.com.solidity.enricher.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.com.solidity.db.abstraction.Identifiable;
import ua.com.solidity.db.entities.NotificationJuridicalTagCondition;
import ua.com.solidity.db.entities.NotificationJuridicalTagMatching;
import ua.com.solidity.db.entities.NotificationPhysicalTagCondition;
import ua.com.solidity.db.entities.NotificationPhysicalTagMatching;
import ua.com.solidity.db.entities.User;
import ua.com.solidity.db.entities.YCompany;
import ua.com.solidity.db.entities.YCompanyMonitoringNotification;
import ua.com.solidity.db.entities.YCompanyPackageMonitoringNotification;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.entities.YPersonMonitoringNotification;
import ua.com.solidity.db.entities.YPersonPackageMonitoringNotification;
import ua.com.solidity.db.repositories.NotificationJuridicalTagMatchingRepository;
import ua.com.solidity.db.repositories.NotificationPhysicalTagMatchingRepository;
import ua.com.solidity.db.repositories.UserRepository;
import ua.com.solidity.db.repositories.YCompanyMonitoringNotificationRepository;
import ua.com.solidity.db.repositories.YCompanyPackageMonitoringNotificationRepository;
import ua.com.solidity.db.repositories.YCompanyRepository;
import ua.com.solidity.db.repositories.YPersonMonitoringNotificationRepository;
import ua.com.solidity.db.repositories.YPersonPackageMonitoringNotificationRepository;
import ua.com.solidity.db.repositories.YPersonRepository;

@Service
@RequiredArgsConstructor
public class MonitoringNotificationService {

    private final UserRepository ur;
    private final YPersonMonitoringNotificationRepository personMonitoringNotificationRepository;
    private final YCompanyMonitoringNotificationRepository companyMonitoringNotificationRepository;
    private final YPersonRepository yPersonRepository;
    private final YCompanyRepository yCompanyRepository;
    private final NotificationPhysicalTagMatchingRepository physicalTagMatchingRepository;
    private final NotificationJuridicalTagMatchingRepository juridicalTagMatchingRepository;
    private final YPersonPackageMonitoringNotificationRepository personPackageMonitoringNotificationRepository;
    private final YCompanyPackageMonitoringNotificationRepository companyPackageMonitoringNotificationRepository;

    public void enrichYPersonMonitoringNotification(Set<YPerson> people) {
        Map<UUID, YPerson> personMap = new HashMap<>();
        people.forEach(person -> personMap.put(person.getId(), person));
        List<User> userList = ur.findAll();

        userList.forEach(user -> user.getPersonSubscriptions().forEach(monitoredPerson -> {
            YPerson cachedPerson = personMap.get(monitoredPerson.getId());
            if (cachedPerson != null) {

                StringBuilder personName = new StringBuilder();
                Stream.of(cachedPerson.getLastName(), cachedPerson.getFirstName(), cachedPerson.getPatName())
                        .forEach(name -> {
                            if (name != null) personName.append(" ").append(name);
                        });
                StringBuilder messageBuilder = new StringBuilder("Person" + personName + " got new information:");
                List<String> messagePieces = new ArrayList<>();
                long[] updatesCount = new long[1];

                Consumer<String> messagePiecesFiller = s -> {
                    if (updatesCount[0] > 0) {
                        messagePieces.add(s + updatesCount[0]);
                        updatesCount[0] = 0;
                    }
                };
                Consumer<Identifiable> updatesIncrementer = entity -> {
                    if (entity.getIdentifier() == null) updatesCount[0]++;
                };

                cachedPerson.getInns().forEach(updatesIncrementer);
                messagePiecesFiller.accept("inns - ");

                cachedPerson.getAddresses().forEach(updatesIncrementer);
                messagePiecesFiller.accept("addresses - ");

                cachedPerson.getPassports().forEach(updatesIncrementer);
                messagePiecesFiller.accept("passports - ");

                cachedPerson.getTags().forEach(updatesIncrementer);
                messagePiecesFiller.accept("tags - ");

                cachedPerson.getAltPeople().forEach(updatesIncrementer);
                messagePiecesFiller.accept("alternate persons - ");

                if (messagePieces.size() > 0) {
                    for (int i = 0; i < messagePieces.size(); i++) {
                        messageBuilder.append(" ").append(messagePieces.get(i));
                        messageBuilder.append(i + 1 < messagePieces.size() ? ";" : ".");
                    }
                    YPersonMonitoringNotification notification = new YPersonMonitoringNotification();
                    notification.setYpersonId(cachedPerson.getId());
                    notification.setMessage(messageBuilder.toString());
                    notification.setUser(user);
                    personMonitoringNotificationRepository.save(notification);
                }
            }
        }));
    }

    public void enrichYCompanyMonitoringNotification(Set<YCompany> companies) {
        Map<UUID, YCompany> companyMap = new HashMap<>();
        companies.forEach(company -> companyMap.put(company.getId(), company));
        List<User> userList = ur.findAll();

        userList.forEach(user -> user.getCompanies().forEach(monitoredCompany -> {
            YCompany cachedCompany = companyMap.get(monitoredCompany.getId());
            if (cachedCompany != null) {

                StringBuilder messageBuilder = new StringBuilder("Company" + monitoredCompany.getName() + " got new information:");
                List<String> messagePieces = new ArrayList<>();
                long[] updatesCount = new long[1];

                Consumer<String> messagePiecesFiller = s -> {
                    if (updatesCount[0] > 0) {
                        messagePieces.add(s + updatesCount[0]);
                        updatesCount[0] = 0;
                    }
                };
                Consumer<Identifiable> updatesIncrementer = entity -> {
                    if (entity.getIdentifier() == null) updatesCount[0]++;
                };

                cachedCompany.getAddresses().forEach(updatesIncrementer);
                messagePiecesFiller.accept("addresses - ");

                cachedCompany.getTags().forEach(updatesIncrementer);
                messagePiecesFiller.accept("tags - ");

                cachedCompany.getAltCompanies().forEach(updatesIncrementer);
                messagePiecesFiller.accept("alternate persons - ");

                if (messagePieces.size() > 0) {
                    for (int i = 0; i < messagePieces.size(); i++) {
                        messageBuilder.append(" ").append(messagePieces.get(i));
                        messageBuilder.append(i + 1 < messagePieces.size() ? ";" : ".");
                    }
                    YCompanyMonitoringNotification notification = new YCompanyMonitoringNotification();
                    notification.setYcompanyId(monitoredCompany.getId());
                    notification.setMessage(messageBuilder.toString());
                    notification.setUser(user);
                    companyMonitoringNotificationRepository.save(notification);
                }
            }
        }));
    }

    public void enrichYPersonPackageMonitoringNotification(Set<YPerson> people) {
        List<NotificationPhysicalTagMatching> matchingList = physicalTagMatchingRepository.findAll();

        List<YPerson> peopleGlobalSaved = yPersonRepository.findAllInIds(people.stream()
                                                                                 .map(YPerson::getId)
                                                                                 .collect(Collectors.toList()));
        people.forEach(person -> {
            person.getTags()
                    .forEach(tag -> {
                        if (tag.getUntil() == null) tag.setUntil(LocalDate.of(3500, 1, 1));
                    });
        });

        List<YPerson> peopleLocalNew = new ArrayList<>();
        List<YPerson> peopleLocalSaved = new ArrayList<>();

        people.forEach(person -> {
            boolean contains = peopleGlobalSaved.stream()
                    .anyMatch(personGS -> personGS.getId().equals(person.getId()));
            if (!contains) peopleLocalNew.add(person);
        });

        people.forEach(person -> {
            boolean contains = peopleGlobalSaved.stream()
                    .anyMatch(personGS -> personGS.getId().equals(person.getId()));
            if (contains) peopleLocalSaved.add(person);
        });

        Map<UUID, YPerson> peopleGlobalSavedMap = new HashMap<>();
        peopleGlobalSaved.forEach(person -> peopleGlobalSavedMap.put(person.getId(), person));

        matchingList.forEach(matching -> {
            Set<NotificationPhysicalTagCondition> conditions = matching.getConditions();

            //new people
            peopleLocalNew.forEach(person -> {

                conditions.forEach(condition -> {
                    boolean hasCondition = condition.getTagTypes()
                            .stream()
                            .allMatch(tagType -> person.getTags()
                                    .stream()
                                    .anyMatch(tag -> tag.getTagType().getId().equals(tagType.getId())
                                            && tag.getUntil().isAfter(LocalDateTime.now().toLocalDate())));

                    if (hasCondition) {
                        YPersonPackageMonitoringNotification notification = new YPersonPackageMonitoringNotification();
                        notification.setYpersonId(person.getId());
                        notification.setEmail(matching.getEmail());
                        notification.setCondition(condition);
                        personPackageMonitoringNotificationRepository.save(notification);
                    }
                });

            });

            //existing people
            peopleLocalSaved.forEach(personLocalSaved -> {

                YPerson personGlobalSaved = peopleGlobalSavedMap.get(personLocalSaved.getId());

                conditions.forEach(condition -> {
                    boolean localHasCondition = condition.getTagTypes()
                            .stream()
                            .allMatch(tagType -> personLocalSaved.getTags()
                                    .stream()
                                    .anyMatch(tag -> tag.getTagType().getId().equals(tagType.getId())
                                            && tag.getUntil().isAfter(LocalDateTime.now().toLocalDate())));

                    boolean globalHasCondition = condition.getTagTypes()
                            .stream()
                            .allMatch(tagType -> personGlobalSaved.getTags()
                                    .stream()
                                    .anyMatch(tag -> tag.getTagType().getId().equals(tagType.getId())));

                    if (localHasCondition && !globalHasCondition) {
                        YPersonPackageMonitoringNotification notification = new YPersonPackageMonitoringNotification();
                        notification.setYpersonId(personLocalSaved.getId());
                        notification.setEmail(matching.getEmail());
                        notification.setCondition(condition);
                        personPackageMonitoringNotificationRepository.save(notification);
                    }
                });

            });

        });

    }

    public void enrichYCompanyPackageMonitoringNotification(Set<YCompany> companies) {
        List<NotificationJuridicalTagMatching> matchingList = juridicalTagMatchingRepository.findAll();

        List<YCompany> companiesGlobalSaved = yCompanyRepository.findAllWithTagsInIds(companies.stream()
                                                                                 .map(YCompany::getId)
                                                                                 .collect(Collectors.toList()));
        companies.forEach(company -> {
            company.getTags()
                    .forEach(tag -> {
                        if (tag.getUntil() == null) tag.setUntil(LocalDate.of(3500, 1, 1));
                    });
        });

        List<YCompany> companiesLocalNew = new ArrayList<>();
        List<YCompany> companiesLocalSaved = new ArrayList<>();

        companies.forEach(company -> {
            boolean contains = companiesGlobalSaved.stream()
                    .anyMatch(personGS -> personGS.getId().equals(company.getId()));
            if (!contains) companiesLocalNew.add(company);
        });

        companies.forEach(company -> {
            boolean contains = companiesGlobalSaved.stream()
                    .anyMatch(personGS -> personGS.getId().equals(company.getId()));
            if (contains) companiesLocalSaved.add(company);
        });

        Map<UUID, YCompany> companiesGlobalSavedMap = new HashMap<>();
        companiesGlobalSaved.forEach(person -> companiesGlobalSavedMap.put(person.getId(), person));

        matchingList.forEach(matching -> {
            Set<NotificationJuridicalTagCondition> conditions = matching.getConditions();

            //new people
            companiesLocalNew.forEach(company -> {

                conditions.forEach(condition -> {
                    boolean hasCondition = condition.getTagTypes()
                            .stream()
                            .allMatch(tagType -> company.getTags()
                                    .stream()
                                    .anyMatch(tag -> tag.getTagType().getId().equals(tagType.getId())
                                            && tag.getUntil().isAfter(LocalDateTime.now().toLocalDate())));

                    if (hasCondition) {
                        YCompanyPackageMonitoringNotification notification = new YCompanyPackageMonitoringNotification();
                        notification.setYcompanyId(company.getId());
                        notification.setEmail(matching.getEmail());
                        notification.setCondition(condition);
                        companyPackageMonitoringNotificationRepository.save(notification);
                    }
                });

            });

            //existing people
            companiesLocalSaved.forEach(companyLocalSaved -> {

                YCompany companyGlobalSaved = companiesGlobalSavedMap.get(companyLocalSaved.getId());

                conditions.forEach(condition -> {
                    boolean localHasCondition = condition.getTagTypes()
                            .stream()
                            .allMatch(tagType -> companyLocalSaved.getTags()
                                    .stream()
                                    .anyMatch(tag -> tag.getTagType().getId().equals(tagType.getId())
                                            && tag.getUntil().isAfter(LocalDateTime.now().toLocalDate())));

                    boolean globalHasCondition = condition.getTagTypes()
                            .stream()
                            .allMatch(tagType -> companyGlobalSaved.getTags()
                                    .stream()
                                    .anyMatch(tag -> tag.getTagType().getId().equals(tagType.getId())));

                    if (localHasCondition && !globalHasCondition) {
                        YCompanyPackageMonitoringNotification notification = new YCompanyPackageMonitoringNotification();
                        notification.setYcompanyId(companyLocalSaved.getId());
                        notification.setEmail(matching.getEmail());
                        notification.setCondition(condition);
                        companyPackageMonitoringNotificationRepository.save(notification);
                    }
                });

            });

        });
    }
}
