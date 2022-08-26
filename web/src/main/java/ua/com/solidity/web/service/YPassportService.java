package ua.com.solidity.web.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ua.com.solidity.db.entities.YPassport;
import ua.com.solidity.db.repositories.YPassportRepository;
import ua.com.solidity.web.dto.olap.YPassportDto;
import ua.com.solidity.web.exception.EntityNotFoundException;
import ua.com.solidity.web.exception.FieldError;
import ua.com.solidity.web.exception.IllegalApiArgumentException;
import ua.com.solidity.web.service.converter.YPassportConverter;
import ua.com.solidity.web.service.validator.DataRegex;

@Slf4j
@RequiredArgsConstructor
@Service
public class YPassportService {

    private static final String DOMESTIC_PASSPORT = "UA_DOMESTIC";
    private static final String FOREIGN_PASSPORT = "UA_FOREIGN";
    private static final String IDCARD_PASSPORT = "UA_IDCARD";
    private static final String LATIN_LETTERS = "ABEKMHOPCTXY";
    private static final String CYRILLIC_LETTERS = "АВЕКМНОРСТХУ";
    private static final String NUMBER_FIELD = "number";
    private static final String SERIES_FIELD = "series";
    private static final String RECORD_NUMBER_FIELD = "recordNumber";

    private final YPassportRepository yPassportRepository;
    private final YPassportConverter converter;

    public YPassportDto findById(Long id) {
        Optional<YPassport> yPassport = yPassportRepository.findById(id);
        if (yPassport.isEmpty()) throw new EntityNotFoundException(YPassport.class, id);
        return converter.toDto(yPassport.get());
    }

