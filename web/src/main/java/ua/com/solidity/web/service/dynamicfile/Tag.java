package ua.com.solidity.web.service.dynamicfile;

public enum Tag {
    B020_1 ("Подтвержденный факт смерти"),
    B020_2 ("Неподтвержденный факт смерти"),
    A001 ("Клиент"),
    G020_1 ("Криминал 1 уровня"),
    G020_2 ("Криминал 2 уровня"),
    G020_3 ("Криминал 3 уровня"),
    G020_4 ("Административная ответственность"),
    B240_1 ("Возбуждено дело о банкротстве"),
    B240_2 ("Признан банкротом"),
    B240_3 ("Санация"),
    B240_4 ("Ликвидация"),
    G110_1 ("Другая негативная информация"),
    G160_1 ("Списаная задолженность"),
    S205_1 ("Физ. Лицо PEP"),
    S205_2 ("Физ. Лицо имеет признаки PEP");

    private String name;

    Tag(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
