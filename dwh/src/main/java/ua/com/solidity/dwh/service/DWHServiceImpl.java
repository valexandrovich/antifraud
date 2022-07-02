package ua.com.solidity.dwh.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ua.com.solidity.common.Utils;
import ua.com.solidity.common.model.EnricherPortionMessage;
import ua.com.solidity.db.entities.Contragent;
import ua.com.solidity.db.entities.ImportRevision;
import ua.com.solidity.db.entities.StatusLogger;
import ua.com.solidity.db.repositories.ContragentRepository;
import ua.com.solidity.db.repositories.ImportRevisionRepository;
import ua.com.solidity.db.repositories.SchedulerEntityRepository;
import ua.com.solidity.dwh.entities.ArContragent;
import ua.com.solidity.dwh.model.UpdateDWHRequest;
import ua.com.solidity.dwh.repositorydwh.ArContragentRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@CustomLog
@RequiredArgsConstructor
@Service
@PropertySource({"classpath:dwh.properties", "classpath:application.properties"})
public class DWHServiceImpl implements DWHService {
	private static final String CONTRAGENT = "contragent";
	private final ArContragentRepository acr;
	private final ContragentRepository cr;
	private final ImportRevisionRepository irr;
	private final SchedulerEntityRepository ser;

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
	private final AmqpTemplate template;

	private final ObjectMapper objectMapper;

	private String importedRecords(long num, LocalDate date) {
		return String.format("Imported %d records archived after %s", num, Timestamp.valueOf(date.atStartOfDay()));
	}

