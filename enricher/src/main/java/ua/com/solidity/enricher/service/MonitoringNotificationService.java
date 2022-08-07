package ua.com.solidity.enricher.service;

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
import ua.com.solidity.db.entities.NotificationPhysicalTagCondition;
import ua.com.solidity.db.entities.NotificationPhysicalTagMatching;
import ua.com.solidity.db.entities.TagType;
import ua.com.solidity.db.entities.User;
import ua.com.solidity.db.entities.YCompany;
import ua.com.solidity.db.entities.YCompanyMonitoringNotification;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.entities.YPersonMonitoringNotification;
import ua.com.solidity.db.entities.YPersonPackageMonitoringNotification;
import ua.com.solidity.db.repositories.NotificationPhysicalTagMatchingRepository;
import ua.com.solidity.db.repositories.UserRepository;
import ua.com.solidity.db.repositories.YCompanyMonitoringNotificationRepository;
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
    private final NotificationPhysicalTagMatchingRepository physicalTagMatchingRepository;
    private final YPersonPackageMonitoringNotificationRepository personPackageMonitoringNotificationRepository;

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

                StringBuilder personName = new StringBuilder();
                Stream.of(person.getLastName(), person.getFirstName(), person.getPatName())
                        .forEach(name -> {
                            if (name != null) personName.append(" ").append(name);
                        });
                StringBuilder messageBuilder = new StringBuilder(personName + " got conditions ");

                var conditionFound = new Object() {
                    boolean state = false;
                };

                List<List<String>> gotConditionsCodesList = new ArrayList<>();

                conditions.forEach(condition -> {
                    boolean hasCondition = condition.getTagTypes()
                            .stream()
                            .allMatch(tagType -> person.getTags()
                                        .stream()
                                        .anyMatch(tag -> tag.getTagType().getId().equals(tagType.getId())));

                    if (hasCondition) {
                        conditionFound.state = true;
                        gotConditionsCodesList.add(condition.getTagTypes()
                                                .stream()
                                                .map(TagType::getCode)
                                                .collect(Collectors.toList()));
                    }
                });

                if (conditionFound.state) {
                    String conditionsCodesString = gotConditionsCodesList.toString();
                    messageBuilder.append(conditionsCodesString, 1, conditionsCodesString.length() - 1);
                    YPersonPackageMonitoringNotification notification = new YPersonPackageMonitoringNotification();
                    notification.setYpersonId(person.getId());
                    notification.setMessage(messageBuilder.toString());
                    notification.setEmail(matching.getEmail());
                    personPackageMonitoringNotificationRepository.save(notification);
                }
            });

            //existing people
            peopleLocalSaved.forEach(personLocal -> {

                YPerson personGlobal = peopleGlobalSavedMap.get(personLocal.getId());
                StringBuilder personName = new StringBuilder();
                Stream.of(personLocal.getLastName(), personLocal.getFirstName(), personLocal.getPatName())
                        .forEach(name -> {
                            if (name != null) personName.append(" ").append(name);
                        });
                StringBuilder messageBuilder = new StringBuilder(personName + " got conditions ");

                var conditionFound = new Object() {
                    boolean state = false;
                };

                List<List<String>> gotConditionsCodesList = new ArrayList<>();

                conditions.forEach(condition -> {
                    boolean localHasCondition = condition.getTagTypes()
                            .stream()
                            .allMatch(tagType -> personLocal.getTags()
                                    .stream()
                                    .anyMatch(tag -> tag.getTagType().getId().equals(tagType.getId())));

                    boolean globalHasCondition = condition.getTagTypes()
                            .stream()
                            .allMatch(tagType -> personGlobal.getTags()
                                    .stream()
                                    .anyMatch(tag -> tag.getTagType().getId().equals(tagType.getId())));

                    if (localHasCondition && !globalHasCondition) {
                        conditionFound.state = true;
                        gotConditionsCodesList.add(condition.getTagTypes()
                                                        .stream()
                                                        .map(TagType::getCode)
                                                        .collect(Collectors.toList()));
                    }
                });

                if (conditionFound.state) {
                    String conditionsCodesString = gotConditionsCodesList.toString();
                    messageBuilder.append(conditionsCodesString, 1, conditionsCodesString.length() - 1);
                    YPersonPackageMonitoringNotification notification = new YPersonPackageMonitoringNotification();
                    notification.setYpersonId(personLocal.getId());
                    notification.setMessage(messageBuilder.toString());
                    notification.setEmail(matching.getEmail());
                    personPackageMonitoringNotificationRepository.save(notification);
                }

            });

        });

    }
}
