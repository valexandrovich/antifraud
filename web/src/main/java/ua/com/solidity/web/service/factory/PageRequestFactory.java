package ua.com.solidity.web.service.factory;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;
import ua.com.solidity.web.request.PaginationRequest;


@Component
public class PageRequestFactory {

	public PageRequest getPageRequest(PaginationRequest paginationRequest) {
		String[] properties = paginationRequest.getProperties();
		String direction = paginationRequest.getDirection();

		Sort sort = Sort.by(Direction.fromString(direction.toUpperCase()),
		                    properties);

		return PageRequest.of(paginationRequest.getPage(),
		                      paginationRequest.getSize(),
		                      sort);
	}
}
