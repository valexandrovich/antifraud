package ua.com.solidity.enricher.model;

import lombok.Data;

import java.util.UUID;

@Data
public class EnricherPortionRequest {
    private String table;
    private UUID portion;
}
