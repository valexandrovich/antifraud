package ua.com.solidity.dwh.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class EnricherRequest {
    private String table;
    private UUID revision;
}
