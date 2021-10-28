package ua.com.solidity.otp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DataSourceColumnMapping {
    private Integer sourceIndex;
    private String sourceColumnName;
    private String targetColumnName;
    @Enumerated(EnumType.STRING)
    private DataType sourceDataType;
    @Enumerated(EnumType.STRING)
    private DataType targetDataType;

    public enum DataType {
        TEXT,
        INTEGER,
        DOUBLE,
        DATE,
        BOOLEAN
    }
}
