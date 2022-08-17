package ua.com.solidity.web.service.validator;

public enum DataRegex {
    LNAME_UK("^([А-ГҐДЕЄЖЗИІЇЙ-ЩЬЮЯа-гґдеєжзиіїй-щьюя']+-[А-ГҐДЕЄЖЗИІЇЙ-ЩЬЮЯа-гґдеєжзиіїй-щьюя']+)?([А-ГҐДЕЄЖЗИІЇЙ-ЩЬЮЯа-гґдеєжзиіїй-щьюя']+)?()$", "одне слово або два слова через дефіс українськими літерами"),
    LNAME_RU("^([А-Яа-я]+-[А-Яа-я]+)?([А-Яа-я]+)?()$", "одне слово або два слова через дефіс російськими літерами"),
    LNAME_EN("^([A-Za-z]+-[A-Za-z]+)?([A-Za-z]+)?()$", "одне слово або два слова через дефіс англійськими літерами"),
    NAME_UK("^([А-ГҐДЕЄЖЗИІЇЙ-ЩЬЮЯа-гґдеєжзиіїй-щьюя']+)?()$", "одне слово українськими літерами"),
    NAME_RU("^([А-Яа-я]+$)?()", "одне слово російськими літерами"),
    NAME_EN("^([A-Za-z]+)?()$", "одне слово англійськими літерами"),
    DATE("^((0?[1-9]|[12][0-9]|3[01]).(0?[1-9]|1[012]).[1-3]\\d{3})?()$", "дата у форматі дд.мм.гггг"),
    INN("^([\\d]{10})?()$", "номер містить 10 цифр і правильну контрольну суму"),
    EDRPOU("^([\\d]{8,9})?()$", "номер містить від 8 до 9 цифр і правильну контрольну суму"),
    PDV("^([\\d]{12})?()$", "номер містить 12 цифр і правильну контрольну суму"),
    PASSPORT_NUMBER("(^[\\d]{6})?()$", "номер містить 6 цифр"),
    DOMESTIC_PASSPORT_SERIES("^([А-ГҐДЕЄЖЗИІЇЙ-ЩЬЮЯ]{2})?()$", "дві великі українські літери"),
    FOREIGN_PASSPORT_SERIES("^([A-Z]{2})?()$", "дві великі англійські літери"),
    FOREIGN_PASSPORT_NUMBER("^([A-Z]{2}[0-9]{6})?()$", "дві великі англійські літери шість цифр разом -> KK123456"),
    FOREIGN_PASSPORT_RECORD_NUMBER("^([\\d]{8}-[\\d]{5})?()$", "подвійний номер з восьми та п'яти цифр розділені тире -> 12345678-12345"),
    FOREIGN_PASSPORT_AUTHORITY("^([\\d]{4})?()$", "номер із чотирьох цифр -> 1234"),
    ID_PASSPORT_NUMBER("^([\\d]{9})?()$", "номер містить 9 цифр"),
    ID_PASSPORT_RECORD_NUMBER("^([0-9]{8}-[0-9]{5})?()$", "подвійний номер з восьми та п'яти цифр розділені тире -> 12345678-12345"),
    ID_PASSPORT_AUTHORITY("^([\\d]{4})?()$", "номер із чотирьох цифр -> 1234"),
    BOOLEAN_TAG("^(+x)?()$", "може містити + або х"),
    UK_RU_EN_MULTIPLE("^([А-Яа-яA-Za-zҐЄІЇґєії'\\W]+)?()", "текст може містити українські англійські та російські слова, включаючи символи та пробіли"),
    UK_RU_EN_SINGLE("^([А-Яа-яA-Za-zҐЄІЇґєії']+)?()$", "одне слово українською англійською або російською"),
    PHONE_NUMBER("^((\\s*)?(\\+)?([- _():=+]?\\d[- _():=+]?){10,14}(\\s*))?()$", "номер може містити до 14 цифр"),
    EMAIL("^([a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,6})?()$", "має бути валідна електронна пошта(e-mail)"),
    GENDER("^[MFМЖЧ]?()$", "один із символів -> M F Ч Ж"),
    UK_RU_MULTIPLE("^([А-Яа-яҐЄІЇґєії'\\W]+)?()", "текст може містити українські та російські слова, включаючи символи та пробіли"),
    CNUM("^([0-9]{2,6})?()$", "номер може містити до 6 цифр"),
    NUMBER("^([0-9]*[.,][0-9]+)?(\\d+)?()$", "може містити цифри");

    DataRegex(String regex, String message) {
        this.regex = regex;
        this.message = message;
    }

    private String regex;

    private String message;

    public String getRegex() {
        return regex;
    }

    public String getMessage() {
        return message;
    }
}