	@Override
	public void update(UpdateDWHRequest updateDWHRequest) { // Find records updated as of specified arcdate and import them from DWH
		long[] counter = new long[1];
		LocalDateTime startTime = LocalDateTime.now();
		LocalDate date;

		UUID revision = UUID.randomUUID();

		Timestamp lastModified = updateDWHRequest != null
				? Optional.of(updateDWHRequest.getLastModified()).orElseGet(() -> null)
				: null;

		ImportRevision importRevision = irr.findFirstBySource(SOURCE);
		Instant instant = importRevision != null
				? Optional.of(importRevision.getRevisionDate()).orElseGet(() -> null)
				: null;

		if (lastModified != null) {
			if (lastModified.getTime() >= 0L) {
				date = lastModified.toLocalDateTime().toLocalDate();
			} else {
				if (instant != null) {
					date = Timestamp.from(instant).toLocalDateTime().toLocalDate();
				} else {
					date = new Timestamp(0L).toLocalDateTime().toLocalDate();
				}
			}
		} else {
			if (instant != null) {
				date = Timestamp.from(instant).toLocalDateTime().toLocalDate();
			} else {
				date = new Timestamp(0L).toLocalDateTime().toLocalDate();
			}

		}

		log.info("Importing from DWH records archived after: {}", Timestamp.valueOf(date.atStartOfDay()));

		StatusLogger statusLogger = new StatusLogger(revision, 0L, "%",
		                                             AR_CONTRAGENT, DWH, startTime, null, null);
		template.convertAndSend(loggerQueue, Utils.objectToJsonString(statusLogger));

		Pageable pageRequest = PageRequest.of(0, pageSize);
		Page<ArContragent> onePage = acr.findByArContragentIDArcDateGreaterThanEqual(date, pageRequest);


		while (!onePage.isEmpty()) {
			UUID portion = UUID.randomUUID(); // Portion
			pageRequest = pageRequest.next();
			List<Contragent> contragentEntityList = new ArrayList<>();

			onePage.forEach(r -> {
				log.debug(r.toString());

				Contragent c = new Contragent();

				
c.setId(r.getArContragentID().getId());
c.setName(handleString(r.getName()));
c.setContragentTypeId(handleString(r.getContragentTypeId()));
c.setInsiderId(r.getInsiderId());
c.setCountryId(r.getCountryId());
c.setOwnershipTypeId(r.getOwnershipTypeId());
c.setIdentifyCode(handleString(r.getIdentifyCode()));
c.setAddress(handleString(r.getAddress()));
c.setBusinessType1(r.getBusinessType1());
c.setBusinessType2(r.getBusinessType2());
c.setBusinessType3(r.getBusinessType3());
c.setBusinessType4(r.getBusinessType4());
c.setBusinessType5(r.getBusinessType5());
c.setContragentStateId(r.getContragentStateId());
c.setAlternateName(handleString(r.getAlternateName()));
c.setRegisterDate(r.getRegisterDate());
c.setNalogRegisterDate(r.getNalogRegisterDate());
c.setJuridicalAddress(handleString(r.getJuridicalAddress()));
c.setStateRegisterNo(handleString(r.getStateRegisterNo()));
c.setStateRegisterDate(r.getStateRegisterDate());
c.setStateRegisterPlace(handleString(r.getStateRegisterPlace()));
c.setAddrCountryId(r.getAddrCountryId());
c.setAddrPostCode(handleString(r.getAddrPostCode()));
c.setAddrRegion(handleString(r.getAddrRegion()));
c.setAddrDistrict(handleString(r.getAddrDistrict()));
c.setAddrCity(handleString(r.getAddrCity()));
c.setAddrStreet(handleString(r.getAddrStreet()));
c.setAddrHouseNo(handleString(r.getAddrHouseNo()));
c.setAddrFlat(handleString(r.getAddrFlat()));
c.setJurAddrCountryId(r.getJurAddrCountryId());
c.setJurAddrRegion(handleString(r.getJurAddrRegion()));
c.setJurAddrDistrict(handleString(r.getJurAddrDistrict()));
c.setJurAddrCity(handleString(r.getJurAddrCity()));
c.setJurAddrStreet(handleString(r.getJurAddrStreet()));
c.setJurAddrHouseNo(handleString(r.getJurAddrHouseNo()));
c.setJurAddrFlat(handleString(r.getJurAddrFlat()));
c.setCloseDate(r.getCloseDate());
c.setLastModified(r.getLastModified());
c.setPassportType(r.getPassportType());
c.setPassportIssueDate(r.getPassportIssueDate());
c.setPassportIssuePlace(handleString(r.getPassportIssuePlace()));
c.setClientName(handleString(r.getClientName()));
c.setClientPatronymicName(handleString(r.getClientPatronymicName()));
c.setClientLastName(handleString(r.getClientLastName()));
c.setClientBirthday(r.getClientBirthday());
c.setBirthplace(handleString(r.getBirthplace()));
c.setGender(handleString(r.getGender()));
c.setPhones(handleString(r.getPhones()));
c.setMobilePhone(handleString(r.getMobilePhone()));
c.setEmail(handleString(r.getEmail()));
c.setBadStatusFlag(r.getBadStatusFlag());
c.setPassportSerial(handleString(r.getPassportSerial()));
c.setPassportNo(handleString(r.getPassportNo()));
c.setPhoneHome(handleString(r.getPhoneHome()));
c.setFamilyStatusId(r.getFamilyStatusId());
c.setCitizenshipCountryId(r.getCitizenshipCountryId());
c.setWorkplace(handleString(r.getWorkplace()));
c.setIsPublicPerson(r.getIsPublicPerson());
c.setWorkPosition(handleString(r.getWorkPosition()));
c.setPassportEndDate(r.getPassportEndDate());
c.setFop(r.getFop());
c.setArcDate(r.getArContragentID().getArcDate());
c.setUuid(UUID.randomUUID());
c.setPortionId(portion);
c.setRevision(revision);
contragentEntityList.add(c);
counter[0]++;

			});
			cr.saveAll(contragentEntityList);
			onePage = acr.findByArContragentIDArcDateGreaterThanEqual(date, pageRequest);

			statusLogger = new StatusLogger(revision, counter[0], RECORDS,
			                                AR_CONTRAGENT, DWH, startTime, null, null);
			template.convertAndSend(loggerQueue, Utils.objectToJsonString(statusLogger));

			log.debug("Sending task to otp-etl.enricher");

			EnricherPortionMessage enricherMessage = new EnricherPortionMessage(CONTRAGENT, portion);
			template.convertAndSend(enricherQueue, Utils.objectToJsonString(enricherMessage));
		}

		Instant newInstant = Timestamp.valueOf(startTime).toInstant();

		ImportRevision newImportRevision = new ImportRevision();
		newImportRevision.setId(revision);
		newImportRevision.setSource(SOURCE);
		newImportRevision.setRevisionDate(newInstant);
		irr.save(newImportRevision);

		log.info("Imported {} records from DWH", counter[0]);

		statusLogger = new StatusLogger(revision, 100L, "%",
		                                AR_CONTRAGENT, DWH, startTime, LocalDateTime.now(),
		                                importedRecords(counter[0], date));
		template.convertAndSend(loggerQueue, Utils.objectToJsonString(statusLogger));
	}

private String handleString(String string){
	// log.debug('')
   return string == null ? "" : string.replaceAll("\u0000", "");
}


	
}
