package ua.com.solidity.util.validator;

import static ua.com.solidity.util.validator.Regex.EDRPOU_FORMAT_REGEX;
import static ua.com.solidity.util.validator.Regex.INN_REGEX;

import java.time.LocalDate;
import java.util.Objects;

public final class Validator {

    private Validator() {
    }

    public static final LocalDate START_DATE = LocalDate.of(1900, 1, 1);

    public static boolean isValidInn(String inn, LocalDate birthDay, String sex) {
        if (inn == null || inn.isBlank()) return false;
        inn = inn.replaceAll("[^0-9]", "");
        if (!inn.matches("[0-9]+")) return false;
        inn = String.format(Regex.INN_FORMAT_REGEX, Long.parseLong(inn));
        if (inn.matches(INN_REGEX)) {
            boolean isValidBirthDateInn = birthDay == null ||
                    Objects.equals(String.valueOf(birthDay.toEpochDay()
                            - START_DATE.toEpochDay() + 1L), inn.substring(0, 5));
            String findSex = Integer.parseInt(String.valueOf(inn.charAt(8))) % 2 == 0 ? "Ж" : "Ч";
            boolean isValidSexInn = sex == null || sex.isBlank() || Objects.equals(findSex, sex);
            int controlNumber = ((-1 * Integer.parseInt(String.valueOf(inn.charAt(0)))
                    + 5 * Integer.parseInt(String.valueOf(inn.charAt(1)))
                    + 7 * Integer.parseInt(String.valueOf(inn.charAt(2)))
                    + 9 * Integer.parseInt(String.valueOf(inn.charAt(3)))
                    + 4 * Integer.parseInt(String.valueOf(inn.charAt(4)))
                    + 6 * Integer.parseInt(String.valueOf(inn.charAt(5)))
                    + 10 * Integer.parseInt(String.valueOf(inn.charAt(6)))
                    + 5 * Integer.parseInt(String.valueOf(inn.charAt(7)))
                    + 7 * Integer.parseInt(String.valueOf(inn.charAt(8)))) % 11) % 10;
            return Objects.equals(Integer.parseInt(String.valueOf(inn.charAt(9))), controlNumber)
                    && isValidBirthDateInn && isValidSexInn;
        }
        return false;
    }

    public static boolean isValidPdv(String pdv) {
        pdv = String.format("%012d", Long.parseLong(pdv));
        if (pdv.matches(Regex.PDV_REGEX)) {
            int controlNumber = (11 * Integer.parseInt(String.valueOf(pdv.charAt(0)))
                    + 13 * Integer.parseInt(String.valueOf(pdv.charAt(1)))
                    + 17 * Integer.parseInt(String.valueOf(pdv.charAt(2)))
                    + 19 * Integer.parseInt(String.valueOf(pdv.charAt(3)))
                    + 23 * Integer.parseInt(String.valueOf(pdv.charAt(4)))
                    + 29 * Integer.parseInt(String.valueOf(pdv.charAt(5)))
                    + 31 * Integer.parseInt(String.valueOf(pdv.charAt(6)))
                    + 37 * Integer.parseInt(String.valueOf(pdv.charAt(7)))
                    + 41 * Integer.parseInt(String.valueOf(pdv.charAt(8)))
                    + 43 * Integer.parseInt(String.valueOf(pdv.charAt(9)))
                    + 47 * Integer.parseInt(String.valueOf(pdv.charAt(10)))) % 11;
            if (controlNumber == 10) {
                controlNumber = (13 * Integer.parseInt(String.valueOf(pdv.charAt(0)))
                        + 17 * Integer.parseInt(String.valueOf(pdv.charAt(1)))
                        + 19 * Integer.parseInt(String.valueOf(pdv.charAt(2)))
                        + 23 * Integer.parseInt(String.valueOf(pdv.charAt(3)))
                        + 29 * Integer.parseInt(String.valueOf(pdv.charAt(4)))
                        + 31 * Integer.parseInt(String.valueOf(pdv.charAt(5)))
                        + 37 * Integer.parseInt(String.valueOf(pdv.charAt(6)))
                        + 41 * Integer.parseInt(String.valueOf(pdv.charAt(7)))
                        + 43 * Integer.parseInt(String.valueOf(pdv.charAt(8)))
                        + 47 * Integer.parseInt(String.valueOf(pdv.charAt(9)))
                        + 53 * Integer.parseInt(String.valueOf(pdv.charAt(10)))) % 11;
            }
            return (String.valueOf(pdv.charAt(11)).equals(String.valueOf(controlNumber)));
        }
        return false;
    }

