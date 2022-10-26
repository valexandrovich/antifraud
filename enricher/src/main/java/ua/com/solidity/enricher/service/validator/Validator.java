package ua.com.solidity.enricher.service.validator;

import static ua.com.solidity.enricher.util.LogUtil.logError;
import static ua.com.solidity.enricher.util.Regex.DOMESTIC_SERIES_REGEX;
import static ua.com.solidity.enricher.util.Regex.FOREIGN_SERIES_REGEX;
import static ua.com.solidity.enricher.util.Regex.IDCARD_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.Regex.PASS_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.Regex.RECORD_NUMBER_REGEX;
import static ua.com.solidity.enricher.util.StringFormatUtil.transliterationToCyrillicLetters;
import static ua.com.solidity.enricher.util.StringFormatUtil.transliterationToLatinLetters;

import lombok.CustomLog;
import org.apache.commons.lang3.StringUtils;
import ua.com.solidity.common.DefaultErrorLogger;
import ua.com.solidity.db.entities.YPerson;

@CustomLog
public final class Validator {
    private Validator() {
    }

    private static final String PASSPORT = "Passport: ";
    private static final String RECORD_NUMBER = "Record number: ";
    private static final String PASSPORT_NUMBER = "Passport number: ";

    public static boolean isEqualsPerson(YPerson person, YPerson newPerson) {
        return !(newPerson != null && person != null
                && ((person.getLastName() != null && newPerson.getLastName() != null
                && !person.getLastName().isBlank() && !newPerson.getLastName().isBlank() && !person.getLastName().equals(newPerson.getLastName()))
                || (person.getFirstName() != null && newPerson.getFirstName() != null
                && !person.getFirstName().isBlank() && !newPerson.getFirstName().isBlank() && !person.getFirstName().equals(newPerson.getFirstName()))
                || (person.getPatName() != null && newPerson.getPatName() != null
                && !person.getPatName().isBlank() && !newPerson.getPatName().isBlank() && !person.getPatName().equals(newPerson.getPatName()))
                || (person.getBirthdate() != null && newPerson.getBirthdate() != null && !person.getBirthdate().equals(newPerson.getBirthdate()))));
    }

    public static boolean isValidLocalPassport(String number, String serial,
                                               long[] counter, DefaultErrorLogger logger) {
        boolean isValidPass = true;
        if (StringUtils.isBlank(number) || StringUtils.isBlank(serial)) {
            logError(logger, (counter[0] + 1L), PASSPORT + serial + number, "Empty serial or number");
            isValidPass = false;
        } else if (!transliterationToCyrillicLetters(serial).matches(DOMESTIC_SERIES_REGEX)
                || !number.matches(PASS_NUMBER_REGEX)) {
            logError(logger, (counter[0] + 1L), PASSPORT + serial + number, "Wrong format passport serial or number");
            isValidPass = false;
        } else if (number.length() > 6) {
            log.warn("Passport with serial: " + serial + " and number: " + number + " has more than 6 digits in number");
            isValidPass = false;
        }
        return isValidPass;
    }

    public static boolean isValidForeignPassport(String number, String serial, String recordNumber,
                                                 long[] counter, DefaultErrorLogger logger) {
        boolean isValidPass = true;
        if (StringUtils.isBlank(number) || StringUtils.isBlank(serial)) {
            logError(logger, (counter[0] + 1L), PASSPORT + serial + number, "Empty serial or number");
            isValidPass = false;
        } else if (!transliterationToLatinLetters(serial).matches(FOREIGN_SERIES_REGEX)
                || !number.matches(PASS_NUMBER_REGEX)) {
            logError(logger, (counter[0] + 1L), PASSPORT + serial + number, "Wrong format passport serial or number");
            isValidPass = false;
        } else if (!StringUtils.isBlank(recordNumber) && !recordNumber.matches(RECORD_NUMBER_REGEX)) {
            logError(logger, (counter[0] + 1L), RECORD_NUMBER + recordNumber, "Wrong format passport record number");
            isValidPass = false;
        } else if (number.length() > 6) {
            log.warn("Passport with serial: " + serial + " and number: " + number + " has more than 6 digits in number");
            isValidPass = false;
        }
        return isValidPass;
    }

    public static boolean isValidIdPassport(String number, String recordNumber,
                                            long[] counter, DefaultErrorLogger logger) {
        boolean isValidPass = true;
        if (StringUtils.isBlank(number)) {
            logError(logger, (counter[0] + 1L), PASSPORT_NUMBER + number, "Empty number");
            isValidPass = false;
        } else if (!number.matches(IDCARD_NUMBER_REGEX)) {
            logError(logger, (counter[0] + 1L), PASSPORT_NUMBER + number, "Wrong format passport number");
            isValidPass = false;
        } else if (!StringUtils.isBlank(recordNumber) && !recordNumber.matches(RECORD_NUMBER_REGEX)) {
            logError(logger, (counter[0] + 1L), RECORD_NUMBER + recordNumber, "Wrong format passport record number");
            isValidPass = false;
        } else if (number.length() > 9) {
            log.warn("Passport with number: " + number + " has more than 9 digits in number");
            isValidPass = false;
        }
        return isValidPass;
    }
}
