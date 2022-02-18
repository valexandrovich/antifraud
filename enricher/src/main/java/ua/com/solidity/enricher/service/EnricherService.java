package ua.com.solidity.enricher.service;

import ua.com.solidity.enricher.model.EnricherRequest;

public interface EnricherService {

    void enrich(EnricherRequest enricherRequest);

}
