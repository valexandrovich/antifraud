package ua.com.solidity.dispatcher.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.com.solidity.dispatcher.storage.YPersonStorage;
import ua.com.solidity.util.model.YPersonProcessing;
import ua.com.solidity.util.model.response.YPersonDispatcherResponse;

@Slf4j
@Service
public class YPersonDispatcherService {

    public YPersonDispatcherResponse dispatch(List<YPersonProcessing> people, String id) {
        log.info("Started processing id: {}, size: {}, current cache size: {}", id, people.size(), YPersonStorage.peopleSet.size());
        List<YPersonProcessing> resp = new ArrayList<>();
        List<UUID> temp = new ArrayList<>();
        people.forEach(person -> {
            if (YPersonStorage.peopleSet.parallelStream().filter(Objects::nonNull).anyMatch(p ->
                    (p.getInn() != null && person.getInn() != null && p.getInn().equals(person.getInn()))
                            || (p.getPassHash() != null && person.getPassHash() != null && p.getPassHash().equals(person.getPassHash()))
                            || (p.getPersonHash() != null && person.getPersonHash() != null && p.getPersonHash().equals(person.getPersonHash())))) {
                temp.add(person.getUuid());
            } else {
                resp.add(person);
            }
        });
        YPersonDispatcherResponse response = new YPersonDispatcherResponse();
        response.setResp(resp.parallelStream().map(YPersonProcessing::getUuid).collect(Collectors.toList()));
        response.setTemp(temp);
        YPersonStorage.peopleSet.addAll(resp);
        log.info("Processing stopped id: {}, processing: {}, skipping: {}, current cache size: {}", id, resp.size(), temp.size(), YPersonStorage.peopleSet.size());
        return response;

    }

    public boolean remove(List<UUID> resp) {

        Set<YPersonProcessing> deleteSet = YPersonStorage.peopleSet.parallelStream()
                .filter(p -> resp.contains(p.getUuid())).collect(Collectors.toSet());
        log.info("Remove started size: {}", deleteSet.size());
        boolean result = YPersonStorage.peopleSet.removeAll(deleteSet);
        log.info("Remove finished current cache size: {}", YPersonStorage.peopleSet.size());
        return result;
    }
}
