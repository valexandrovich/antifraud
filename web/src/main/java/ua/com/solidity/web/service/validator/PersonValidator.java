package ua.com.solidity.web.service.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import ua.com.solidity.db.entities.ManualPerson;
import ua.com.solidity.web.response.secondary.ManualPersonStatus;

public class PersonValidator {
    private static final String MESSAGE_LONG_VALUE = "Довжина поля перевищує 255 символів";
    private static final String MESSAGE_EMPTY_DATA = "Одне з полів має бути заповненим: прізвище, ІПН, паспорт";

    public static List<ManualPersonStatus> manualPersonValidate(List<ManualPerson> people) {
        List<ManualPersonStatus> statusList = new ArrayList<>();

        for (ManualPerson person : people) {
            if (!valid(person.getCnum(), DataRegex.CNUM.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 0, DataRegex.CNUM.getMessage()));
            if (StringUtils.isNotBlank(person.getLnameUk()) && person.getLnameUk().length() == 255
            && person.getLnameUk().contains("..."))
                statusList.add(new ManualPersonStatus(person.getId(), 1, MESSAGE_LONG_VALUE));
            if (!valid(person.getLnameUk(), DataRegex.NAME_UK.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 1, DataRegex.NAME_UK.getMessage()));
            if (StringUtils.isNotBlank(person.getFnameUk()) && person.getFnameUk().length() == 255
                    && person.getLnameUk().contains("..."))
                statusList.add(new ManualPersonStatus(person.getId(), 2, MESSAGE_LONG_VALUE));
            if (!valid(person.getFnameUk(), DataRegex.NAME_UK.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 2, DataRegex.NAME_UK.getMessage()));
            if (StringUtils.isNotBlank(person.getPnameUk()) && person.getPnameUk().length() == 255
                    && person.getLnameUk().contains("..."))
                statusList.add(new ManualPersonStatus(person.getId(), 3, MESSAGE_LONG_VALUE));
            if (!valid(person.getPnameUk(), DataRegex.NAME_UK.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 3, DataRegex.NAME_UK.getMessage()));
            if (StringUtils.isNotBlank(person.getLnameRu()) && person.getLnameRu().length() == 255
                    && person.getLnameUk().contains("..."))
                statusList.add(new ManualPersonStatus(person.getId(), 4, MESSAGE_LONG_VALUE));
            if (!valid(person.getLnameRu(), DataRegex.NAME_RU.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 4, DataRegex.NAME_RU.getMessage()));
            if (StringUtils.isNotBlank(person.getFnameRu()) && person.getFnameRu().length() == 255
                    && person.getLnameUk().contains("..."))
                statusList.add(new ManualPersonStatus(person.getId(), 5, MESSAGE_LONG_VALUE));
            if (!valid(person.getFnameRu(), DataRegex.NAME_RU.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 5, DataRegex.NAME_RU.getMessage()));
            if (StringUtils.isNotBlank(person.getPnameRu()) && person.getFnameRu().length() == 255
                    && person.getLnameUk().contains("..."))
                statusList.add(new ManualPersonStatus(person.getId(), 6, MESSAGE_LONG_VALUE));
            if (!valid(person.getPnameRu(), DataRegex.NAME_RU.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 6, DataRegex.NAME_RU.getMessage()));
            if (StringUtils.isNotBlank(person.getLnameEn()) && person.getLnameEn().length() == 255
                    && person.getLnameUk().contains("..."))
                statusList.add(new ManualPersonStatus(person.getId(), 7, MESSAGE_LONG_VALUE));
            if (!valid(person.getLnameEn(), DataRegex.NAME_EN.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 7, DataRegex.NAME_EN.getMessage()));
            if (StringUtils.isNotBlank(person.getFnameEn()) && person.getFnameEn().length() == 255
                    && person.getLnameUk().contains("..."))
                statusList.add(new ManualPersonStatus(person.getId(), 8, MESSAGE_LONG_VALUE));
            if (!valid(person.getFnameEn(), DataRegex.NAME_EN.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 8, DataRegex.NAME_EN.getMessage()));
            if (StringUtils.isNotBlank(person.getPnameEn()) && person.getPnameEn().length() == 255
                    && person.getLnameUk().contains("..."))
                statusList.add(new ManualPersonStatus(person.getId(), 9, MESSAGE_LONG_VALUE));
            if (!valid(person.getPnameEn(), DataRegex.NAME_EN.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 9, DataRegex.NAME_EN.getMessage()));
            if (!valid(person.getBirthday(), DataRegex.DATE.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 10, DataRegex.DATE.getMessage()));
            if (!valid(person.getOkpo(), DataRegex.INN.getRegex()) || (StringUtils.isNotBlank(person.getOkpo()) && !isValidInn(person.getOkpo())))
                statusList.add(new ManualPersonStatus(person.getId(), 11, DataRegex.INN.getMessage()));
            if (StringUtils.isNotBlank(person.getCountry()) && person.getCountry().length() == 255
                    && person.getLnameUk().contains("..."))
                statusList.add(new ManualPersonStatus(person.getId(), 12, MESSAGE_LONG_VALUE));
            if (!valid(person.getCountry(), DataRegex.NAME_EN.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 12, DataRegex.NAME_EN.getMessage()));
            if (!valid(person.getPhone(), DataRegex.PHONE_NUMBER.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 14, DataRegex.PHONE_NUMBER.getMessage()));
            if (!valid(person.getEmail(), DataRegex.EMAIL.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 15, DataRegex.EMAIL.getMessage()));
            if (StringUtils.isNotBlank(person.getBirthPlace()) && person.getBirthPlace().length() == 255
                    && person.getLnameUk().contains("..."))
                statusList.add(new ManualPersonStatus(person.getId(), 16, MESSAGE_LONG_VALUE));
            if (!valid(person.getBirthPlace(), DataRegex.UK_RU_EN_MULTIPLE.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 16, DataRegex.UK_RU_EN_MULTIPLE.getMessage()));
            if (!valid(person.getSex(), DataRegex.GENDER.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 17, DataRegex.GENDER.getMessage()));
            if (StringUtils.isNotBlank(person.getComment()) && person.getComment().length() == 255
                    && person.getLnameUk().contains("..."))
                statusList.add(new ManualPersonStatus(person.getId(), 18, MESSAGE_LONG_VALUE));
            if (!valid(person.getComment(), DataRegex.UK_RU_EN_MULTIPLE.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 18, DataRegex.UK_RU_EN_MULTIPLE.getMessage()));
            if (!valid(person.getPassLocalNum(), DataRegex.PASSPORT_NUMBER.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 19, DataRegex.PASSPORT_NUMBER.getMessage()));
            if (!valid(person.getPassLocalSerial(), DataRegex.DOMESTIC_PASSPORT_SERIES.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 20, DataRegex.DOMESTIC_PASSPORT_SERIES.getMessage()));
            if (StringUtils.isNotBlank(person.getPassLocalIssuer()) && person.getPassLocalIssuer().length() == 255
                    && person.getLnameUk().contains("..."))
                statusList.add(new ManualPersonStatus(person.getId(), 21, MESSAGE_LONG_VALUE));
            if (!valid(person.getPassLocalIssuer(), DataRegex.UK_RU_MULTIPLE.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 21, DataRegex.UK_RU_MULTIPLE.getMessage()));
            if (!valid(person.getPassLocalIssueDate(), DataRegex.DATE.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 22, DataRegex.DATE.getMessage()));
            if (!valid(person.getPassIntNum(), DataRegex.FOREIGN_PASSPORT_NUMBER.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 23, DataRegex.FOREIGN_PASSPORT_NUMBER.getMessage()));
            if (!valid(person.getPassIntRecNum(), DataRegex.FOREIGN_PASSPORT_RECORD_NUMBER.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 24, DataRegex.FOREIGN_PASSPORT_RECORD_NUMBER.getMessage()));
            if (!valid(person.getPassIntIssuer(), DataRegex.FOREIGN_PASSPORT_AUTHORITY.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 25, DataRegex.FOREIGN_PASSPORT_AUTHORITY.getMessage()));
            if (!valid(person.getPassIntIssueDate(), DataRegex.DATE.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 26, DataRegex.DATE.getMessage()));
            if (!valid(person.getPassIdNum(), DataRegex.ID_PASSPORT_NUMBER.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 27, DataRegex.ID_PASSPORT_NUMBER.getMessage()));
            if (!valid(person.getPassIdRecNum(), DataRegex.ID_PASSPORT_RECORD_NUMBER.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 28, DataRegex.ID_PASSPORT_RECORD_NUMBER.getMessage()));
            if (!valid(person.getPassIdIssuer(), DataRegex.ID_PASSPORT_AUTHORITY.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 29, DataRegex.ID_PASSPORT_AUTHORITY.getMessage()));
            if (!valid(person.getPassIdIssueDate(), DataRegex.DATE.getRegex()))
                statusList.add(new ManualPersonStatus(person.getId(), 30, DataRegex.DATE.getMessage()));
            if (StringUtils.isBlank(person.getOkpo())
                    && StringUtils.isBlank(person.getLnameUk())
                    && StringUtils.isBlank(person.getPassIdNum())
                    && StringUtils.isBlank(person.getPassIntNum())
                    && (StringUtils.isBlank(person.getPassLocalNum())
                    && StringUtils.isBlank(person.getPassLocalSerial()))) {
                statusList.add(new ManualPersonStatus(person.getId(), 1, MESSAGE_EMPTY_DATA));
                statusList.add(new ManualPersonStatus(person.getId(), 11, MESSAGE_EMPTY_DATA));
                statusList.add(new ManualPersonStatus(person.getId(), 27, MESSAGE_EMPTY_DATA));
                statusList.add(new ManualPersonStatus(person.getId(), 19, MESSAGE_EMPTY_DATA));
                statusList.add(new ManualPersonStatus(person.getId(), 20, MESSAGE_EMPTY_DATA));
                statusList.add(new ManualPersonStatus(person.getId(), 23, MESSAGE_EMPTY_DATA));
            }
        }
        return statusList;
    }

    private static boolean valid(String value, String regex) {
        if (value == null || regex == null) return true;
        return value.matches(regex);
    }

    private static boolean isValidInn(String inn) {
        if (StringUtils.isBlank(inn)) return false;
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
