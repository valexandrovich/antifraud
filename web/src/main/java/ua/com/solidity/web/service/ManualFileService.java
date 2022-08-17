package ua.com.solidity.web.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ua.com.solidity.common.OtpExchange;
import ua.com.solidity.common.UtilString;
import ua.com.solidity.common.Utils;
import ua.com.solidity.common.model.EnricherPortionMessage;
import ua.com.solidity.db.entities.FileDescription;
import ua.com.solidity.db.entities.ManualCTag;
import ua.com.solidity.db.entities.ManualCompany;
import ua.com.solidity.db.entities.ManualFileType;
import ua.com.solidity.db.entities.ManualPerson;
import ua.com.solidity.db.entities.ManualTag;
import ua.com.solidity.db.repositories.FileDescriptionRepository;
import ua.com.solidity.db.repositories.ManualCTagRepository;
import ua.com.solidity.db.repositories.ManualCompanyRepository;
import ua.com.solidity.db.repositories.ManualFileTypeRepository;
import ua.com.solidity.db.repositories.ManualPersonRepository;
import ua.com.solidity.db.repositories.ManualTagRepository;
import ua.com.solidity.db.repositories.TagTypeRepository;
import ua.com.solidity.db.repositories.YCompanyRoleRepository;
import ua.com.solidity.db.repositories.YCompanyStateRepository;
import ua.com.solidity.web.dto.dynamicfile.ManualCompanyDto;
import ua.com.solidity.web.dto.dynamicfile.ManualPersonDto;
import ua.com.solidity.web.dto.olap.CompanyRoleDto;
import ua.com.solidity.web.dto.olap.CompanyStateDto;
import ua.com.solidity.web.dto.olap.TagTypeDto;
import ua.com.solidity.web.exception.EntityNotFoundException;
import ua.com.solidity.web.exception.IllegalApiArgumentException;
import ua.com.solidity.web.response.ValidatedManualCompanyResponse;
import ua.com.solidity.web.response.ValidatedManualPersonResponse;
import ua.com.solidity.web.response.secondary.ManualCompanyStatus;
import ua.com.solidity.web.response.secondary.ManualPersonStatus;
import ua.com.solidity.web.response.secondary.ManualTagStatus;
import ua.com.solidity.web.security.service.Extractor;
import ua.com.solidity.web.service.converter.ManualCompanyConverter;
import ua.com.solidity.web.service.converter.ManualPersonConverter;
import ua.com.solidity.web.service.converter.YCompanyConverter;
import ua.com.solidity.web.service.converter.YTagConverter;
import ua.com.solidity.web.service.dynamicfile.ColumnNameJuridicalFile;
import ua.com.solidity.web.service.dynamicfile.ColumnNamePhysicalFile;
import ua.com.solidity.web.service.validator.CompanyValidator;
import ua.com.solidity.web.service.validator.ManualTagValidator;
import ua.com.solidity.web.service.validator.PersonValidator;

@Slf4j
@RequiredArgsConstructor
@Service
public class ManualFileService {
    private final Extractor extractor;
    private final ManualPersonConverter manualPersonConverter;
    private final ManualCompanyConverter manualCompanyConverter;
    private final FileDescriptionRepository fileDescriptionRepository;
    private final ManualPersonRepository manualPersonRepository;
    private final ManualTagRepository manualTagRepository;
    private final ManualTagValidator manualTagValidator;
    private final ManualCTagRepository manualCTagRepository;
    private final ManualCompanyRepository manualCompanyRepository;
    private final ManualFileTypeRepository manualFileTypeRepository;
    private final CompanyValidator companyValidator;
    private final TagTypeRepository tagTypeRepository;
    private final YTagConverter tagConverter;
    private final YCompanyStateRepository companyStateRepository;
    private final YCompanyConverter companyConverter;
    private final YCompanyRoleRepository companyRoleRepository;
    private int count;

    private static final String MANUAL_PERSON = "manual_person";
    private static final String MANUAL_COMPANY = "manual_company";
    private static final String PHYSICAL = "PHYSICAL";
    private static final String JURIDICAL = "JURIDICAL";

    private final AmqpTemplate template;

