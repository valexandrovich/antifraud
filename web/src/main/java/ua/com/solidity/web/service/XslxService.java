package ua.com.solidity.web.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ua.com.solidity.db.entities.FileDescription;
import ua.com.solidity.db.entities.PhysicalPerson;
import ua.com.solidity.db.repositories.FileDescriptionRepository;
import ua.com.solidity.db.repositories.PhysicalPersonRepository;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.web.dto.PhysicalPersonDto;
import ua.com.solidity.web.exception.EntityNotFoundException;
import ua.com.solidity.web.request.SearchRequest;
import ua.com.solidity.web.response.ResponseBodyWithUserName;
import ua.com.solidity.web.search.GenericSpecification;
import ua.com.solidity.web.search.SearchCriteria;
import ua.com.solidity.web.search.SearchOperation;
import ua.com.solidity.web.security.RequestHeaders;
import ua.com.solidity.web.security.service.JwtExtractor;
import ua.com.solidity.web.security.service.JwtUtilService;
import ua.com.solidity.web.security.token.JwtToken;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class XslxService {

    private final JwtUtilService jwtUtilService;
    private final JwtExtractor jwtExtractor;
    private final ModelMapper modelMapper;
    private final PhysicalPersonRepository personRepository;
    private final FileDescriptionRepository fileDescriptionRepository;
    private final YPersonRepository ypr;

    public ResponseBodyWithUserName<UUID> upload(MultipartFile multipartFile, HttpServletRequest request) {
        log.debug("Attempting to parse file in XlsxService for uploading into DB.");
        UUID uuid = UUID.randomUUID();
        String pattern = "dd.MM.yyyy hh:mm:ss";
        DateFormat df = new SimpleDateFormat(pattern);
        FileDescription fileDescription = new FileDescription();
        fileDescription.setUuid(uuid);
        fileDescription.setDescription("");

        String header = request.getHeader(RequestHeaders.HEADER_PARAM_JWT_TOKEN);
        String token = jwtExtractor.extract(header);
        String login = jwtUtilService.extractUserLogin(new JwtToken(token));

        fileDescription.setUserName(login);
        fileDescriptionRepository.save(fileDescription);

        int rowCount = 0;

        try (InputStream stream = multipartFile.getInputStream()) {
            XSSFWorkbook wb = new XSSFWorkbook(stream);
            XSSFSheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }
                Map<Integer, String> valuesMap = new HashMap<>();

                PhysicalPerson person = new PhysicalPerson();

                person.setUuid(fileDescription);


                Iterator<Cell> cellIterator = row.cellIterator();
                int cellCount = 0;
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    int columnIndex = cell.getColumnIndex();
                    String value;

                    switch (cell.getCellType()) {
                        case STRING:
                        case FORMULA:
                            value = cell.getStringCellValue();
                            if (value.isBlank() || value.isEmpty()) value = null;
                            else cellCount++;

                            valuesMap.put(columnIndex, value);
                            break;
                        case NUMERIC:

                            if (DateUtil.isCellDateFormatted(cell)) {
                                value = df.format(cell.getDateCellValue());
                            } else {
                                double d = cell.getNumericCellValue();
                                long l = (long) d;
                                value = Long.toString(l);
                            }

                            if (value.isBlank() || value.isEmpty()) value = null;
                            else cellCount++;

                            valuesMap.put(columnIndex, value);
                            break;
                        default:
                    }
                }
                if (cellCount > 0) {
                    rowCount++;
                    savePerson(person, valuesMap);
                }
            }

        } catch (IOException e) {
            log.error("Opening an InputStream from xlsx file failed", e);
        }
        FileDescription fileDescription1 = fileDescriptionRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException(FileDescription.class, uuid));
        fileDescription1.setRowCount(rowCount);
        fileDescriptionRepository.save(fileDescription1);
        return new ResponseBodyWithUserName<>(uuid, login, HttpStatus.OK.value(), "Uploaded");
    }

    public void upload(UUID uuid, String description) {
        FileDescription fileDescription = fileDescriptionRepository.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException(FileDescription.class, uuid));
        fileDescription.setDescription(description);
        fileDescriptionRepository.save(fileDescription);
    }

    public List<FileDescription> getUploaded() {
        return fileDescriptionRepository.findAll();
    }

    public List<PhysicalPersonDto> getUploaded(UUID uuid) {
        FileDescription description = fileDescriptionRepository.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException(FileDescription.class, uuid.toString()));

        return personRepository.findByUuid(description).stream()
                .map(p -> modelMapper.map(p, PhysicalPersonDto.class))
                .collect(Collectors.toList());
    }

    public List<YPerson> search(SearchRequest searchRequest) {
        boolean criteriaFound = false;
        GenericSpecification<YPerson> gs = new GenericSpecification<>();

        String firstName = Objects.toString(searchRequest.getName(),""); // Protection from null
        if (!firstName.equals("")) {
            criteriaFound = true;
            gs.add(new SearchCriteria("firstName", firstName, SearchOperation.EQUALS));
        }

        String surName = Objects.toString(searchRequest.getSurname(),"");
        if (!surName.equals("")) {
            criteriaFound = true;
            gs.add(new SearchCriteria("lastName", surName, SearchOperation.EQUALS));
        }

        String patName = Objects.toString(searchRequest.getPatronymic(),"");
        if (!patName.equals("")) {
            criteriaFound = true;
            gs.add(new SearchCriteria("patName", patName, SearchOperation.EQUALS));
        }

        String year = Objects.toString(searchRequest.getYear(),"");
        String month = Objects.toString(searchRequest.getMonth(),"");
        String day = Objects.toString(searchRequest.getDay(),"");
        if (!year.equals("") && !month.equals("") && !day.equals("")) {
            criteriaFound = true;
            gs.add(new SearchCriteria("birthdate",
                    LocalDate.of(
                            Integer.parseInt(year),
                            Integer.parseInt(month),
                            Integer.parseInt(day)),
                    SearchOperation.EQUALS));
        }

        if (criteriaFound) {
            return ypr.findAll(gs);
        } else {
            return new ArrayList<>();
        }
    }

    private void savePerson(PhysicalPerson person, Map<Integer, String> values) {
        person.setCustomer(values.get(0));
        person.setSurnameUk(values.get(1));
        person.setNameUk(values.get(2));
        person.setPatronymicUk(values.get(3));
        person.setSurnameRu(values.get(4));
        person.setNameRu(values.get(5));
        person.setPatronymicRu(values.get(6));
        person.setSurnameEn(values.get(7));
        person.setNameEn(values.get(8));
        person.setPatronymicEn(values.get(9));
        person.setBirthDay(values.get(10));
        person.setINN(values.get(11));
        person.setLocalPassportCode(values.get(12));
        person.setLocalPassportSeries(values.get(13));
        person.setLocalPassportAuthority(values.get(14));
        person.setLocalPassportDate(values.get(15));
        person.setForeignPassportNumber(values.get(16));
        person.setForeignPassportRecordNumber(values.get(17));
        person.setForeignPassportAuthority(values.get(18));
        person.setForeignPassportDate(values.get(19));
        person.setIdPassportNumber(values.get(20));
        person.setIdPassportRecordNumber(values.get(21));
        person.setIdPassportAuthority(values.get(22));
        person.setIdPassportDate(values.get(23));
        person.setDeathTag(values.get(24));
        person.setDeathDate(values.get(25));
        person.setDeathNotificationDate(values.get(26));
        person.setDeathNotificationSource(values.get(27));
        person.setBlackListTagN(values.get(28));
        person.setBlackListDateFromN(values.get(29));
        person.setBlackListDateToN(values.get(30));
        person.setEllipsis(values.get(31));
        person.setComment(values.get(32));
        person.setCitizenship(values.get(33));
        person.setLivingAddress(values.get(34));
        person.setPhoneNumber(values.get(35));
        person.setEmail(values.get(36));
        person.setBirthPlace(values.get(37));
        person.setSex(values.get(38));
        person.setSensitiveInformationTag(values.get(39));
        person.setRelationTag(values.get(40));
        person.setBankProducts(values.get(41));

        personRepository.save(person);
    }
}
