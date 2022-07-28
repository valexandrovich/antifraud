package ua.com.solidity.web.service;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ua.com.solidity.db.entities.User;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.web.dto.olap.YPersonDto;
import ua.com.solidity.web.dto.olap.YPersonSearchDto;
import ua.com.solidity.web.exception.EntityNotFoundException;
import ua.com.solidity.web.request.PaginationRequest;
import ua.com.solidity.web.request.PaginationSearchRequest;
import ua.com.solidity.web.request.SearchRequest;
import ua.com.solidity.web.search.GenericSpecification;
import ua.com.solidity.web.search.SearchCriteria;
import ua.com.solidity.web.search.SearchOperation;
import ua.com.solidity.web.security.service.Extractor;
import ua.com.solidity.web.service.converter.YPersonConverter;
import ua.com.solidity.web.service.factory.PageRequestFactory;
import ua.com.solidity.common.UtilString;

@Slf4j
@RequiredArgsConstructor
@Service
public class YPersonService {
	private final Extractor extractor;
	private final YPersonRepository ypr;
	private final YPersonConverter yPersonConverter;
	private final PageRequestFactory pageRequestFactory;

	private static final String ALT_PEOPLE = "altPeople";
	private static final String PASSPORTS = "passports";
	private static final String NUMBER = "number";
	private static final String BIRTHDATE = "birthdate";
	private static final String RECORD_NUMBER = "recordNumber";
	private static final String LAST_NAME = "lastName";
	private static final String FIRST_NAME = "firstName";
	private static final String PAT_NAME = "patName";
	private static final String ADDRESS = "address";
	private static final String SERIES = "series";
	private static final String INN = "inn";
	private static final String INNS = "inns";
	private static final String ADDRESSES = "addresses";
	private static final String PHONE = "phone";
	private static final String PHONES = "phones";
	boolean criteriaFound;


	public Page<YPersonSearchDto> search(PaginationSearchRequest paginationSearchRequest,
	                                         HttpServletRequest httpServletRequest) {
		PaginationRequest paginationRequest = paginationSearchRequest.getPaginationRequest();
		SearchRequest searchRequest = paginationSearchRequest.getSearchRequest();

		GenericSpecification<YPerson> gs = new GenericSpecification<>();
		criteriaFound = false;

		searchByName(searchRequest, gs, paginationRequest);

		String year = Objects.toString(searchRequest.getYear(), "");
		String month = Objects.toString(searchRequest.getMonth(), "");
		String day = Objects.toString(searchRequest.getDay(), "");
		if (!year.equals("") && !month.equals("") && !day.equals("")) {
			criteriaFound = true;
			gs.add(new SearchCriteria(BIRTHDATE,
			                          LocalDate.of(
					                          Integer.parseInt(year),
					                          Integer.parseInt(month),
					                          Integer.parseInt(day)),
			                          null,
			                          SearchOperation.EQUALS));
		}

		String inn = Objects.toString(searchRequest.getInn(), "");
		if (!inn.equals("")) {
			criteriaFound = true;
			gs.add(new SearchCriteria(INN, inn, INNS, SearchOperation.EQUALS));
		}

		searchByPassport(searchRequest, gs);

		String address = Objects.toString(UtilString.toUpperCase(searchRequest.getAddress()), "");
		if (!address.equals("")) {
			criteriaFound = true;
			gs.add(new SearchCriteria(ADDRESS, address, ADDRESSES, SearchOperation.MATCH));
		}

		String age = Objects.toString(searchRequest.getAge(), "");
		if (!age.equals("")) {
			criteriaFound = true;
			LocalDate finishDate = LocalDate.now().minusYears(Integer.parseInt(age));
			gs.add(new SearchCriteria(BIRTHDATE, finishDate, null, SearchOperation.BETWEEN));
		}

		String phone = Objects.toString(searchRequest.getPhone(), "");
		if (!phone.equals("")) {
			criteriaFound = true;
			gs.add(new SearchCriteria(PHONE, phone, PHONES, SearchOperation.MATCH));
		}

		PageRequest pageRequest = pageRequestFactory.getPageRequest(paginationRequest);
		if (criteriaFound) {
			User user = extractor.extractUser(httpServletRequest);
			return ypr.findAll(gs, pageRequest)
					.map(entity -> {
						YPersonSearchDto dto = yPersonConverter.toSearchDto(entity);
						user.getPersonSubscriptions().forEach(subscribedYPerson -> {
							if (subscribedYPerson.getId() == dto.getId()) dto.setSubscribe(true);
						});
						return dto;
					});
		} else {
			return Page.empty(pageRequest);
		}
	}

