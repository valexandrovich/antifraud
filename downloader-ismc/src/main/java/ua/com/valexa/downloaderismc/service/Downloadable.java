package ua.com.valexa.downloaderismc.service;

import java.util.Map;

public interface Downloadable {
    void handleDownload(Long stepId, Map<String, String> parameters);
}

