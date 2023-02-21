package ua.com.solidity.report.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Column {

    NUMBER(0, "№"),
    NAME(1, "ПІБ/Назва компанії") {
        @Override
        public int getColumnWidth() {
            return super.getColumnWidth() * 2;
        }
    },
    UNIQUE_IDENTIFIER(2, "РНОКПП/ЄДРПОУ"),
    TAG_TYPE_CODE(3, "Код мітки"),
    EVENT_DATE(4, "Дата події"),
    START_DATE(5, "Дата початку дії мітки"),
    END_DATE(6, "Дата закінчення дії мітки"),
    NUMBER_VALUE(7, "Числове значення мітки"),
    TEXT_VALUE(8, "Текстове значення мітки") {
        @Override
        public int getColumnWidth() {
            return (int) (super.getColumnWidth() * 2.5);
        }
    },
    DESCRIPTION(9, "Опис мітки") {
        @Override
        public int getColumnWidth() {
            return super.getColumnWidth() * 4;
        }
    },
    SOURCE(10, "Джерело мітки");
    private final int position;
    private final String name;

    public int getColumnWidth() {
        return (name.length() + 3) * 256;
    }
}
