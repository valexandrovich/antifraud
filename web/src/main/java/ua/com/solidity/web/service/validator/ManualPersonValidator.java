package ua.com.solidity.web.service.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import ua.com.solidity.db.entities.ManualPerson;
import ua.com.solidity.web.response.secondary.ManualPersonStatus;

public class ManualPersonValidator {

    public static List<ManualPersonStatus> manualPersonValidate(List<ManualPerson> people) {
        List<ManualPersonStatus> statusList = new ArrayList<>();

        for (ManualPerson person : people) {
            if (!valid(person.getCnum(), ManualDataRegex.CNUM.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 0, ManualDataRegex.CNUM.getMessage()));
            if (!valid(person.getLnameUk(), ManualDataRegex.NAME_UK.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 1, ManualDataRegex.NAME_UK.getMessage()));
            if (!valid(person.getFnameUk(), ManualDataRegex.NAME_UK.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 2, ManualDataRegex.NAME_UK.getMessage()));
            if (!valid(person.getPnameUk(), ManualDataRegex.NAME_UK.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 3, ManualDataRegex.NAME_UK.getMessage()));
            if (!valid(person.getLnameRu(), ManualDataRegex.NAME_RU.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 4, ManualDataRegex.NAME_RU.getMessage()));
            if (!valid(person.getFnameRu(), ManualDataRegex.NAME_RU.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 5, ManualDataRegex.NAME_RU.getMessage()));
            if (!valid(person.getPnameRu(), ManualDataRegex.NAME_RU.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 6, ManualDataRegex.NAME_RU.getMessage()));
            if (!valid(person.getLnameEn(), ManualDataRegex.NAME_EN.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 7, ManualDataRegex.NAME_EN.getMessage()));
            if (!valid(person.getFnameEn(), ManualDataRegex.NAME_EN.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 8, ManualDataRegex.NAME_EN.getMessage()));
            if (!valid(person.getPnameEn(), ManualDataRegex.NAME_EN.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 9, ManualDataRegex.NAME_EN.getMessage()));
            if (!valid(person.getBirthday(), ManualDataRegex.DATE.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 10, ManualDataRegex.DATE.getMessage()));
            if (!valid(person.getOkpo(), ManualDataRegex.INN.getRegex()) || !isValidInn(person.getOkpo()))
                statusList.add(new ManualPersonStatus(person.getId(), 11, ManualDataRegex.INN.getMessage()));
            if (!valid(person.getCountry(), ManualDataRegex.NAME_EN.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 12, ManualDataRegex.NAME_EN.getMessage()));
            if (!valid(person.getPhone(), ManualDataRegex.PHONE_NUMBER.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 14, ManualDataRegex.PHONE_NUMBER.getMessage()));
            if (!valid(person.getEmail(), ManualDataRegex.EMAIL.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 15, ManualDataRegex.EMAIL.getMessage()));
            if (!valid(person.getBirthPlace(), ManualDataRegex.UK_RU_EN_MULTIPLE.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 16, ManualDataRegex.UK_RU_EN_MULTIPLE.getMessage()));
            if (!valid(person.getSex(), ManualDataRegex.GENDER.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 17, ManualDataRegex.GENDER.getMessage()));
            if (!valid(person.getComment(), ManualDataRegex.UK_RU_EN_MULTIPLE.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 18, ManualDataRegex.UK_RU_EN_MULTIPLE.getMessage()));
            if (!valid(person.getPassLocalNum(), ManualDataRegex.LOCAL_PASSPORT_NUMBER.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 19, ManualDataRegex.LOCAL_PASSPORT_NUMBER.getMessage()));
            if (!valid(person.getPassLocalSerial(), ManualDataRegex.LOCAL_PASSPORT_SERIES.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 20, ManualDataRegex.LOCAL_PASSPORT_SERIES.getMessage()));
            if (!valid(person.getPassLocalIssuer(), ManualDataRegex.UK_RU_MULTIPLE.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 21, ManualDataRegex.UK_RU_MULTIPLE.getMessage()));
            if (!valid(person.getPassLocalIssueDate(), ManualDataRegex.DATE.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 22, ManualDataRegex.DATE.getMessage()));
            if (!valid(person.getPassIntNum(), ManualDataRegex.FOREIGN_PASSPORT_NUMBER.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 23, ManualDataRegex.FOREIGN_PASSPORT_NUMBER.getMessage()));
            if (!valid(person.getPassIntRecNum(), ManualDataRegex.FOREIGN_PASSPORT_RECORD_NUMBER.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 24, ManualDataRegex.FOREIGN_PASSPORT_RECORD_NUMBER.getMessage()));
            if (!valid(person.getPassIntIssuer(), ManualDataRegex.FOREIGN_PASSPORT_AUTHORITY.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 25, ManualDataRegex.FOREIGN_PASSPORT_AUTHORITY.getMessage()));
            if (!valid(person.getPassIntIssueDate(), ManualDataRegex.DATE.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 26, ManualDataRegex.DATE.getMessage()));
            if (!valid(person.getPassIdNum(), ManualDataRegex.ID_PASSPORT_NUMBER.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 27, ManualDataRegex.ID_PASSPORT_NUMBER.getMessage()));
            if (!valid(person.getPassIdRecNum(), ManualDataRegex.ID_PASSPORT_RECORD_NUMBER.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 28, ManualDataRegex.ID_PASSPORT_RECORD_NUMBER.getMessage()));
            if (!valid(person.getPassIdIssuer(), ManualDataRegex.ID_PASSPORT_AUTHORITY.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 29, ManualDataRegex.ID_PASSPORT_AUTHORITY.getMessage()));
            if (!valid(person.getPassIdIssueDate(), ManualDataRegex.DATE.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 30, ManualDataRegex.DATE.getMessage()));
            if (StringUtils.isBlank(person.getOkpo())
                    && StringUtils.isBlank(person.getLnameUk())
                    && StringUtils.isBlank(person.getPassIdNum())
                    && StringUtils.isBlank(person.getPassIdRecNum())
                    && StringUtils.isBlank(person.getPassIntNum())
                    && StringUtils.isBlank(person.getPassIntRecNum())
                    && StringUtils.isBlank(person.getPassLocalNum())
                    && StringUtils.isBlank(person.getPassLocalSerial()))
                statusList.add(new ManualPersonStatus(person.getId(), 0, "Одне з полів має бути заповненим: прізвище, ІПН, паспорт"));
        }
        return statusList;
    }

    private static boolean valid(String value, String regex) {
        if (value == null || regex == null) return true;
        return value.matches(regex);
    }

    private static boolean isValidInn(String inn) {
        int controlNumber = ((-1 * Integer.parseInt(String.valueOf(inn.charAt(0)))
                + 5 * Integer.parseInt(String.valueOf(inn.charAt(1)))
                + 7 * Integer.parseInt(String.valueOf(inn.charAt(2)))
                + 9 * Integer.parseInt(String.valueOf(inn.charAt(3)))
                + 4 * Integer.parseInt(String.valueOf(inn.charAt(4)))
                + 6 * Integer.parseInt(String.valueOf(inn.charAt(5)))
                + 10 * Integer.parseInt(String.valueOf(inn.charAt(6)))
                + 5 * Integer.parseInt(String.valueOf(inn.charAt(7)))
                + 7 * Integer.parseInt(String.valueOf(inn.charAt(8)))) % 11) % 10;
        return Objects.equals(Integer.parseInt(String.valueOf(inn.charAt(9))), controlNumber);
    }
}
