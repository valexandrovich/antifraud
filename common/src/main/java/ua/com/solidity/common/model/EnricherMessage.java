package ua.com.solidity.common.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@SuppressWarnings("unused")
public class EnricherMessage {
    private String table;
    private UUID revision;
}
