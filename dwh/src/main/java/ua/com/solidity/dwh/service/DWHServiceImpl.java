package ua.com.solidity.dwh.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ua.com.solidity.common.Utils;
import ua.com.solidity.common.model.EnricherMessage;
import ua.com.solidity.db.entities.Contragent;
import ua.com.solidity.db.entities.ImportRevision;
import ua.com.solidity.db.entities.StatusLogger;
import ua.com.solidity.db.repositories.ContragentRepository;
import ua.com.solidity.db.repositories.ImportRevisionRepository;
import ua.com.solidity.dwh.entities.ArContragent;
import ua.com.solidity.dwh.repositorydwh.ArContragentRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class DWHServiceImpl implements DWHService {
	private static final String CONTRAGENT = "contragent";
	private final ArContragentRepository acr;
	private final ContragentRepository cr;
	private final ImportRevisionRepository irr;

	private static final String DWH = "DWH";
	private static final String AR_CONTRAGENT = "AR_CONTRAGENT";
	private static final String RECORDS = "records";
	private static final Long SOURCE = 9000L;

	@Value("${enricher.rabbitmq.name}")
	private String enricherQueue;
	@Value("${statuslogger.rabbitmq.name}")
	private String loggerQueue;
	@Value("${otp.dwh.page-size}")
	private Integer pageSize;

	private String importedRecords(long num, LocalDate date) {
		return String.format("Imported %d records archived after %s", num, Timestamp.valueOf(date.atStartOfDay()));
	}

	@Override
	public void update() { // Find records updated as of specified arcdate and import them from DWH
		long[] counter = new long[1];
		LocalDateTime startTime = LocalDateTime.now();
		Instant instant;
		LocalDate date;
		UUID revision = UUID.randomUUID();

		ImportRevision importRevision = irr.findFirstBySource(SOURCE);
		if (importRevision != null && (instant = importRevision.getRevisionDate()) != null) {
			date = Timestamp.from(instant).toLocalDateTime().toLocalDate();
		} else {
			date = new Timestamp(0L).toLocalDateTime().toLocalDate();
		}
		log.info("Importing from DWH records archived after: {}", Timestamp.valueOf(date.atStartOfDay()));

		StatusLogger statusLogger = new StatusLogger(revision, 0L, "%",
		                                             AR_CONTRAGENT, DWH, startTime, null, null);
		Utils.sendRabbitMQMessage(loggerQueue, Utils.objectToJsonString(statusLogger));

		Pageable pageRequest = PageRequest.of(0, pageSize);
		Page<ArContragent> onePage = acr.findByArcDateGreaterThanEqual(date, pageRequest);


		while (!onePage.isEmpty()) {
			pageRequest = pageRequest.next();
			List<Contragent> contragentEntityList = new ArrayList<>();

			onePage.forEach(r -> {
				log.debug(r.toString());

				Contragent c = new Contragent();

				c.setId(r.getId());
				c.setName(r.getName());
				c.setContragentTypeId(r.getContragentTypeId());
				c.setInsiderId(r.getInsiderId());
				c.setCountryId(r.getCountryId());
				c.setOwnershipTypeId(r.getOwnershipTypeId());
				c.setIdentifyCode(r.getIdentifyCode());
				c.setAddress(r.getAddress());
				c.setBusinessType1(r.getBusinessType1());
				c.setBusinessType2(r.getBusinessType2());
				c.setBusinessType3(r.getBusinessType3());
				c.setBusinessType4(r.getBusinessType4());
				c.setBusinessType5(r.getBusinessType5());
				c.setContragentStateId(r.getContragentStateId());
				c.setAlternateName(r.getAlternateName());
				c.setRegisterDate(r.getRegisterDate());
				c.setNalogRegisterDate(r.getNalogRegisterDate());
				c.setJuridicalAddress(r.getJuridicalAddress());
				c.setStateRegisterNo(r.getStateRegisterNo());
				c.setStateRegisterDate(r.getStateRegisterDate());
				c.setStateRegisterPlace(r.getStateRegisterPlace());
				c.setAddrCountryId(r.getAddrCountryId());
				c.setAddrPostCode(r.getAddrPostCode());
				c.setAddrRegion(r.getAddrRegion());
				c.setAddrDistrict(r.getAddrDistrict());
				c.setAddrCity(r.getAddrCity());
				c.setAddrStreet(r.getAddrStreet());
				c.setAddrHouseNo(r.getAddrHouseNo());
				c.setAddrFlat(r.getAddrFlat());
				c.setJurAddrCountryId(r.getJurAddrCountryId());
				c.setJurAddrRegion(r.getJurAddrRegion());
				c.setJurAddrDistrict(r.getJurAddrDistrict());
				c.setJurAddrCity(r.getJurAddrCity());
				c.setJurAddrStreet(r.getJurAddrStreet());
				c.setJurAddrHouseNo(r.getJurAddrHouseNo());
				c.setJurAddrFlat(r.getJurAddrFlat());
				c.setCloseDate(r.getCloseDate());
				c.setLastModified(r.getLastModified());
				c.setPassportType(r.getPassportType());
				c.setPassportIssueDate(r.getPassportIssueDate());
				c.setPassportIssuePlace(r.getPassportIssuePlace());
				c.setClientName(r.getClientName());
				c.setClientPatronymicName(r.getClientPatronymicName());
				c.setClientLastName(r.getClientLastName());
				c.setClientBirthday(r.getClientBirthday());
				c.setBirthplace(r.getBirthplace());
				c.setGender(r.getGender());
				c.setPhones(r.getPhones());
				c.setMobilePhone(r.getMobilePhone());
				c.setEmail(r.getEmail());
				c.setBadStatusFlag(r.getBadStatusFlag());
				c.setPassportSerial(r.getPassportSerial());
				c.setPassportNo(r.getPassportNo());
				c.setPhoneHome(r.getPhoneHome());
				c.setFamilyStatusId(r.getFamilyStatusId());
				c.setCitizenshipCountryId(r.getCitizenshipCountryId());
				c.setWorkplace(r.getWorkplace());
				c.setIsPublicPerson(r.getIsPublicPerson());
				c.setWorkPosition(r.getWorkPosition());
				c.setPassportEndDate(r.getPassportEndDate());
				c.setFop(r.getFop());
				c.setArcDate(r.getArcDate());
				c.setUuid(UUID.randomUUID());
				c.setRevision(revision);
				contragentEntityList.add(c);
				counter[0]++;
			});
			cr.saveAll(contragentEntityList);
			onePage = acr.findByArcDateAfter(date, pageRequest);

			statusLogger = new StatusLogger(revision, counter[0], RECORDS,
			                                AR_CONTRAGENT, DWH, startTime, null, null);
			Utils.sendRabbitMQMessage(loggerQueue, Utils.objectToJsonString(statusLogger));
		}

		Instant newInstant = Timestamp.valueOf(startTime).toInstant();

		ImportRevision newImportRevision = new ImportRevision();
		newImportRevision.setId(revision);
		newImportRevision.setSource(SOURCE);
		newImportRevision.setRevisionDate(newInstant);
		irr.save(newImportRevision);

		log.info("Imported {} records from DWH", counter[0]);
		log.info("Sending task to otp-etl.enricher");

		statusLogger = new StatusLogger(revision, 100L, "%",
		                                AR_CONTRAGENT, DWH, startTime, LocalDateTime.now(),
		                                importedRecords(counter[0], date));
		Utils.sendRabbitMQMessage(loggerQueue, Utils.objectToJsonString(statusLogger));

		EnricherMessage enricherMessage = new EnricherMessage(CONTRAGENT, revision);
		Utils.sendRabbitMQMessage(enricherQueue, Utils.objectToJsonString(enricherMessage));
	}
}