    public void delete(Long id) {
        YPassport yPassport = yPassportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(YPassport.class, id));
        yPassport.cleanAssociations();
        yPassportRepository.save(yPassport);
        try {
            yPassportRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new EntityNotFoundException(YPassport.class, id);
        }
    }

    public void update(YPassportDto dto) {
        if (dto.getId() == null) throw new IllegalApiArgumentException("id shouldn't be empty");

        Optional<YPassport> yPassport = yPassportRepository.findById(dto.getId());
        if (yPassport.isEmpty()) throw new EntityNotFoundException(YPassport.class, dto.getId());

        Map<String, Runnable> passportTypeMap =
                Map.ofEntries(
                        Map.entry(DOMESTIC_PASSPORT, () -> {
                            validateDomesticPassport(dto.getNumber(), dto.getSeries());
                            setValidity(dto);
                            yPassportRepository.save(converter.toEntity(dto));
                        }),
                        Map.entry(FOREIGN_PASSPORT, () -> {
                            validateForeignPassport(dto.getNumber(), dto.getSeries(), dto.getRecordNumber());
                            setValidity(dto);
                            yPassportRepository.save(converter.toEntity(dto));
                        }),
                        Map.entry(IDCARD_PASSPORT, () -> {
                            validateIdPassport(dto.getNumber(), dto.getRecordNumber());
                            setValidity(dto);
                            yPassportRepository.save(converter.toEntity(dto));
                        })
                );

        passportTypeMap.entrySet()
                .stream()
                .filter(entry -> Objects.equals(entry.getKey(), dto.getType()))
                .findFirst()
                .ifPresentOrElse(entry -> entry.getValue().run(),
                                 () -> {
                                     throw new IllegalApiArgumentException("тип паспорта має бути UA_DOMESTIC, UA_FOREIGN або UA_IDCARD");
                                 });

    }

    public void setValidity(YPassportDto dto) {
        LocalDate endDate = dto.getEndDate();
        dto.setValidity(endDate != null && endDate.isAfter(LocalDateTime.now().toLocalDate()));
    }

    private void validateDomesticPassport(Integer number, String serial) {
        String passportNo = String.format("%06d", number);
        List<FieldError> fieldErrors = new ArrayList<>();
        List<String> messages = new ArrayList<>();

        if (!passportNo.matches(DataRegex.PASSPORT_NUMBER.getRegex())) {
            fieldErrors.add(new FieldError(NUMBER_FIELD,
                                           DataRegex.PASSPORT_NUMBER.getMessage()));
        }
        if (!transliterationToCyrillicLetters(serial).matches(DataRegex.DOMESTIC_PASSPORT_SERIES.getRegex())) {
            fieldErrors.add(new FieldError(SERIES_FIELD,
                                           DataRegex.DOMESTIC_PASSPORT_SERIES.getMessage()));
        }
        if (!fieldErrors.isEmpty()) {
            for (FieldError exception : fieldErrors) {
                String field = exception.getField();
                String message = exception.getMessage();
                messages.add(field + ": " + message);
            }
            throw new IllegalApiArgumentException(messages);
        }
    }

    private void validateForeignPassport(Integer number, String serial, String recordNumber) {
        String passportNo = String.format("%06d", number);
        List<FieldError> fieldErrors = new ArrayList<>();
        List<String> messages = new ArrayList<>();

        if (!passportNo.matches(DataRegex.PASSPORT_NUMBER.getRegex())) {
            fieldErrors.add(new FieldError(NUMBER_FIELD,
                                           DataRegex.PASSPORT_NUMBER.getMessage()));
        }
        if (!transliterationToCyrillicLetters(serial).matches(DataRegex.DOMESTIC_PASSPORT_SERIES.getRegex())) {
            fieldErrors.add(new FieldError(SERIES_FIELD,
                                           DataRegex.DOMESTIC_PASSPORT_SERIES.getMessage()));
        }
        if (!StringUtils.isBlank(recordNumber) && !recordNumber.matches(DataRegex.FOREIGN_PASSPORT_RECORD_NUMBER.getRegex())) {
            fieldErrors.add(new FieldError(RECORD_NUMBER_FIELD,
                                           DataRegex.FOREIGN_PASSPORT_RECORD_NUMBER.getMessage()));
        }
        if (!fieldErrors.isEmpty()) {
            for (FieldError exception : fieldErrors) {
                String field = exception.getField();
                String message = exception.getMessage();
                messages.add(field + ": " + message);
            }
            throw new IllegalApiArgumentException(messages);
        }
    }

    private void validateIdPassport(Integer number, String recordNumber) {
        String passportNo = String.format("%09d", number);
        List<FieldError> fieldErrors = new ArrayList<>();
        List<String> messages = new ArrayList<>();

        if (!passportNo.matches(DataRegex.ID_PASSPORT_NUMBER.getRegex())) {
            fieldErrors.add(new FieldError(NUMBER_FIELD,
                                           DataRegex.ID_PASSPORT_NUMBER.getMessage()));
        }
        if (!StringUtils.isBlank(recordNumber) && !recordNumber.matches(DataRegex.FOREIGN_PASSPORT_RECORD_NUMBER.getRegex())) {
            fieldErrors.add(new FieldError(RECORD_NUMBER_FIELD,
                                           DataRegex.FOREIGN_PASSPORT_RECORD_NUMBER.getMessage()));
        }
        if (!fieldErrors.isEmpty()) {
            for (FieldError exception : fieldErrors) {
                String field = exception.getField();
                String message = exception.getMessage();
                messages.add(field + ": " + message);
            }
            throw new IllegalApiArgumentException(messages);
        }
    }

    private String transliterationToCyrillicLetters(String serial) {
        StringBuilder cyrillicSerial = new StringBuilder();
        if (!StringUtils.isBlank(serial))
            for (int i = 0; i < serial.length(); i++) {
                int index = LATIN_LETTERS.indexOf(serial.charAt(i));
                if (index > -1) cyrillicSerial.append(CYRILLIC_LETTERS.charAt(index));
                else cyrillicSerial.append(serial.charAt(i));
            }
        return cyrillicSerial.toString();
    }

    private String transliterationToLatinLetters(String serial) {
        StringBuilder latinSerial = new StringBuilder();
        for (int i = 0; i < serial.length(); i++) {
            int index = CYRILLIC_LETTERS.indexOf(serial.charAt(i));
            if (index > -1) latinSerial.append(LATIN_LETTERS.charAt(index));
            else latinSerial.append(serial.charAt(i));
        }
        return latinSerial.toString();
    }
}
