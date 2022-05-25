package ua.com.solidity.web.service.validator;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import ua.com.solidity.db.entities.ManualPerson;
import ua.com.solidity.web.response.secondary.ManualPersonStatus;

public class ManualPersonValidator {

    public static List<ManualPersonStatus> manualPersonValidate(List<ManualPerson> people) {
        List<ManualPersonStatus> statusList = new ArrayList<>();

        for (ManualPerson person : people) {
            if (!valid(person.getCnum(), PhysicalPersonRegex.CNUM.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 0, PhysicalPersonRegex.CNUM.getMessage()));
            if (!valid(person.getLnameUk(), PhysicalPersonRegex.NAME_UK.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(),1, PhysicalPersonRegex.NAME_UK.getMessage()));
            if (!valid(person.getFnameUk(), PhysicalPersonRegex.NAME_UK.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(),2, PhysicalPersonRegex.NAME_UK.getMessage()));
            if (!valid(person.getPnameUk(), PhysicalPersonRegex.NAME_UK.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(),3, PhysicalPersonRegex.NAME_UK.getMessage()));
            if (!valid(person.getLnameRu(), PhysicalPersonRegex.NAME_RU.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(),4, PhysicalPersonRegex.NAME_RU.getMessage()));
            if (!valid(person.getFnameRu(), PhysicalPersonRegex.NAME_RU.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(),5, PhysicalPersonRegex.NAME_RU.getMessage()));
            if (!valid(person.getPnameRu(), PhysicalPersonRegex.NAME_RU.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(),6, PhysicalPersonRegex.NAME_RU.getMessage()));
            if (!valid(person.getLnameEn(), PhysicalPersonRegex.NAME_EN.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(),7, PhysicalPersonRegex.NAME_EN.getMessage()));
            if (!valid(person.getFnameEn(), PhysicalPersonRegex.NAME_EN.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(),8, PhysicalPersonRegex.NAME_EN.getMessage()));
            if (!valid(person.getPnameEn(), PhysicalPersonRegex.NAME_EN.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(),9, PhysicalPersonRegex.NAME_EN.getMessage()));
            if (!valid(person.getBirthday(), PhysicalPersonRegex.DATE.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(),10, PhysicalPersonRegex.DATE.getMessage()));
            if (!valid(person.getOkpo(), PhysicalPersonRegex.INN.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(),11, PhysicalPersonRegex.INN.getMessage()));
            if (!valid(person.getCountry(), PhysicalPersonRegex.NAME_EN.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(),12, PhysicalPersonRegex.NAME_EN.getMessage()));
            if (!valid(person.getPhone(), PhysicalPersonRegex.PHONE_NUMBER.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(),14, PhysicalPersonRegex.PHONE_NUMBER.getMessage()));
            if (!valid(person.getEmail(), PhysicalPersonRegex.EMAIL.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(),15, PhysicalPersonRegex.EMAIL.getMessage()));
            if (!valid(person.getBirthPlace(), PhysicalPersonRegex.UK_RU_EN_MULTIPLE.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(),16, PhysicalPersonRegex.UK_RU_EN_MULTIPLE.getMessage()));
            if (!valid(person.getSex(), PhysicalPersonRegex.GENDER.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(),17, PhysicalPersonRegex.GENDER.getMessage()));
            if (!valid(person.getComment(), PhysicalPersonRegex.UK_RU_EN_MULTIPLE.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(),18, PhysicalPersonRegex.UK_RU_EN_MULTIPLE.getMessage()));
            if (!valid(person.getPassLocalNum(), PhysicalPersonRegex.LOCAL_PASSPORT_NUMBER.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(),19, PhysicalPersonRegex.LOCAL_PASSPORT_NUMBER.getMessage()));
            if (!valid(person.getPassLocalSerial(), PhysicalPersonRegex.LOCAL_PASSPORT_SERIES.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(),20, PhysicalPersonRegex.LOCAL_PASSPORT_SERIES.getMessage()));
            if (!valid(person.getPassLocalIssuer(), PhysicalPersonRegex.UK_RU_MULTIPLE.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(),21, PhysicalPersonRegex.UK_RU_MULTIPLE.getMessage()));
            if (!valid(person.getPassLocalIssueDate(), PhysicalPersonRegex.DATE.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(),22, PhysicalPersonRegex.DATE.getMessage()));
            if (!valid(person.getPassIntNum(), PhysicalPersonRegex.FOREIGN_PASSPORT_NUMBER.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(),23, PhysicalPersonRegex.FOREIGN_PASSPORT_NUMBER.getMessage()));
            if (!valid(person.getPassIntRecNum(), PhysicalPersonRegex.FOREIGN_PASSPORT_RECORD_NUMBER.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(),24, PhysicalPersonRegex.FOREIGN_PASSPORT_RECORD_NUMBER.getMessage()));
            if (!valid(person.getPassIntIssuer(), PhysicalPersonRegex.FOREIGN_PASSPORT_AUTHORITY.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(),25, PhysicalPersonRegex.FOREIGN_PASSPORT_AUTHORITY.getMessage()));
            if (!valid(person.getPassIntIssueDate(), PhysicalPersonRegex.DATE.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(),26, PhysicalPersonRegex.DATE.getMessage()));
            if (!valid(person.getPassIdNum(), PhysicalPersonRegex.ID_PASSPORT_NUMBER.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(),27, PhysicalPersonRegex.ID_PASSPORT_NUMBER.getMessage()));
            if (!valid(person.getPassIdRecNum(), PhysicalPersonRegex.ID_PASSPORT_RECORD_NUMBER.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(),28, PhysicalPersonRegex.ID_PASSPORT_RECORD_NUMBER.getMessage()));
            if (!valid(person.getPassIdIssuer(), PhysicalPersonRegex.ID_PASSPORT_AUTHORITY.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(),29, PhysicalPersonRegex.ID_PASSPORT_AUTHORITY.getMessage()));
            if (!valid(person.getPassIdIssueDate(), PhysicalPersonRegex.DATE.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(),30, PhysicalPersonRegex.DATE.getMessage()));
            if (StringUtils.isBlank(person.getOkpo())
                    && StringUtils.isBlank(person.getLnameUk())
                    && StringUtils.isBlank(person.getPassIdNum())
                    && StringUtils.isBlank(person.getPassIdRecNum())
                    && StringUtils.isBlank(person.getPassIntNum())
                    && StringUtils.isBlank(person.getPassIntRecNum())
                    && StringUtils.isBlank(person.getPassLocalNum())
                    && StringUtils.isBlank(person.getPassLocalSerial()))
                statusList.add(new ManualPersonStatus(person.getId(),0, "Одне з полів має бути заповненим: прізвище, ІПН, паспорт"));
        }
        return statusList;
    }

    private static boolean valid(String value, String regex) {
        if (value == null || regex == null) return true;
        return value.matches(regex);
    }
}
