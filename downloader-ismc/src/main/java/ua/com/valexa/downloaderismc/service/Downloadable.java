package ua.com.valexa.downloaderismc.service;

import ua.com.valexa.common.dto.StepResponseDto;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface Downloadable {
    StepResponseDto handleDownload(Long stepId, Map<String, String> parameters);
}

