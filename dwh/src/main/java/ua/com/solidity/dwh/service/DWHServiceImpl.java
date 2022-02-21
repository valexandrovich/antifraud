package ua.com.solidity.dwh.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ua.com.solidity.dwh.entities.ArContragent;
import ua.com.solidity.dwh.repositorydwh.ArContragentRepository;
import ua.com.solidity.dwh.entities.Contragent;
import ua.com.solidity.dwh.repository.ContragentRepository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class DWHServiceImpl implements DWHService {
    private final ArContragentRepository acr;
    private final ContragentRepository cr;

    @Value("${otp.dwh.page-size}")
    private Integer pageSize;

    @Override
    public void update(Timestamp timestamp) { // Find records updated as of specified arcdate and import them from DWH
        int[] counter = new int[1];
        LocalDate date = timestamp.toLocalDateTime().toLocalDate();
        log.info("Importing from DWH records archived after: {}", timestamp);

        Pageable pageRequest = PageRequest.of(0, pageSize);
        Page<ArContragent> onePage = acr.findByArcDateAfter(date, pageRequest);

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
                contragentEntityList.add(c);
                counter[0]++;
            });
            cr.saveAll(contragentEntityList);
            onePage = acr.findByArcDateAfter(date, pageRequest);
        }

        log.info("Imported {} records from DWH", counter[0]);
    }
}
