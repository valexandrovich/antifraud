package ua.com.solidity.dwh.model;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class UpdateDWHRequest {
    private Timestamp lastModified; // Means import all records modified after timestamp
}
