package ua.com.solidity.enricher.service;

import static ua.com.solidity.enricher.util.Base.BASE_CREATOR;
import static ua.com.solidity.enricher.util.Base.BASE_DIRECTOR;
import static ua.com.solidity.enricher.util.Base.BASE_DRFO;
import static ua.com.solidity.enricher.util.Base.BASE_ELECTIONS;
import static ua.com.solidity.enricher.util.Base.BASE_FODB;
import static ua.com.solidity.enricher.util.Base.BASE_PASSPORTS;
import static ua.com.solidity.enricher.util.Base.CONTRAGENT;
import static ua.com.solidity.enricher.util.Base.GOVUA1;
import static ua.com.solidity.enricher.util.Base.GOVUA10;
import static ua.com.solidity.enricher.util.Base.GOVUA11;
import static ua.com.solidity.enricher.util.Base.GOVUA17;
import static ua.com.solidity.enricher.util.Base.GOVUA20;
import static ua.com.solidity.enricher.util.Base.MANUAL_PERSON;

import java.util.Map;
import java.util.Objects;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ua.com.solidity.enricher.model.EnricherPortionRequest;
import ua.com.solidity.enricher.service.enricher.BaseCreatorEnricher;
import ua.com.solidity.enricher.service.enricher.BaseDirectorEnricher;
import ua.com.solidity.enricher.service.enricher.BaseDrfoEnricher;
import ua.com.solidity.enricher.service.enricher.BaseElectionsEnricher;
import ua.com.solidity.enricher.service.enricher.BaseFodbEnricher;
import ua.com.solidity.enricher.service.enricher.BasePassportsEnricher;
import ua.com.solidity.enricher.service.enricher.ContragentEnricher;
import ua.com.solidity.enricher.service.enricher.Govua10Enricher;
import ua.com.solidity.enricher.service.enricher.Govua11Enricher;
import ua.com.solidity.enricher.service.enricher.Govua17Enricher;
import ua.com.solidity.enricher.service.enricher.Govua1Enricher;
import ua.com.solidity.enricher.service.enricher.Govua20Enricher;
import ua.com.solidity.enricher.service.enricher.ManualPersonEnricher;

@CustomLog
@Component
@RequiredArgsConstructor
public class EnricherProxy {

    private final BaseDrfoEnricher baseDrfoEnricher;
    private final BaseElectionsEnricher baseElectionsEnricher;
    private final BaseFodbEnricher baseFodbEnricher;
    private final BasePassportsEnricher basePassportsEnricher;
    private final ContragentEnricher contragentEnricher;
    private final ManualPersonEnricher manualPersonEnricher;
    private final BaseDirectorEnricher baseDirectorEnricher;
    private final BaseCreatorEnricher baseCreatorEnricher;
    private final Govua10Enricher govua10Enricher;
    private final Govua1Enricher govua1Enricher;
    private final Govua11Enricher govua11Enricher;
    private final Govua17Enricher govua17Enricher;
    private final Govua20Enricher govua20Enricher;

    public void direct(EnricherPortionRequest enricherRequest) {
        log.info("Received request to enrich {} portion {}", enricherRequest.getTable(), enricherRequest.getPortion());
        Map<String, Runnable> baseMap =
                Map.ofEntries(
                        Map.entry(BASE_DRFO, () -> baseDrfoEnricher.enrich(enricherRequest.getPortion())),
                        Map.entry(BASE_ELECTIONS, () -> baseElectionsEnricher.enrich(enricherRequest.getPortion())),
                        Map.entry(BASE_FODB, () -> baseFodbEnricher.enrich(enricherRequest.getPortion())),
                        Map.entry(BASE_PASSPORTS, () -> basePassportsEnricher.enrich(enricherRequest.getPortion())),
                        Map.entry(CONTRAGENT, () -> contragentEnricher.enrich(enricherRequest.getPortion())),
                        Map.entry(MANUAL_PERSON, () -> manualPersonEnricher.enrich(enricherRequest.getPortion())),
                        Map.entry(BASE_DIRECTOR, () -> baseDirectorEnricher.enrich(enricherRequest.getPortion())),
                        Map.entry(BASE_CREATOR, () -> baseCreatorEnricher.enrich(enricherRequest.getPortion())),
                        Map.entry(GOVUA10, () -> govua10Enricher.enrich(enricherRequest.getPortion())),
                        Map.entry(GOVUA1, () -> govua1Enricher.enrich(enricherRequest.getPortion())),
                        Map.entry(GOVUA11, () -> govua11Enricher.enrich(enricherRequest.getPortion())),
                        Map.entry(GOVUA20, () -> govua20Enricher.enrich(enricherRequest.getPortion())),
                        Map.entry(GOVUA17, () -> govua17Enricher.enrich(enricherRequest.getPortion()))
                );

        baseMap.entrySet()
                .stream()
                .filter(entry -> Objects.equals(entry.getKey(), enricherRequest.getTable()))
                .findFirst()
                .ifPresentOrElse(entry -> entry.getValue().run(),
                        () -> log.warn("Ignoring unsupported {} enrichment", enricherRequest.getTable()));

    }
}
