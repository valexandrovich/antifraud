package ua.com.solidity.enricher.service;

import ua.com.solidity.enricher.model.EnricherPortionRequest;

public interface EnricherService {

    void enrich(EnricherPortionRequest enricherRequest);

}
