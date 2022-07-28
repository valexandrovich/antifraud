package ua.com.solidity.dispatcher.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import ua.com.solidity.dispatcher.model.YCompanyProcessing;
import ua.com.solidity.dispatcher.model.response.YCompanyDispatcherResponse;
import ua.com.solidity.dispatcher.storage.YCompanyStorage;

@Service
public class YCompanyDispatcherService {

    public YCompanyDispatcherResponse dispatch(List<YCompanyProcessing> companies) {
        List<YCompanyProcessing> resp = new ArrayList<>();
        List<UUID> temp = new ArrayList<>();
        companies.forEach(company -> {
            if (YCompanyStorage.companiesSet.parallelStream().filter(Objects::nonNull).anyMatch(c ->
                    (c.getEdrpou() != null && company.getEdrpou() != null && c.getEdrpou().equals(company.getEdrpou()))
                            || (c.getPdv() != null && company.getPdv() != null && c.getPdv().equals(company.getPdv()))
                            || (c.getCompanyHash() != null && company.getCompanyHash() != null && c.getCompanyHash().equals(company.getCompanyHash())))) {
                temp.add(company.getUuid());
            } else {
                resp.add(company);
            }
        });
        YCompanyDispatcherResponse response = new YCompanyDispatcherResponse();
        response.setResp(resp.parallelStream().map(YCompanyProcessing::getUuid).collect(Collectors.toList()));
        response.setTemp(temp);
        YCompanyStorage.companiesSet.addAll(resp);
        return response;

    }

    public boolean remove(List<UUID> resp) {
        Set<YCompanyProcessing> deleteSet = YCompanyStorage.companiesSet.parallelStream()
                .filter(p -> resp.contains(p.getUuid())).collect(Collectors.toSet());
        return YCompanyStorage.companiesSet.removeAll(deleteSet);
    }
}
