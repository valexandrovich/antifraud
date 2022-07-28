package ua.com.solidity.enricher.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.com.solidity.db.abstraction.Identifiable;
import ua.com.solidity.db.entities.User;
import ua.com.solidity.db.entities.YCompany;
import ua.com.solidity.db.entities.YCompanyMonitoringNotification;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.entities.YPersonMonitoringNotification;
import ua.com.solidity.db.repositories.UserRepository;
import ua.com.solidity.db.repositories.YCompanyMonitoringNotificationRepository;
import ua.com.solidity.db.repositories.YPersonMonitoringNotificationRepository;

@Service
@RequiredArgsConstructor
public class MonitoringNotificationService {

	private final UserRepository ur;
	private final YPersonMonitoringNotificationRepository ypmnr;
	private final YCompanyMonitoringNotificationRepository ycmnr;

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
					ypmnr.save(notification);
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
					ycmnr.save(notification);
				}
			}
		}));
	}
}
