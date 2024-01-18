package ua.com.valexa.common.dto;

import lombok.Data;
import ua.com.valexa.dbismc.model.enums.StepStatus;


import java.util.HashMap;
import java.util.Map;

@Data
public class StepResponseDto {
    private Long stepId;
    private StepStatus status;
    private String comment;
    private Map<String, String> results = new HashMap<>();
}
