package ua.com.solidity.dispatcher.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.com.solidity.dispatcher.storage.EntityStorage;
import ua.com.solidity.util.model.EntityProcessing;
import ua.com.solidity.util.model.response.DispatcherResponse;

@Slf4j
@Service
public class DispatcherService {

    public DispatcherResponse dispatch(List<EntityProcessing> people, String id) {
        LocalDateTime timeLimit = LocalDateTime.now().minusMinutes(3);
        log.info("Started processing id: {}, size: {}, current cache size: {}", id, people.size(), EntityStorage.entitySet.size());
        Set<EntityProcessing> resp = new HashSet<>();
        List<UUID> temp = new ArrayList<>();
        List<UUID> respId = new ArrayList<>();
        Set<Long> inns = new HashSet<>();
        Set<Integer> passes = new HashSet<>();
        Set<Integer> peopleHash = new HashSet<>();
        Set<Long> edrpous = new HashSet<>();
        Set<Long> pdvs = new HashSet<>();
        Set<Integer> companiesHash = new HashSet<>();
        EntityStorage.entitySet.forEach(person -> {
            inns.add(person.getInn());
            passes.add(person.getPassHash());
            peopleHash.add(person.getPersonHash());
            edrpous.add(person.getEdrpou());
            pdvs.add(person.getPdv());
            companiesHash.add(person.getCompanyHash());
        });

        Set<EntityProcessing> oldEntities = EntityStorage.entitySet.parallelStream()
                        .filter(entity -> entity.getAddingTime().isBefore(timeLimit)).collect(Collectors.toSet());
        EntityStorage.entitySet.removeAll(oldEntities);

        people.forEach(person -> {
            if ((inns.contains(person.getInn()) && person.getInn() != 0)
                    || (passes.contains(person.getPassHash()) && person.getPassHash() != 0)
                    || (peopleHash.contains(person.getPersonHash()) && person.getPersonHash() != 0)
                    || (edrpous.contains(person.getEdrpou()) && person.getEdrpou() != 0)
                    || (pdvs.contains(person.getPdv()) && person.getPdv() != 0)
                    || (companiesHash.contains(person.getCompanyHash()) && person.getCompanyHash() != 0))
                temp.add(person.getUuid());
            else {
                person.setAddingTime(LocalDateTime.now());
                resp.add(person);
                respId.add(person.getUuid());
            }
        });
        DispatcherResponse response = new DispatcherResponse();
        response.setResp(resp);
        response.setTemp(temp);
        response.setRespId(respId);
        EntityStorage.entitySet.addAll(resp);
        log.info("Processing stopped id: {}, processing: {}, skipping: {}, current cache size: {}", id, resp.size(), temp.size(), EntityStorage.entitySet.size());
        return response;
    }

    public boolean remove(Set<EntityProcessing> resp) {
        log.info("Remove started size: {}", resp.size());
        boolean result = EntityStorage.entitySet.removeAll(resp);
        log.info("Remove finished current cache size: {}", EntityStorage.entitySet.size());
        return result;
    }
}
