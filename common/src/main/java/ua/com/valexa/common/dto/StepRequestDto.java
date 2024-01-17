package ua.com.valexa.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class StepRequestDto {
    private Long id;
    private String workerName;
    private Map<String, String> parameters;
}