    public void upload(UUID uuid, String description) {
        FileDescription fileDescription = fileDescriptionRepository.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException(FileDescription.class, uuid));
        fileDescription.setDescription(description);
        fileDescriptionRepository.save(fileDescription);
    }

    public List<FileDescription> getUploaded() {
        return fileDescriptionRepository.findByValidated(true).stream()
                .filter(f -> f.getDescription() != null
                        && !f.getDescription().isBlank())
                .collect(Collectors.toList());
    }

    public UUID uploadDynamicFile(MultipartFile multipartFile, String delimiter, String code, String type, HttpServletRequest request) {
        UUID uuid = UUID.randomUUID();
        FileDescription fileDescription = new FileDescription();
        fileDescription.setUuid(uuid);
        Optional<ManualFileType> typeOptional = manualFileTypeRepository.findByName(UtilString.toUpperCase(type));
        typeOptional.ifPresent(fileDescription::setType);
        fileDescription.setDescription("");

        String login = extractor.extractLogin(request);

        fileDescription.setUserName(login);
        fileDescriptionRepository.save(fileDescription);

        String fileType = "";
        count = 0;

        String fileName = UtilString.toLowerCase(multipartFile.getOriginalFilename());
        if (!StringUtils.isBlank(fileName))
            fileType = fileName.substring(fileName.lastIndexOf(".") + 1);

        if (!StringUtils.isBlank(fileType) && fileType.equals("xlsx")) {
            parseXlsx(multipartFile, fileDescription);
        } else if (!StringUtils.isBlank(fileType) && (fileType.equals("csv") || fileType.equals("txt"))) {
            parseCsv(multipartFile, delimiter, code, fileDescription);
        }
        fileDescription.setRowCount(count);
        fileDescriptionRepository.save(fileDescription);
        if (fileDescription.getType().getName().equals(PHYSICAL))
            getUploadedManualPerson(fileDescription.getUuid());
        else if (fileDescription.getType().getName().equals(JURIDICAL))
            getUploadedManualCompany(fileDescription.getUuid());
        return uuid;
    }

    private List<Integer> findDuplicateColumn(String column, List<String> header) {
        return IntStream.range(0, header.size())
                .filter(i -> header.get(i).equals(column))
                .boxed().collect(Collectors.toList());
    }

    private void parseCsv(MultipartFile file, String delimiter, String code, FileDescription fileDescription) {
        CSVFormat.Builder builder = CSVFormat.Builder.create();
        builder.setSkipHeaderRecord(true).setDelimiter(delimiter)
                .setHeader()
                .setIgnoreEmptyLines(true)
                .setTrim(true);
        try (InputStream stream = file.getInputStream()) {
            CSVParser parser = CSVParser.parse(stream, Charset.forName(code), builder.build());
            List<String> header = new ArrayList<>(parser.getHeaderNames());
            header.set(0, header.get(0).substring(1));

            addWrongColumnFile(fileDescription, header);
            if (fileDescription.getWrongColumn().length() == 0) {
                for (CSVRecord parserLine : parser) {
                    List<String> line = new ArrayList<>();
                    parserLine.forEach(l -> {
                        if (l != null && l.length() > 255)
                            line.add(l.substring(0, 252) + "...");
                        else
                            line.add(l);
                    });
                    if (fileDescription.getType().getName().equals(PHYSICAL))
                        addManualPerson(line, header, fileDescription);
                    else if (fileDescription.getType().getName().equals(JURIDICAL))
                        addManualCompany(line, header, fileDescription);
                }
            }
        } catch (IOException e) {
            log.error("Opening an InputStream from csv file " + file.getOriginalFilename() + " failed", e);
        }
    }

    private void parseXlsx(MultipartFile file, FileDescription fileDescription) {
        try (InputStream stream = file.getInputStream()) {
            XSSFWorkbook wb = new XSSFWorkbook(stream);
            XSSFSheet sheet = wb.getSheetAt(0);
            List<String> header = new ArrayList<>();
            List<String> line = new ArrayList<>();
            int i = -1;
            while (sheet.getRow(i) == null && i != sheet.getLastRowNum()) i++;
            if (sheet.getLastRowNum() > 0)
                for (Cell cell : sheet.getRow(i))
                    header.add(cell.toString());

            addWrongColumnFile(fileDescription, header);

            if (fileDescription.getWrongColumn().length() == 0 && sheet.getLastRowNum() > 0) {
                for (int j = i + 1; j <= sheet.getLastRowNum(); j++) {
                    if (sheet.getRow(j) != null)
                        checkXlsxFieldFormat(line, header, sheet.getRow(j), fileDescription);
                }
            }
        } catch (IOException e) {
            log.error("Opening an InputStream from xlsx file " + file.getOriginalFilename() + " failed", e);
        }
    }

    private void checkXlsxFieldFormat(List<String> line, List<String> header, Row row, FileDescription file) {
        String pattern = "dd.MM.yyyy";
        DateFormat df = new SimpleDateFormat(pattern);
        line.clear();
        for (int j = 0; j < header.size(); j++) {
            Cell cell = row.getCell(j);
            if (cell != null && cell.getCellType().equals(CellType.NUMERIC)
                    && DateUtil.isCellDateFormatted(cell)) {
                line.add(df.format(cell.getDateCellValue()));
            } else if (cell != null &&
                    cell.getCellType().equals(CellType.NUMERIC)) {
                line.add(Objects.toString(Math.round(cell.getNumericCellValue()), ""));
            } else if (cell != null && Objects.toString(cell, "").length() > 255) {
                line.add(Objects.toString(cell, "").substring(0, 252) + "...");
            } else line.add(Objects.toString(cell, ""));
        }
        if (file.getType().getName().equals(PHYSICAL))
            addManualPerson(line, header, file);
        else if (file.getType().getName().equals(JURIDICAL))
            addManualCompany(line, header, file);
    }

    private void addManualPerson(List<String> line, List<String> header, FileDescription file) {
        ManualPerson person = new ManualPerson();
        person.setUuid(file);
        var isEmpty = new Object() {
            boolean state = true;
        };
        List<ObjectFieldSetter> executorList = List.of(
                ObjectFieldSetter.of(ColumnNamePhysicalFile.CNUM.name(), person::setCnum),
                ObjectFieldSetter.of(ColumnNamePhysicalFile.LNAME_UK.name(), person::setLnameUk),
                ObjectFieldSetter.of(ColumnNamePhysicalFile.FNAME_UK.name(), person::setFnameUk),
                ObjectFieldSetter.of(ColumnNamePhysicalFile.PNAME_UK.name(), person::setPnameUk),
                ObjectFieldSetter.of(ColumnNamePhysicalFile.LNAME_RU.name(), person::setLnameRu),
                ObjectFieldSetter.of(ColumnNamePhysicalFile.FNAME_RU.name(), person::setFnameRu),
                ObjectFieldSetter.of(ColumnNamePhysicalFile.PNAME_RU.name(), person::setPnameRu),
                ObjectFieldSetter.of(ColumnNamePhysicalFile.LNAME_EN.name(), person::setLnameEn),
                ObjectFieldSetter.of(ColumnNamePhysicalFile.FNAME_EN.name(), person::setFnameEn),
                ObjectFieldSetter.of(ColumnNamePhysicalFile.PNAME_EN.name(), person::setPnameEn),
                ObjectFieldSetter.of(ColumnNamePhysicalFile.BIRTHDAY.name(), person::setBirthday),
                ObjectFieldSetter.of(ColumnNamePhysicalFile.OKPO.name(), person::setOkpo),
                ObjectFieldSetter.of(ColumnNamePhysicalFile.COUNTRY.name(), person::setCountry),
                ObjectFieldSetter.of(ColumnNamePhysicalFile.ADDRESS.name(), person::setAddress),
                ObjectFieldSetter.of(ColumnNamePhysicalFile.PHONE.name(), person::setPhone),
                ObjectFieldSetter.of(ColumnNamePhysicalFile.EMAIL.name(), person::setEmail),
                ObjectFieldSetter.of(ColumnNamePhysicalFile.BIRTH_PLACE.name(), person::setBirthPlace),
                ObjectFieldSetter.of(ColumnNamePhysicalFile.SEX.name(), person::setSex),
                ObjectFieldSetter.of(ColumnNamePhysicalFile.COMMENT.name(), person::setComment),
                ObjectFieldSetter.of(ColumnNamePhysicalFile.PASS_LOCAL_NUM.name(), person::setPassLocalNum),
                ObjectFieldSetter.of(ColumnNamePhysicalFile.PASS_LOCAL_SERIAL.name(), person::setPassLocalSerial),
                ObjectFieldSetter.of(ColumnNamePhysicalFile.PASS_LOCAL_ISSUER.name(), person::setPassLocalIssuer),
                ObjectFieldSetter.of(ColumnNamePhysicalFile.PASS_LOCAL_ISSUE_DATE.name(), person::setPassLocalIssueDate),
                ObjectFieldSetter.of(ColumnNamePhysicalFile.PASS_INT_NUM.name(), person::setPassIntNum),
                ObjectFieldSetter.of(ColumnNamePhysicalFile.PASS_INT_REC_NUM.name(), person::setPassIntRecNum),
                ObjectFieldSetter.of(ColumnNamePhysicalFile.PASS_INT_ISSUER.name(), person::setPassIntIssuer),
                ObjectFieldSetter.of(ColumnNamePhysicalFile.PASS_INT_ISSUE_DATE.name(), person::setPassIntIssueDate),
                ObjectFieldSetter.of(ColumnNamePhysicalFile.PASS_ID_NUM.name(), person::setPassIdNum),
                ObjectFieldSetter.of(ColumnNamePhysicalFile.PASS_ID_REC_NUM.name(), person::setPassIdRecNum),
                ObjectFieldSetter.of(ColumnNamePhysicalFile.PASS_ID_ISSUER.name(), person::setPassIdIssuer),
                ObjectFieldSetter.of(ColumnNamePhysicalFile.PASS_ID_ISSUE_DATE.name(), person::setPassIdIssueDate)
        );
        executorList.forEach(executor -> {
            String column = executor.getColumn();
            if (header.contains(column) && !StringUtils.isBlank(line.get(header.indexOf(column)))) {
                String value = line.get(header.indexOf(column)).replace("\"", "");
                executor.getFieldSetter().accept(UtilString.toUpperCase(value).trim());
                isEmpty.state = false;
            }
        });
        String column = ColumnNamePhysicalFile.MK_ID.name();
        Set<ManualTag> tags = addManualTag(line, header, person, column, file);
        if (!tags.isEmpty()) {
            person.setTags(tags);
            isEmpty.state = false;
        }
        if (!isEmpty.state) {
            manualPersonRepository.save(person);
            count++;
        }
    }

    private void addManualCompany(List<String> line, List<String> header, FileDescription file) {
        ManualCompany company = new ManualCompany();
        company.setUuid(file);
        var isEmpty = new Object() {
            boolean state = true;
        };
        List<ObjectFieldSetter> executorList = List.of(
                ObjectFieldSetter.of(ColumnNameJuridicalFile.CNUM.name(), company::setCnum),
                ObjectFieldSetter.of(ColumnNameJuridicalFile.NAME.name(), company::setName),
                ObjectFieldSetter.of(ColumnNameJuridicalFile.NAME_EN.name(), company::setNameEn),
                ObjectFieldSetter.of(ColumnNameJuridicalFile.SHORT_NAME.name(), company::setShortName),
                ObjectFieldSetter.of(ColumnNameJuridicalFile.EDRPOU.name(), company::setEdrpou),
                ObjectFieldSetter.of(ColumnNameJuridicalFile.PDV.name(), company::setPdv),
                ObjectFieldSetter.of(ColumnNameJuridicalFile.ADDRESS.name(), company::setAddress),
                ObjectFieldSetter.of(ColumnNameJuridicalFile.STATE.name(), company::setState),
                ObjectFieldSetter.of(ColumnNameJuridicalFile.LNAME.name(), company::setLname),
                ObjectFieldSetter.of(ColumnNameJuridicalFile.FNAME.name(), company::setFname),
                ObjectFieldSetter.of(ColumnNameJuridicalFile.PNAME.name(), company::setPname),
                ObjectFieldSetter.of(ColumnNameJuridicalFile.INN_RELATION_PERSON.name(), company::setInn),
                ObjectFieldSetter.of(ColumnNameJuridicalFile.TYPE_RELATION_PERSON.name(), company::setTypeRelationPerson),
                ObjectFieldSetter.of(ColumnNameJuridicalFile.CNAME.name(), company::setCname),
                ObjectFieldSetter.of(ColumnNameJuridicalFile.EDRPOU_RELATION_COMPANY.name(), company::setEdrpouRelationCompany),
                ObjectFieldSetter.of(ColumnNameJuridicalFile.TYPE_RELATION_COMPANY.name(), company::setTypeRelationCompany)
        );
        executorList.forEach(executor -> {
            String column = executor.getColumn();
            if (header.contains(column) && !StringUtils.isBlank(line.get(header.indexOf(column)))) {
                String value = line.get(header.indexOf(column)).replace("\"", "");
                executor.getFieldSetter().accept(UtilString.toUpperCase(value).trim());
                isEmpty.state = false;
            }
        });
        String column = ColumnNameJuridicalFile.MK_ID.name();
        Set<ManualCTag> tags = addManualCTag(line, header, company, column, file);
        if (!tags.isEmpty()) {
            company.setTags(tags);
            isEmpty.state = false;
        }
        if (!isEmpty.state) {
            manualCompanyRepository.save(company);
            count++;
        }
    }

    private Set<ManualTag> addManualTag(List<String> line, List<String> header, ManualPerson person,
                                        String column, FileDescription file) {
        Set<ManualTag> tags = new HashSet<>();
        if (header.contains(column) && checkTagColumns(header, file).equals("")) {
            List<Integer> idTagIndexesList = findDuplicateColumn(ColumnNamePhysicalFile.MK_ID.name(), header);
            for (int i = 0; i < idTagIndexesList.size(); i++) {
                var isEmptyTag = new Object() {
                    boolean state = true;
                };
                ManualTag tag = new ManualTag();
                int index = i;
                List<ObjectFieldSetter> executorTagList = List.of(
                        ObjectFieldSetter.of(ColumnNamePhysicalFile.MK_ID.name(), tag::setMkId),
                        ObjectFieldSetter.of(ColumnNamePhysicalFile.MK_EVENT_DATE.name(), tag::setMkEventDate),
                        ObjectFieldSetter.of(ColumnNamePhysicalFile.MK_START.name(), tag::setMkStart),
                        ObjectFieldSetter.of(ColumnNamePhysicalFile.MK_EXPIRE.name(), tag::setMkExpire),
                        ObjectFieldSetter.of(ColumnNamePhysicalFile.MK_NUMBER_VALUE.name(), tag::setMkNumberValue),
                        ObjectFieldSetter.of(ColumnNamePhysicalFile.MK_TEXT_VALUE.name(), tag::setMkTextValue),
                        ObjectFieldSetter.of(ColumnNamePhysicalFile.MK_DESCRIPTION.name(), tag::setMkDescription),
                        ObjectFieldSetter.of(ColumnNamePhysicalFile.MK_SOURCE.name(), tag::setMkSource));

                executorTagList.forEach(executor -> {
                    String tagColumn = executor.getColumn();
                    List<Integer> listValue = findDuplicateColumn(tagColumn, header);
                    if (header.contains(tagColumn) && !StringUtils.isBlank(line.get(listValue.get(index)))) {
                        String value = line.get(listValue.get(index)).replace("\"", "");
                        executor.getFieldSetter().accept(UtilString.toUpperCase(value).trim());
                        isEmptyTag.state = false;
                    }
                });
                if (!isEmptyTag.state) {
                    person = manualPersonRepository.save(person);
                    tag.setPerson(person);
                    tags.add(tag);
                }
            }
        }
        return tags;
    }

    private Set<ManualCTag> addManualCTag(List<String> line, List<String> header, ManualCompany company,
                                          String column, FileDescription file) {
        Set<ManualCTag> tags = new HashSet<>();
        if (header.contains(column) && checkTagColumns(header, file).equals("")) {
            List<Integer> idTagIndexesList = findDuplicateColumn(ColumnNamePhysicalFile.MK_ID.name(), header);
            for (int i = 0; i < idTagIndexesList.size(); i++) {
                var isEmptyTag = new Object() {
                    boolean state = true;
                };
                ManualCTag tag = new ManualCTag();
                int index = i;
                List<ObjectFieldSetter> executorTagList = List.of(
                        ObjectFieldSetter.of(ColumnNamePhysicalFile.MK_ID.name(), tag::setMkId),
                        ObjectFieldSetter.of(ColumnNamePhysicalFile.MK_EVENT_DATE.name(), tag::setMkEventDate),
                        ObjectFieldSetter.of(ColumnNamePhysicalFile.MK_START.name(), tag::setMkStart),
                        ObjectFieldSetter.of(ColumnNamePhysicalFile.MK_EXPIRE.name(), tag::setMkExpire),
                        ObjectFieldSetter.of(ColumnNamePhysicalFile.MK_NUMBER_VALUE.name(), tag::setMkNumberValue),
                        ObjectFieldSetter.of(ColumnNamePhysicalFile.MK_TEXT_VALUE.name(), tag::setMkTextValue),
                        ObjectFieldSetter.of(ColumnNamePhysicalFile.MK_DESCRIPTION.name(), tag::setMkDescription),
                        ObjectFieldSetter.of(ColumnNamePhysicalFile.MK_SOURCE.name(), tag::setMkSource));

                executorTagList.forEach(executor -> {
                    String tagColumn = executor.getColumn();
                    List<Integer> listValue = findDuplicateColumn(tagColumn, header);
                    if (header.contains(tagColumn) && !StringUtils.isBlank(line.get(listValue.get(index)))) {
                        String value = line.get(listValue.get(index)).replace("\"", "");
                        executor.getFieldSetter().accept(UtilString.toUpperCase(value).trim());
                        isEmptyTag.state = false;
                    }
                });
                if (!isEmptyTag.state) {
                    company = manualCompanyRepository.save(company);
                    tag.setCompany(company);
                    tags.add(tag);
                }
            }
        }
        return tags;
    }

    private String checkTagColumns(List<String> header, FileDescription file) {
        List<Integer> idTagIndexesList = findDuplicateColumn(ColumnNamePhysicalFile.MK_ID.name(), header);
        List<Integer> eventDateIndexesList = findDuplicateColumn(ColumnNamePhysicalFile.MK_EVENT_DATE.name(), header);
        List<Integer> startIndexesList = findDuplicateColumn(ColumnNamePhysicalFile.MK_START.name(), header);
        List<Integer> expireIndexesList = findDuplicateColumn(ColumnNamePhysicalFile.MK_EXPIRE.name(), header);
        List<Integer> numberValueIndexesList = findDuplicateColumn(ColumnNamePhysicalFile.MK_NUMBER_VALUE.name(), header);
        List<Integer> textValueIndexesList = findDuplicateColumn(ColumnNamePhysicalFile.MK_TEXT_VALUE.name(), header);
        List<Integer> descriptionIndexesList = findDuplicateColumn(ColumnNamePhysicalFile.MK_DESCRIPTION.name(), header);
        List<Integer> sourceIndexesList = findDuplicateColumn(ColumnNamePhysicalFile.MK_SOURCE.name(), header);

        String wrongColumn = "";
        if (!(idTagIndexesList.size() == eventDateIndexesList.size()
                && idTagIndexesList.size() == startIndexesList.size()
                && idTagIndexesList.size() == expireIndexesList.size()
                && idTagIndexesList.size() == numberValueIndexesList.size()
                && idTagIndexesList.size() == textValueIndexesList.size()
                && idTagIndexesList.size() == descriptionIndexesList.size()
                && idTagIndexesList.size() == sourceIndexesList.size()))
            wrongColumn = "Вказано не всі колонки для тегу;";

        file.setWrongColumn(wrongColumn);
        fileDescriptionRepository.save(file);

        return wrongColumn;
    }

    public ValidatedManualPersonResponse getUploadedManualPerson(UUID uuid) {
        FileDescription description = fileDescriptionRepository.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException(FileDescription.class, uuid.toString()));

        List<ManualPerson> manualPeople = manualPersonRepository.findByUuid(description);

        List<ManualPersonStatus> peopleStatus = PersonValidator.manualPersonValidate(manualPeople);

        List<ManualTagStatus> tagsStatus = manualTagValidator.validate(
                manualPeople.stream()
                        .flatMap(p -> p.getTags().stream())
                        .collect(Collectors.toList()));

        List<ManualPersonDto> peopleDto = manualPeople.parallelStream()
                .map(manualPersonConverter::toDto)
                .sorted()
                .collect(Collectors.toList());

        Set<String> wrongColumns = Arrays.stream(description.getWrongColumn().split(";"))
                .filter(c -> !StringUtils.isBlank(c))
                .collect(Collectors.toSet());

        if (!peopleDto.isEmpty() && peopleStatus.isEmpty()
                && tagsStatus.isEmpty() && wrongColumns.isEmpty()) {
            description.setValidated(true);
            fileDescriptionRepository.save(description);
        }

        return new ValidatedManualPersonResponse(peopleDto, peopleStatus, tagsStatus, wrongColumns);
    }

    public ValidatedManualCompanyResponse getUploadedManualCompany(UUID uuid) {
        FileDescription description = fileDescriptionRepository.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException(FileDescription.class, uuid.toString()));

        List<ManualCompany> manualCompanies = manualCompanyRepository.findByUuid(description);

        List<ManualCompanyStatus> companiesStatus = companyValidator.manualCompanyValidate(manualCompanies);

        List<ManualTagStatus> tagsStatus = manualTagValidator.validateCTag(
                manualCompanies.stream()
                        .flatMap(p -> p.getTags().stream())
                        .collect(Collectors.toList()));

        List<ManualCompanyDto> companiesDto = manualCompanies.parallelStream()
                .map(manualCompanyConverter::toDto)
                .sorted()
                .collect(Collectors.toList());

        Set<String> wrongColumns = Arrays.stream(description.getWrongColumn().split(";"))
                .filter(c -> !StringUtils.isBlank(c))
                .collect(Collectors.toSet());

        if (!companiesDto.isEmpty() && companiesStatus.isEmpty()
                && tagsStatus.isEmpty() && wrongColumns.isEmpty()) {
            description.setValidated(true);
            fileDescriptionRepository.save(description);
        }

        return new ValidatedManualCompanyResponse(companiesDto, companiesStatus, tagsStatus, wrongColumns);
    }

    private void addWrongColumnFile(FileDescription fileDescription, List<String> header) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(fileDescription.getWrongColumn()))
            sb.append(fileDescription.getWrongColumn());
        if (fileDescription.getType().getName().equals(PHYSICAL)) {
            for (String columnName : header) {
                if (!Arrays.stream(ColumnNamePhysicalFile.values()).map(Enum::name).collect(Collectors.toList()).contains(columnName))
                    sb.append(columnName).append(";");
                if (StringUtils.isBlank(columnName))
                    sb.append("Файл містить заголовки без назви").append(";");
            }
        } else if (fileDescription.getType().getName().equals(JURIDICAL)) {
            for (String columnName : header) {
                if (!Arrays.stream(ColumnNameJuridicalFile.values()).map(Enum::name).collect(Collectors.toList()).contains(columnName))
                    sb.append(columnName).append(";");
                if (StringUtils.isBlank(columnName))
                    sb.append("Файл містить заголовки без назви").append(";");
            }

        }
        fileDescription.setWrongColumn(sb.toString());
        fileDescriptionRepository.save(fileDescription);
    }

    public void delete(UUID id) {
        FileDescription fileDescription = fileDescriptionRepository.findByUuid(id)
                .orElseThrow(() -> new RuntimeException("Can't find FileDescription with id= " + id));
        List<ManualPerson> people = manualPersonRepository.findByUuid(fileDescription);

        List<ManualCompany> companies = manualCompanyRepository.findByUuid(fileDescription);

        companies.stream().flatMap(c -> c.tags.stream()).forEach(manualCTagRepository::delete);

        people.stream().flatMap(p -> p.tags.stream()).forEach(manualTagRepository::delete);

        manualCompanyRepository.deleteAll(companies);

        manualPersonRepository.deleteAll(people);

        fileDescriptionRepository.delete(fileDescription);
    }

    public void enrich(UUID id) {
        FileDescription description = fileDescriptionRepository.findByUuid(id).orElseThrow(() ->
                new EntityNotFoundException(FileDescription.class, id));

        if (!description.isValidated()) throw new IllegalApiArgumentException("Файл містить помилки");

        Message message = null;
        if (description.getType().getName().equals(PHYSICAL)) {
            String jo = Utils.objectToJsonString(new EnricherPortionMessage(MANUAL_PERSON, id));
            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setPriority(10);
            message = new Message(jo.getBytes(StandardCharsets.UTF_8), messageProperties);
        }
        if (description.getType().getName().equals(JURIDICAL)) {
            String jo = Utils.objectToJsonString(new EnricherPortionMessage(MANUAL_COMPANY, id));
            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setPriority(10);
            message = new Message(jo.getBytes(StandardCharsets.UTF_8), messageProperties);
        }
        log.info("Sending task to {}", OtpExchange.ENRICHER);
        template.send(OtpExchange.ENRICHER, message);
    }

    public ValidatedManualPersonResponse updatePhysicalFile(Long id, int index, String value) {
        return manualPersonRepository.findById(id).isPresent()
                ? updateManualPerson(id, index, value)
                : updateManualTag(id, index, value);
    }

    public ValidatedManualCompanyResponse updateJuridicalFile(Long id, int index, String value) {
        return manualCompanyRepository.findById(id).isPresent()
                ? updateManualCompany(id, index, value)
                : updateManualCTag(id, index, value);
    }

    @Getter
    private static class ObjectFieldSetter {
        ObjectFieldSetter(String column, Consumer<String> fieldSetter) {
            this.column = column;
            this.fieldSetter = fieldSetter;
        }

        private final String column;
        private final Consumer<String> fieldSetter;

        public static ObjectFieldSetter of(String column, Consumer<String> fieldSetter) {
            return new ObjectFieldSetter(column, fieldSetter);
        }

        public void execute(String value) {
            fieldSetter.accept(value);
        }
    }

    public ValidatedManualPersonResponse updateManualPerson(Long id, int index, String value) {
        ManualPerson person = manualPersonRepository.getById(id);
        String finalValue = UtilString.toUpperCase(value).trim();
        List<Consumer<ManualPerson>> consumerList = List.of(
                p -> p.setCnum(finalValue),
                p -> p.setLnameUk(finalValue),
                p -> p.setFnameUk(finalValue),
                p -> p.setPnameUk(finalValue),
                p -> p.setLnameRu(finalValue),
                p -> p.setFnameRu(finalValue),
                p -> p.setPnameRu(finalValue),
                p -> p.setLnameEn(finalValue),
                p -> p.setFnameEn(finalValue),
                p -> p.setPnameEn(finalValue),
                p -> p.setBirthday(finalValue),
                p -> p.setOkpo(finalValue),
                p -> p.setCountry(finalValue),
                p -> p.setAddress(finalValue),
                p -> p.setPhone(finalValue),
                p -> p.setEmail(finalValue),
                p -> p.setBirthPlace(finalValue),
                p -> p.setSex(finalValue),
                p -> p.setComment(finalValue),
                p -> p.setPassLocalNum(finalValue),
                p -> p.setPassLocalSerial(finalValue),
                p -> p.setPassLocalIssuer(finalValue),
                p -> p.setPassLocalIssueDate(finalValue),
                p -> p.setPassIntNum(finalValue),
                p -> p.setPassIntRecNum(finalValue),
                p -> p.setPassIntIssuer(finalValue),
                p -> p.setPassIntIssueDate(finalValue),
                p -> p.setPassIdNum(finalValue),
                p -> p.setPassIdRecNum(finalValue),
                p -> p.setPassIdIssuer(finalValue),
                p -> p.setPassIdIssueDate(finalValue));
        consumerList.get(index).accept(person);
        manualPersonRepository.save(person);
        return getUploadedManualPerson(person.getUuid().getUuid());
    }

    public ValidatedManualCompanyResponse updateManualCompany(Long id, int index, String value) {
        ManualCompany company = manualCompanyRepository.getById(id);
        String finalValue = UtilString.toUpperCase(value).trim();
        List<Consumer<ManualCompany>> consumerList = List.of(
                p -> p.setCnum(finalValue),
                p -> p.setName(finalValue),
                p -> p.setNameEn(finalValue),
                p -> p.setShortName(finalValue),
                p -> p.setEdrpou(finalValue),
                p -> p.setPdv(finalValue),
                p -> p.setState(finalValue),
                p -> p.setAddress(finalValue),
                p -> p.setLname(finalValue),
                p -> p.setFname(finalValue),
                p -> p.setPname(finalValue),
                p -> p.setInn(finalValue),
                p -> p.setTypeRelationPerson(finalValue),
                p -> p.setCname(finalValue),
                p -> p.setEdrpouRelationCompany(finalValue),
                p -> p.setTypeRelationCompany(finalValue));
        consumerList.get(index).accept(company);
        manualCompanyRepository.save(company);
        return getUploadedManualCompany(company.getUuid().getUuid());
    }

    public ValidatedManualPersonResponse updateManualTag(Long id, int index, String value) {
        ManualTag tag = manualTagRepository.getById(id);
        String finalValue = UtilString.toUpperCase(value).trim();
        List<Consumer<ManualTag>> consumerList = List.of(
                t -> t.setMkId(finalValue),
                t -> t.setMkEventDate(finalValue),
                t -> t.setMkStart(finalValue),
                t -> t.setMkExpire(finalValue),
                t -> t.setMkNumberValue(finalValue),
                t -> t.setMkTextValue(finalValue),
                t -> t.setMkDescription(finalValue),
                t -> t.setMkSource(finalValue));
        consumerList.get(index).accept(tag);
        manualTagRepository.save(tag);
        return getUploadedManualPerson(tag.getPerson().getUuid().getUuid());
    }

    public ValidatedManualCompanyResponse updateManualCTag(Long id, int index, String value) {
        ManualCTag tag = manualCTagRepository.getById(id);
        String finalValue = UtilString.toUpperCase(value).trim();
        List<Consumer<ManualCTag>> consumerList = List.of(
                t -> t.setMkId(finalValue),
                t -> t.setMkEventDate(finalValue),
                t -> t.setMkStart(finalValue),
                t -> t.setMkExpire(finalValue),
                t -> t.setMkNumberValue(finalValue),
                t -> t.setMkTextValue(finalValue),
                t -> t.setMkDescription(finalValue),
                t -> t.setMkSource(finalValue));
        consumerList.get(index).accept(tag);
        manualCTagRepository.save(tag);
        return getUploadedManualCompany(tag.getCompany().getUuid().getUuid());
    }

    public Set<TagTypeDto> getTagType() {
        return tagTypeRepository.findAll().stream()
                .map(tagConverter::toTagTypeDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<CompanyStateDto> getCompanyState() {
        return companyStateRepository.findAll().stream()
                .map(companyConverter::toCompanyStateDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<CompanyRoleDto> getCompanyRole() {
        return companyRoleRepository.findAll().stream()
                .map(companyConverter::toCompanyRoleDto).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public ByteArrayInputStream downloadJuridicalFile() throws IOException {
        ByteArrayOutputStream outputStream;
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet();
            XSSFRow row = sheet.createRow(0);
            row.createCell(0, CellType.STRING).setCellValue("CNUM");
            row.createCell(1, CellType.STRING).setCellValue("NAME");
            row.createCell(2, CellType.STRING).setCellValue("NAME_EN");
            row.createCell(3, CellType.STRING).setCellValue("SHORT_NAME");
            row.createCell(4, CellType.STRING).setCellValue("EDRPOU");
            row.createCell(5, CellType.STRING).setCellValue("PDV");
            row.createCell(6, CellType.STRING).setCellValue("STATE");
            row.createCell(7, CellType.STRING).setCellValue("ADDRESS");
            row.createCell(8, CellType.STRING).setCellValue("LNAME");
            row.createCell(9, CellType.STRING).setCellValue("FNAME");
            row.createCell(10, CellType.STRING).setCellValue("PNAME");
            row.createCell(11, CellType.STRING).setCellValue("INN_RELATION_PERSON");
            row.createCell(12, CellType.STRING).setCellValue("TYPE_RELATION_PERSON");
            row.createCell(13, CellType.STRING).setCellValue("CNAME");
            row.createCell(14, CellType.STRING).setCellValue("EDRPOU_RELATION_COMPANY");
            row.createCell(15, CellType.STRING).setCellValue("TYPE_RELATION_COMPANY");
            row.createCell(16, CellType.STRING).setCellValue("MK_ID");
            row.createCell(17, CellType.NUMERIC).setCellValue("MK_EVENT_DATE");
            row.createCell(18, CellType.STRING).setCellValue("MK_START");
            row.createCell(19, CellType.STRING).setCellValue("MK_EXPIRE");
            row.createCell(20, CellType.STRING).setCellValue("MK_NUMBER_VALUE");
            row.createCell(21, CellType.STRING).setCellValue("MK_TEXT_VALUE");
            row.createCell(22, CellType.STRING).setCellValue("MK_DESCRIPTION");
            row.createCell(23, CellType.STRING).setCellValue("MK_SOURCE");
            outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        }
    }

    public ByteArrayInputStream downloadPhysicalFile() throws IOException {
        ByteArrayOutputStream outputStream;
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet();
            int i = 0;
            XSSFRow row = sheet.createRow(i);
            row.createCell(0, CellType.STRING).setCellValue("CNUM");
            row.createCell(1, CellType.STRING).setCellValue("LNAME_UK");
            row.createCell(2, CellType.STRING).setCellValue("FNAME_UK");
            row.createCell(3, CellType.STRING).setCellValue("PNAME_UK");
            row.createCell(4, CellType.STRING).setCellValue("LNAME_RU");
            row.createCell(5, CellType.STRING).setCellValue("FNAME_RU");
            row.createCell(6, CellType.STRING).setCellValue("PNAME_RU");
            row.createCell(7, CellType.STRING).setCellValue("LNAME_EN");
            row.createCell(8, CellType.STRING).setCellValue("FNAME_EN");
            row.createCell(9, CellType.STRING).setCellValue("PNAME_EN");
            row.createCell(10, CellType.STRING).setCellValue("BIRTHDAY");
            row.createCell(11, CellType.STRING).setCellValue("OKPO");
            row.createCell(12, CellType.STRING).setCellValue("COUNTRY");
            row.createCell(13, CellType.STRING).setCellValue("ADDRESS");
            row.createCell(14, CellType.STRING).setCellValue("PHONE");
            row.createCell(15, CellType.STRING).setCellValue("EMAIL");
            row.createCell(16, CellType.STRING).setCellValue("BIRTH_PLACE");
            row.createCell(17, CellType.STRING).setCellValue("SEX");
            row.createCell(18, CellType.STRING).setCellValue("COMMENT");
            row.createCell(19, CellType.STRING).setCellValue("PASS_LOCAL_NUM");
            row.createCell(20, CellType.STRING).setCellValue("PASS_LOCAL_SERIAL");
            row.createCell(21, CellType.STRING).setCellValue("PASS_LOCAL_ISSUE_DATE");
            row.createCell(22, CellType.STRING).setCellValue("PASS_INT_NUM");
            row.createCell(23, CellType.STRING).setCellValue("PASS_INT_REC_NUM");
            row.createCell(24, CellType.STRING).setCellValue("PASS_INT_ISSUER");
            row.createCell(25, CellType.STRING).setCellValue("PASS_INT_ISSUE_DATE");
            row.createCell(26, CellType.STRING).setCellValue("PASS_ID_NUM");
            row.createCell(27, CellType.STRING).setCellValue("PASS_ID_REC_NUM");
            row.createCell(28, CellType.STRING).setCellValue("PASS_ID_ISSUER");
            row.createCell(29, CellType.STRING).setCellValue("PASS_ID_ISSUE_DATE");
            row.createCell(30, CellType.STRING).setCellValue("MK_ID");
            row.createCell(31, CellType.STRING).setCellValue("MK_EVENT_DATE");
            row.createCell(32, CellType.STRING).setCellValue("MK_START");
            row.createCell(33, CellType.STRING).setCellValue("MK_EXPIRE");
            row.createCell(34, CellType.STRING).setCellValue("MK_NUMBER_VALUE");
            row.createCell(35, CellType.STRING).setCellValue("MK_TEXT_VALUE");
            row.createCell(36, CellType.STRING).setCellValue("MK_DESCRIPTION");
            row.createCell(37, CellType.STRING).setCellValue("MK_SOURCE");
            outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        }
    }
}