    public static boolean isValidEdrpou(String edrpou) {
        boolean result = false;
        edrpou = String.format(EDRPOU_FORMAT_REGEX, Long.parseLong(edrpou));
        if (edrpou.matches(Regex.OKPO_REGEX)) {
            int controlNumber;
            int code = Integer.parseInt(edrpou);
            if (edrpou.length() == 8) {
                int firstNum = Integer.parseInt(String.valueOf(edrpou.charAt(0)));
                int secondNum = Integer.parseInt(String.valueOf(edrpou.charAt(1)));
                int thirdNum = Integer.parseInt(String.valueOf(edrpou.charAt(2)));
                int fourthNum = Integer.parseInt(String.valueOf(edrpou.charAt(3)));
                int fifthNum = Integer.parseInt(String.valueOf(edrpou.charAt(4)));
                int sixthNum = Integer.parseInt(String.valueOf(edrpou.charAt(5)));
                int seventhNum = Integer.parseInt(String.valueOf(edrpou.charAt(6)));
                if (code < 30000000 || code > 60000000)
                    controlNumber = (firstNum + secondNum * 2 + thirdNum * 3 + fourthNum * 4
                            + fifthNum * 5 + sixthNum * 6 + seventhNum * 7) % 11;
                else controlNumber = (firstNum * 7 + secondNum + thirdNum * 2 + fourthNum * 3
                        + fifthNum * 4 + sixthNum * 5 + seventhNum * 6) % 11;
                if (controlNumber == 10) {
                    if (code < 30000000 || code > 60000000)
                        controlNumber = (firstNum * 3 + secondNum * 4 + thirdNum * 5 + fourthNum * 6
                                + fifthNum * 7 + sixthNum * 8 + seventhNum * 9) % 11;
                    else controlNumber = (firstNum * 9 + secondNum * 3 + thirdNum * 4 + fourthNum * 5
                            + fifthNum * 6 + sixthNum * 7 + seventhNum * 8) % 11;
                }
                if (controlNumber == 10) controlNumber = 0;
                result = (String.valueOf(edrpou.charAt(7)).equals(String.valueOf(controlNumber)));
            }
            if (edrpou.length() == 9 || !result) {
                edrpou = String.format("%09d", Long.parseLong(edrpou));
                int firstNum = Integer.parseInt(String.valueOf(edrpou.charAt(0)));
                int secondNum = Integer.parseInt(String.valueOf(edrpou.charAt(1)));
                int thirdNum = Integer.parseInt(String.valueOf(edrpou.charAt(2)));
                int fourthNum = Integer.parseInt(String.valueOf(edrpou.charAt(3)));
                int fifthNum = Integer.parseInt(String.valueOf(edrpou.charAt(4)));
                int sixthNum = Integer.parseInt(String.valueOf(edrpou.charAt(5)));
                int seventhNum = Integer.parseInt(String.valueOf(edrpou.charAt(6)));
                int eighthNum = Integer.parseInt(String.valueOf(edrpou.charAt(7)));
                controlNumber = (firstNum * 9 + secondNum * 11 + thirdNum * 13 + fourthNum * 17
                        + fifthNum * 19 + sixthNum * 23 + seventhNum * 29 + eighthNum * 31) % 11;
                if (controlNumber == 10) {
                    controlNumber = (firstNum * 11 + secondNum * 13 + thirdNum * 17 + fourthNum * 19
                            + fifthNum * 23 + sixthNum * 29 + seventhNum * 31 + eighthNum * 37) % 11;
                }
                result = (String.valueOf(edrpou.charAt(8)).equals(String.valueOf(controlNumber)));
            }
        }
        return result;
    }
}
