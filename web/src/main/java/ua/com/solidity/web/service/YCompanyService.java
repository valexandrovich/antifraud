package ua.com.solidity.web.service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ua.com.solidity.db.entities.User;
import ua.com.solidity.db.entities.YCompany;
import ua.com.solidity.db.repositories.YCompanyRepository;
import ua.com.solidity.web.dto.YCompanyDto;
import ua.com.solidity.web.dto.YCompanySearchDto;
import ua.com.solidity.web.exception.EntityNotFoundException;
import ua.com.solidity.web.request.PaginationRequest;
import ua.com.solidity.web.request.PaginationYCompanySearchRequest;
import ua.com.solidity.web.request.YCompanySearchRequest;
import ua.com.solidity.web.search.GenericSpecification;
import ua.com.solidity.web.search.SearchCriteria;
import ua.com.solidity.web.search.SearchOperation;
import ua.com.solidity.web.security.service.Extractor;
import ua.com.solidity.web.service.converter.YCompanyConverter;
import ua.com.solidity.web.service.factory.PageRequestFactory;

@Slf4j
@RequiredArgsConstructor
@Service
public class YCompanyService {
    private final Extractor extractor;
    private final YCompanyRepository yCompanyRepository;
    private final YCompanyConverter yCompanyConverter;
    private final PageRequestFactory pageRequestFactory;

    private static final String ALT_COMPANIES = "altCompanies";
    private static final String NAME = "name";
    private static final String EDRPOU = "edrpou";
    private static final String PDV = "pdv";
    private static final String ADDRESS = "address";
    private static final String ADDRESSES = "addresses";
    boolean criteriaFound;


    public Page<YCompanySearchDto> search(PaginationYCompanySearchRequest paginationSearchRequest,
                                          HttpServletRequest httpServletRequest) {
        PaginationRequest paginationRequest = paginationSearchRequest.getPaginationRequest();
        YCompanySearchRequest searchRequest = paginationSearchRequest.getSearchRequest();

        GenericSpecification<YCompany> gs = new GenericSpecification<>();
        criteriaFound = false;

        Specification<YCompany> gsName = searchByName(searchRequest);

        String edrpou = Objects.toString(searchRequest.getEdrpou(), "");
        if (!edrpou.equals("")) {
            criteriaFound = true;
            gs.add(new SearchCriteria(EDRPOU, edrpou, null, SearchOperation.EQUALS));
        }

        String pdv = Objects.toString(searchRequest.getPdv(), "");
        if (!pdv.equals("")) {
            criteriaFound = true;
            gs.add(new SearchCriteria(PDV, pdv, null, SearchOperation.EQUALS));
        }

        String address = Objects.toString(searchRequest.getAddress().toUpperCase().trim(), "");
        if (!address.equals("")) {
            criteriaFound = true;
            gs.add(new SearchCriteria(ADDRESS, address, ADDRESSES, SearchOperation.MATCH));
        }

        PageRequest pageRequest = pageRequestFactory.getPageRequest(paginationRequest);
        if (criteriaFound) {
            User user = extractor.extractUser(httpServletRequest);
            return yCompanyRepository.findAll(gsName.and(gs), pageRequest)
                    .map(entity -> {
                        YCompanySearchDto dto = yCompanyConverter.toSearchDto(entity);
                        user.getCompanies().forEach(subscribedYCompany -> {
                            if (subscribedYCompany.getId() == dto.getId()) dto.setSubscribe(true);
                        });
                        return dto;
                    });
        } else {
            return Page.empty(pageRequest);
        }
    }

    private Specification<YCompany> searchByName(YCompanySearchRequest searchRequest) {
        GenericSpecification<YCompany> gs = new GenericSpecification<>();
        GenericSpecification<YCompany> gsAltName = new GenericSpecification<>();
        String name = Objects.toString(searchRequest.getName().toUpperCase().trim(), ""); // Protection from null
        if (!name.equals("")) {
            criteriaFound = true;
            gs.add(new SearchCriteria(NAME, name, null, SearchOperation.MATCH));
            gsAltName.add(new SearchCriteria(NAME, name, ALT_COMPANIES, SearchOperation.MATCH));
        }
        return Specification.where(gs.or(gsAltName));
    }

    public YCompanyDto findById(UUID id, HttpServletRequest request) {
        YCompany yCompany = yCompanyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(YCompany.class, id));
        YCompanyDto dto = yCompanyConverter.toDto(yCompany);
        User user = extractor.extractUser(request);
        Optional<YCompany> companyOptional = user.getCompanies()
                .stream()
                .filter(e -> e.getId() == id)
                .findAny();
        if (companyOptional.isPresent()) dto.setSubscribe(true);
        return dto;
    }
}
