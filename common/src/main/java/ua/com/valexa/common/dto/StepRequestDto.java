package ua.com.valexa.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

@Data
@NoArgsConstructor
@ToString
public class StepRequestDto {
    private Long id;
    private String workerName;
    private Map<String, String> parameters;
}
