package ua.com.solidity.enricher.model;

import lombok.Data;

import java.util.UUID;

@Data
public class EnricherRequest {
    private String table;
    private UUID revision;
}