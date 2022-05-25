package ua.com.solidity.web.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import ua.com.solidity.db.entities.User;
import ua.com.solidity.web.dto.YPersonDto;
import ua.com.solidity.web.security.service.Extractor;
import ua.com.solidity.web.service.converter.YPersonConverter;
import ua.com.solidity.web.utils.UtilPage;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

	private final Extractor extractor;
	private final YPersonConverter yPersonConverter;

	public Page<YPersonDto> subscriptions(int pageNo, int pageSize, HttpServletRequest request) {
		User user = extractor.extractUser(request);
		List<YPersonDto> people = user.getPeople()
				.stream()
				.map(yPersonConverter::toDto)
				.collect(Collectors.toList());

		return UtilPage.toPage(people, pageNo, pageSize);
	}
}
