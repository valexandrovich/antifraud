package ua.com.solidity.web.service;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.db.entities.YPassport;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.repositories.YINNRepository;
import ua.com.solidity.web.dto.olap.YINNDto;
import ua.com.solidity.web.exception.EntityNotFoundException;
import ua.com.solidity.web.service.converter.YINNConverter;
import ua.com.solidity.web.service.validator.DataRegex;

@Slf4j
@RequiredArgsConstructor
@Service
public class YInnService {

    public static final LocalDate START_DATE = LocalDate.of(1900, 1, 1);

    private final YINNRepository yinnRepository;
    private final YINNConverter converter;

    public YINNDto findById(Long id) {
        Optional<YINN> yPassport = yinnRepository.findById(id);
        if (yPassport.isEmpty()) throw new EntityNotFoundException(YINN.class, id);
        return converter.toDto(yPassport.get());
    }
    public void delete(Long id) {
        YINN yinn = yinnRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(YPassport.class, id));
        yinn.cleanAssociations();
        yinnRepository.save(yinn);
        try {
            yinnRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new EntityNotFoundException(YPassport.class, id);
        }
    }

    public static boolean isValidInn(Long inn, LocalDate birthDay) {
        String innFormated = String.format("%010d", inn);
        if (innFormated.matches(DataRegex.INN.getRegex())) {
            boolean isValidBirthDateInn = birthDay == null ||
                    Objects.equals(String.valueOf(birthDay.toEpochDay()
                                                          - START_DATE.toEpochDay() + 1L), innFormated.substring(0, 5));
            int controlNumber = ((-1 * Integer.parseInt(String.valueOf(innFormated.charAt(0)))
                    + 5 * Integer.parseInt(String.valueOf(innFormated.charAt(1)))
                    + 7 * Integer.parseInt(String.valueOf(innFormated.charAt(2)))
                    + 9 * Integer.parseInt(String.valueOf(innFormated.charAt(3)))
                    + 4 * Integer.parseInt(String.valueOf(innFormated.charAt(4)))
                    + 6 * Integer.parseInt(String.valueOf(innFormated.charAt(5)))
                    + 10 * Integer.parseInt(String.valueOf(innFormated.charAt(6)))
                    + 5 * Integer.parseInt(String.valueOf(innFormated.charAt(7)))
                    + 7 * Integer.parseInt(String.valueOf(innFormated.charAt(8)))) % 11) % 10;
            return Objects.equals(Integer.parseInt(String.valueOf(innFormated.charAt(9))), controlNumber)
                    && isValidBirthDateInn;
        }
        return false;
    }

    private void addBirthdayByInn(YPerson person) {
        Optional<YINN> optionalYINN = person.getInns().parallelStream().findFirst();
        if (optionalYINN.isPresent()) {
            String inn = String.format(DataRegex.INN.getRegex(), optionalYINN.get().getInn());
            LocalDate birthDay = LocalDate.ofEpochDay(Long.parseLong(inn.substring(0, 5)) + START_DATE.toEpochDay() - 1L);
            person.setBirthdate(birthDay);
        }
    }
}
