package ua.com.solidity.web.utils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ua.com.solidity.web.dto.YPersonDto;

import java.util.List;

public class UtilPage {

	public static Page<YPersonDto> toPage(List<YPersonDto> list, int pageNo, int pageSize) {
		int totalPages = list.size() / pageSize;
		PageRequest pageable = PageRequest.of(pageNo, pageSize);

		int max = pageNo >= totalPages ? list.size() : pageSize * (pageNo + 1);
		int min = pageNo > totalPages ? max : pageSize * pageNo;

		return new PageImpl<>(list.subList(min, max),
		                      pageable,
		                      list.size());
	}
}
