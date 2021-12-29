package ua.com.solidity.otp.web.servise;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ua.com.solidity.db.entities.FileDescription;
import ua.com.solidity.db.entities.PhysicalPerson;
import ua.com.solidity.db.repositories.FileDescriptionRepository;
import ua.com.solidity.db.repositories.PhysicalPersonRepository;
import ua.com.solidity.otp.web.dto.PhysicalPersonDto;
import ua.com.solidity.otp.web.exception.EntityNotFoundException;
import ua.com.solidity.otp.web.request.SearchRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class XslxService {


    private final ModelMapper modelMapper;
    private final PhysicalPersonRepository personRepository;
    private final FileDescriptionRepository fileDescriptionRepository;

    public UUID upload(MultipartFile request) {
        log.debug("Attempting to parse file in XlsxService for uploading into DB.");
        UUID uuid = UUID.randomUUID();
        FileDescription fileDescription = new FileDescription();
        fileDescription.setUuid(uuid);
        fileDescription.setDescription("");
        fileDescriptionRepository.save(fileDescription);

        try (InputStream stream = request.getInputStream()) {
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
                while (cellIterator.hasNext()) {
                    Cell cell = (Cell) cellIterator.next();
                    int rowIndex = cell.getRowIndex();
                    int columnIndex = cell.getColumnIndex();
                    String value;
                    System.out.print("row:" + rowIndex + " index:" + columnIndex + " - ");

                    switch (cell.getCellType()) {
                        case STRING:
                            value = cell.getStringCellValue();
                            if (value.isBlank() || value.isEmpty()) value = null;

                            valuesMap.put(columnIndex, value);
                            System.out.print(cell.getStringCellValue() + "\t\t\t");
                            break;
                        case NUMERIC:
                            value = Double.toString(cell.getNumericCellValue());
                            if (value.isBlank() || value.isEmpty()) value = null;

                            valuesMap.put(columnIndex, value);
                            System.out.print(cell.getNumericCellValue() + "\t\t\t");
                            break;
                        case FORMULA:
                            value = cell.getStringCellValue();
                            if (value.isBlank() || value.isEmpty()) value = null;

                            valuesMap.put(columnIndex, value);
                            System.out.print(cell.getStringCellValue() + "\t\t\t");
                            break;
                        default:
                    }
                }
                savePerson(person, valuesMap);
                System.out.println("");
            }

        } catch (IOException e) {
            log.error("Opening an InputStream from xlsx file failed", e);
        }
        return uuid;
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
        List<PhysicalPersonDto> personList = personRepository.findByUuid(description).stream()
                .map(p -> modelMapper.map(p, PhysicalPersonDto.class))
                .collect(Collectors.toList());


        return personList;
    }

    public List<PhysicalPerson> search(SearchRequest searchRequest) {

        List<PhysicalPerson> persons = personRepository.findByNameUkEquals(searchRequest.getName());
//        List<PhysicalPerson> persons = personRepository.findBySurnameUkOrNameUkOrPatronymicUkOrSurnameRuOrNameRuOrPatronymicRuOrSurnameEnOrNameEnOrPatronymicEn(
//                searchRequest.getSurname(),
//                searchRequest.getName(),
//                searchRequest.getPatronymic(),
//                searchRequest.getSurname(),
//                searchRequest.getName(),
//                searchRequest.getPatronymic(),
//                searchRequest.getSurname(),
//                searchRequest.getName(),
//                searchRequest.getPatronymic());

        return persons;
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
