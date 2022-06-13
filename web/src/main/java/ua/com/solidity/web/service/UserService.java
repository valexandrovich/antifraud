package ua.com.solidity.web.service;

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
import ua.com.solidity.db.repositories.UserRepository;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.web.dto.YPersonDto;
import ua.com.solidity.web.exception.EntityNotFoundException;
import ua.com.solidity.web.exception.IllegalApiArgumentException;
import ua.com.solidity.web.request.PaginationRequest;
import ua.com.solidity.web.security.service.Extractor;
import ua.com.solidity.web.service.converter.YPersonConverter;
import ua.com.solidity.web.service.factory.PageRequestFactory;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final Extractor extractor;
    private final YPersonConverter yPersonConverter;
    private final YPersonRepository yPersonRepository;
    private final UserRepository userRepository;
    private final PageRequestFactory pageRequestFactory;

    public Page<YPersonDto> subscriptions(PaginationRequest paginationRequest, HttpServletRequest request) {
        User user = extractor.extractUser(request);
        PageRequest pageRequest = pageRequestFactory.getPageRequest(paginationRequest);

        return yPersonRepository.findByUsers(user, pageRequest).map(p -> {
            YPersonDto yPersonDto = yPersonConverter.toDto(p);
            yPersonDto.setSubscribe(true);
            return yPersonDto;
        });
    }

    public void subscribe(UUID id, HttpServletRequest request) {
        User user = extractor.extractUser(request);
        Optional<YPerson> personOptional = user.getPeople()
                .stream()
                .filter(e -> e.getId().equals(id))
                .findAny();
        if (personOptional.isPresent()) throw new IllegalApiArgumentException("Ви вже підписалися на цю людину");
        YPerson yPerson = yPersonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(YPerson.class, id));
        user.getPeople().add(yPerson);
        userRepository.save(user);
    }

    public void unSubscribe(UUID id, HttpServletRequest request) {
        User user = extractor.extractUser(request);
        boolean removed = user.getPeople().removeIf(i -> i.getId().equals(id));
        if (!removed) throw new IllegalApiArgumentException("Ви не підписані на цю людину");
        userRepository.save(user);
    }
}