	private void searchByName(SearchRequest searchRequest,
                              GenericSpecification<YPerson> gs,
                              PaginationRequest paginationRequest) {
		String firstName = Objects.toString(searchRequest.getName().toUpperCase().trim(), ""); // Protection from null
        if (!firstName.equals("")) {
            criteriaFound = true;
            gs.add(new SearchCriteria(FIRST_NAME, firstName, null, SearchOperation.EQUALS));
        }

        String surName = Objects.toString(searchRequest.getSurname().toUpperCase().trim(), "");
        if (!surName.equals("")) {
            criteriaFound = true;
            gs.add(new SearchCriteria(LAST_NAME, surName, null, SearchOperation.EQUALS));
        }

        String patName = Objects.toString(searchRequest.getPatronymic().toUpperCase().trim(), "");
        if (!patName.equals("")) {
            criteriaFound = true;
            gs.add(new SearchCriteria(PAT_NAME, patName, null, SearchOperation.EQUALS));
        }
        if (criteriaFound) {
            PageRequest pageRequest = pageRequestFactory.getPageRequest(paginationRequest);
            if (ypr.findAll(gs, pageRequest).isEmpty()) {
                gs.clear();
                if (!firstName.equals("")) {
                    criteriaFound = true;
                    gs.add(new SearchCriteria(FIRST_NAME, firstName, ALT_PEOPLE, SearchOperation.EQUALS));
                }

                if (!surName.equals("")) {
                    criteriaFound = true;
                    gs.add(new SearchCriteria(LAST_NAME, surName, ALT_PEOPLE, SearchOperation.EQUALS));
                }

                if (!patName.equals("")) {
                    criteriaFound = true;
                    gs.add(new SearchCriteria(PAT_NAME, patName, ALT_PEOPLE, SearchOperation.EQUALS));
                }
            }
        }
	}

	private void searchByPassport(SearchRequest searchRequest, GenericSpecification<YPerson> gs) {
		String passportNumber = Objects.toString(searchRequest.getPassportNumber(), "");
		String passportSeries = Objects.toString(searchRequest.getPassportSeria(), "");
		if (!passportNumber.equals("") && !passportSeries.equals("")) {
			criteriaFound = true;
			gs.add(new SearchCriteria(NUMBER, passportNumber, PASSPORTS, SearchOperation.EQUALS));
			gs.add(new SearchCriteria(SERIES, passportSeries.toUpperCase(), PASSPORTS, SearchOperation.EQUALS));
		}

		String idPassportNumber = Objects.toString(searchRequest.getId_documentNumber(), "");
		if (!idPassportNumber.equals("")) {
			criteriaFound = true;
			gs.add(new SearchCriteria(NUMBER, idPassportNumber, PASSPORTS, SearchOperation.EQUALS));
		}

		String idPassportRecord = Objects.toString(searchRequest.getId_registryNumber(), "");
		if (!idPassportRecord.equals("")) {
			criteriaFound = true;
			gs.add(new SearchCriteria(RECORD_NUMBER, idPassportRecord, PASSPORTS, SearchOperation.EQUALS));
		}

		String foreignPassportNumber = Objects.toString(searchRequest.getForeignP_documentNumber(), "");
		if (!foreignPassportNumber.equals("")) {
			criteriaFound = true;
			gs.add(new SearchCriteria(SERIES, foreignPassportNumber.substring(0, 2).toUpperCase(), PASSPORTS, SearchOperation.EQUALS));
			gs.add(new SearchCriteria(NUMBER, foreignPassportNumber.substring(2), PASSPORTS, SearchOperation.EQUALS));
		}

		String foreignPassportRecord = Objects.toString(searchRequest.getForeignP_registryNumber(), "");
		if (!foreignPassportRecord.equals("")) {
			criteriaFound = true;
			gs.add(new SearchCriteria(RECORD_NUMBER, foreignPassportRecord, PASSPORTS, SearchOperation.EQUALS));
		}
	}

	public YPersonDto findById(UUID id, HttpServletRequest request) {
		YPerson yPerson = ypr.findById(id)
				.orElseThrow(() -> new EntityNotFoundException(YPerson.class, id));
		YPersonDto dto = yPersonConverter.toDto(yPerson);
		User user = extractor.extractUser(request);
		Optional<YPerson> personOptional = user.getPersonSubscriptions()
				.stream()
				.filter(e -> e.getId() == id)
				.findAny();
		if (personOptional.isPresent()) dto.setSubscribe(true);
		return dto;
	}
}
