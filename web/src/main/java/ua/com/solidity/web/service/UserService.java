package ua.com.solidity.web.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ua.com.solidity.db.entities.User;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.web.dto.YPersonDto;
import ua.com.solidity.web.request.PaginationRequest;
import ua.com.solidity.web.security.service.Extractor;
import ua.com.solidity.web.service.converter.YPersonConverter;
import ua.com.solidity.web.service.factory.PageRequestFactory;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

	private final Extractor extractor;
	private final YPersonConverter yPersonConverter;
	private final YPersonRepository yPersonRepository;
	private final PageRequestFactory pageRequestFactory;

	public Page<YPersonDto> subscriptions(PaginationRequest paginationRequest, HttpServletRequest request) {
		User user = extractor.extractUser(request);
		PageRequest pageRequest = pageRequestFactory.getPageRequest(paginationRequest);
	
		return yPersonRepository.findByUsers(user, pageRequest).map((p) -> {
		  YPersonDto yPersonDto = yPersonConverter.toDto(p);
		  yPersonDto.setSubscribe(true);
		  return yPersonDto;
		});
	  }
}
