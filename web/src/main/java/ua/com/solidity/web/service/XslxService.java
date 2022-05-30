package ua.com.solidity.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CharMatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.amqp.core.AmqpTemplate;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ua.com.solidity.common.model.EnricherMessage;
import ua.com.solidity.db.entities.FileDescription;
import ua.com.solidity.db.entities.ManualPerson;
import ua.com.solidity.db.entities.ManualTag;
import ua.com.solidity.db.entities.PhysicalPerson;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.repositories.FileDescriptionRepository;
import ua.com.solidity.db.repositories.ManualPersonRepository;
import ua.com.solidity.db.repositories.ManualTagRepository;
import ua.com.solidity.db.repositories.PhysicalPersonRepository;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.web.dto.ManualPersonDto;
import ua.com.solidity.web.dto.ManualTagDto;
import ua.com.solidity.web.dto.PhysicalPersonDto;
import ua.com.solidity.db.entities.User;
import ua.com.solidity.web.exception.EntityNotFoundException;
import ua.com.solidity.web.exception.IllegalApiArgumentException;
import ua.com.solidity.db.repositories.UserRepository;
import ua.com.solidity.web.response.ValidatedManualPersonResponse;
import ua.com.solidity.web.response.ValidatedPhysicalPersonResponse;
import ua.com.solidity.web.response.secondary.CellStatus;
import ua.com.solidity.web.response.secondary.ManualPersonStatus;
import ua.com.solidity.web.response.secondary.ManualTagStatus;
import ua.com.solidity.web.security.service.Extractor;
import ua.com.solidity.web.service.dynamicfile.Tag;
import ua.com.solidity.web.service.validator.ManualPersonValidator;
import ua.com.solidity.web.service.validator.ManualTagValidator;
import ua.com.solidity.web.service.validator.PhysicalPersonValidator;
import ua.com.solidity.web.service.dynamicfile.ColumnName;
import ua.com.solidity.web.utils.UtilString;

import static ua.com.solidity.web.utils.UtilString.toLowerCase;
import static ua.com.solidity.web.utils.UtilString.toUpperCase;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
@Service
public class XslxService {

	private final Extractor extractor;
	private final ModelMapper modelMapper;
	private final PhysicalPersonRepository personRepository;
	private final FileDescriptionRepository fileDescriptionRepository;
	private final YPersonRepository ypr;
	private final UserRepository userRepository;
    private final ManualPersonRepository manualPersonRepository;
    private final ManualTagRepository manualTagRepository;
    private int count;

    private static final String MANUAL_PERSON = "manual_person";

    @Value("${enricher.rabbitmq.name}")
    private String enricherQueue;
    private final AmqpTemplate template;

	public UUID upload(MultipartFile multipartFile, HttpServletRequest request) {
		log.debug("Attempting to parse file in XlsxService for uploading into DB.");
		UUID uuid = UUID.randomUUID();
		String pattern = "dd.MM.yyyy";
		DateFormat df = new SimpleDateFormat(pattern);
		FileDescription fileDescription = new FileDescription();
		fileDescription.setUuid(uuid);
		fileDescription.setDescription("");

		String login = extractor.extractLogin(request);

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
		FileDescription downloadedFileDescription = fileDescriptionRepository.findByUuid(uuid)
				.orElseThrow(() -> new EntityNotFoundException(FileDescription.class, uuid));
		downloadedFileDescription.setRowCount(rowCount);
		fileDescriptionRepository.save(downloadedFileDescription);
		return uuid;
	}

	public void upload(UUID uuid, String description) {
		FileDescription fileDescription = fileDescriptionRepository.findById(uuid)
				.orElseThrow(() -> new EntityNotFoundException(FileDescription.class, uuid));
		fileDescription.setDescription(description);
		fileDescriptionRepository.save(fileDescription);
	}

	public List<FileDescription> getUploaded() {
		return fileDescriptionRepository.findByValidated(true);
	}

	public ValidatedPhysicalPersonResponse getUploaded(UUID uuid) {
		FileDescription description = fileDescriptionRepository.findById(uuid)
				.orElseThrow(() -> new EntityNotFoundException(FileDescription.class, uuid.toString()));

		List<PhysicalPerson> physicalPersonList = personRepository.findByUuid(description);
		List<CellStatus> cellStatusList = PhysicalPersonValidator.validatePhysicalPerson(physicalPersonList);

		List<PhysicalPersonDto> dtoList = physicalPersonList.stream()
				.map(p -> modelMapper.map(p, PhysicalPersonDto.class))
				.collect(Collectors.toList());
		return new ValidatedPhysicalPersonResponse(dtoList, cellStatusList);
	}

	private void savePerson(PhysicalPerson person, Map<Integer, String> values) {

		person.setCustomer(values.get(0));
		person.setSurnameUk(toUpperCase(values.get(1)));
		person.setNameUk(toUpperCase(values.get(2)));
		person.setPatronymicUk(toUpperCase(values.get(3)));
		person.setSurnameRu(toUpperCase(values.get(4)));
		person.setNameRu(toUpperCase(values.get(5)));
		person.setPatronymicRu(toUpperCase(values.get(6)));
		person.setSurnameEn(toUpperCase(values.get(7)));
		person.setNameEn(toUpperCase(values.get(8)));
		person.setPatronymicEn(toUpperCase(values.get(9)));
		person.setBirthDay(values.get(10));
		person.setINN(values.get(11));
		person.setLocalPassportCode(values.get(12));
		person.setLocalPassportSeries(toUpperCase(values.get(13)));
		person.setLocalPassportAuthority(toUpperCase(values.get(14)));
		person.setLocalPassportDate(values.get(15));
		person.setForeignPassportNumber(toUpperCase(values.get(16)));
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
		person.setDeathNotificationSource(toUpperCase(values.get(27)));
		person.setBlackListTagN(values.get(28));
		person.setBlackListDateFromN(values.get(29));
		person.setBlackListDateToN(values.get(30));
		person.setEllipsis(values.get(31));
		person.setComment(toUpperCase(values.get(32)));
		person.setCitizenship(toUpperCase(values.get(33)));
		person.setLivingAddress(toUpperCase(values.get(34)));
		String phone = values.get(35) == null ? null : CharMatcher.inRange('0', '9')
				.precomputed()
				.retainFrom(values.get(35));
		person.setPhoneNumber(phone);
		person.setEmail(toLowerCase(values.get(36)));
		person.setBirthPlace(toUpperCase(values.get(37)));
		person.setSex(toUpperCase(values.get(38)));
		person.setSensitiveInformationTag(toUpperCase(values.get(39)));
		person.setRelationTag(values.get(40));
		person.setBankProducts(toUpperCase(values.get(41)));

		personRepository.save(person);
	}

	public void subscribe(UUID id, HttpServletRequest request) {
		User user = extractor.extractUser(request);
		Optional<YPerson> personOptional = user.getPeople()
				.stream()
				.filter(e -> e.getId().equals(id))
				.findAny();
		if (personOptional.isPresent()) throw new IllegalApiArgumentException("Ви вже підписалися на цю людину");
		YPerson yPerson = ypr.findById(id)
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

    public UUID uploadDynamicFile(MultipartFile multipartFile, String delimiter, String code,
                                  HttpServletRequest request) {
        UUID uuid = UUID.randomUUID();
        FileDescription fileDescription = new FileDescription();
        fileDescription.setUuid(uuid);
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
        getUploadedManualPerson(fileDescription.getUuid());
        return uuid;
    }

    private List<Integer> findDuplicateColumn(String column, List<String> header) {
        return IntStream.range(0, header.size())
                .filter(i -> header.get(i).equals(column))
                .boxed().collect(Collectors.toList());
    }

    private void parseCsv(MultipartFile file, String delimiter, String code,
                          FileDescription fileDescription) {
        CSVFormat.Builder builder = CSVFormat.Builder.create();
        builder.setSkipHeaderRecord(true).setDelimiter(delimiter)
                .setHeader()
                .setIgnoreEmptyLines(true)
                .setTrim(true);
        try (InputStream stream = file.getInputStream()) {
            CSVParser parser = CSVParser.parse(stream, Charset.forName(code), builder.build());
            List<String> header = new ArrayList<>(parser.getHeaderNames());
            header.set(0, header.get(0).substring(1));

            addWrongColumn(fileDescription, header);
            if (fileDescription.getWrongColumn().length() == 0) {
                for (CSVRecord line : parser)
                    addManualPerson(line.toList(), header, fileDescription);
            }
        } catch (IOException e) {
            log.error("Opening an InputStream from csv file " + file.getOriginalFilename() + " failed", e);
        }
    }

    private void parseXlsx(MultipartFile file, FileDescription fileDescription) {
        String pattern = "dd.MM.yyyy";
        DateFormat df = new SimpleDateFormat(pattern);
        try (InputStream stream = file.getInputStream()) {
            XSSFWorkbook wb = new XSSFWorkbook(stream);
            XSSFSheet sheet = wb.getSheetAt(0);
            List<String> header = new ArrayList<>();
            List<String> line = new ArrayList<>();
            if (sheet.getRow(0) != null)
                for (Cell cell : sheet.getRow(0))
                    header.add(cell.toString());

            addWrongColumn(fileDescription, header);

            if (fileDescription.getWrongColumn().length() == 0) {
                if (sheet.getLastRowNum() > 0) {
                    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                        line.clear();
                        if (sheet.getRow(i) != null) {
                            for (int j = 0; j < header.size(); j++) {
                                Cell cell = sheet.getRow(i).getCell(j);
                                if (cell != null && cell.getCellType().equals(CellType.NUMERIC)
                                        && DateUtil.isCellDateFormatted(cell)) {
                                    line.add(df.format(cell.getDateCellValue()));
                                } else if (cell != null &&
                                        cell.getCellType().equals(CellType.NUMERIC)) {
                                    line.add(Objects.toString(Math.round(cell.getNumericCellValue()), ""));
                                } else {
                                    line.add(Objects.toString(cell, ""));
                                }
                            }
                            addManualPerson(line, header, fileDescription);
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("Opening an InputStream from xslx file " + file.getOriginalFilename() + " failed", e);
        }
    }

    private void addManualPerson(List<String> line, List<String> header, FileDescription file) {
        ManualPerson person = new ManualPerson();
        person.setUuid(file);
        boolean notEmpty = false;

        String column = ColumnName.CNUM.name();
        if (findColumn(column, header, line)) {
            person.setCnum(line.get(header.indexOf(column)).replace("\"", ""));
            notEmpty = true;
        }
        column = ColumnName.LNAME_UK.name();
        if (findColumn(column, header, line)) {
            person.setLnameUk(line.get(header.indexOf(column)).replace("\"", ""));
            notEmpty = true;
        }
        column = ColumnName.FNAME_UK.name();
        if (findColumn(column, header, line)) {
            person.setFnameUk(line.get(header.indexOf(column)).replace("\"", ""));
            notEmpty = true;
        }
        column = ColumnName.PNAME_UK.name();
        if (findColumn(column, header, line)) {
            person.setPnameUk(line.get(header.indexOf(column)).replace("\"", ""));
            notEmpty = true;
        }
        column = ColumnName.LNAME_RU.name();
        if (findColumn(column, header, line)) {
            person.setLnameRu(line.get(header.indexOf(column)).replace("\"", ""));
            notEmpty = true;
        }
        column = ColumnName.FNAME_RU.name();
        if (findColumn(column, header, line)) {
            person.setFnameRu(line.get(header.indexOf(column)).replace("\"", ""));
            notEmpty = true;
        }
        column = ColumnName.PNAME_RU.name();
        if (findColumn(column, header, line)) {
            person.setPnameRu(line.get(header.indexOf(column)).replace("\"", ""));
            notEmpty = true;
        }
        column = ColumnName.LNAME_EN.name();
        if (findColumn(column, header, line)) {
            person.setLnameEn(line.get(header.indexOf(column)).replace("\"", ""));
            notEmpty = true;
        }
        column = ColumnName.FNAME_EN.name();
        if (findColumn(column, header, line)) {
            person.setFnameEn(line.get(header.indexOf(column)).replace("\"", ""));
            notEmpty = true;
        }
        column = ColumnName.PNAME_EN.name();
        if (findColumn(column, header, line)) {
            person.setPnameEn(line.get(header.indexOf(column)).replace("\"", ""));
            notEmpty = true;
        }
        column = ColumnName.BIRTHDAY.name();
        if (findColumn(column, header, line)) {
            person.setBirthday(line.get(header.indexOf(column)).replace("\"", ""));
            notEmpty = true;
        }
        column = ColumnName.OKPO.name();
        if (findColumn(column, header, line)) {
            person.setOkpo(line.get(header.indexOf(column)).replace("\"", ""));
            notEmpty = true;
        }
        column = ColumnName.COUNTRY.name();
        if (findColumn(column, header, line)) {
            person.setCountry(line.get(header.indexOf(column)).replace("\"", ""));
            notEmpty = true;
        }
        column = ColumnName.ADDRESS.name();
        if (findColumn(column, header, line)) {
            person.setAddress(line.get(header.indexOf(column)).replace("\"", ""));
            notEmpty = true;
        }
        column = ColumnName.PHONE.name();
        if (findColumn(column, header, line)) {
            person.setPhone(line.get(header.indexOf(column)).replace("\"", ""));
            notEmpty = true;
        }
        column = ColumnName.EMAIL.name();
        if (findColumn(column, header, line)) {
            person.setEmail(line.get(header.indexOf(column)).replace("\"", ""));
            notEmpty = true;
        }
        column = ColumnName.BIRTH_PLACE.name();
        if (findColumn(column, header, line)) {
            person.setBirthPlace(line.get(header.indexOf(column)).replace("\"", ""));
            notEmpty = true;
        }
        column = ColumnName.SEX.name();
        if (findColumn(column, header, line)) {
            person.setSex(line.get(header.indexOf(column)).replace("\"", ""));
            notEmpty = true;
        }
        column = ColumnName.COMMENT.name();
        if (findColumn(column, header, line)) {
            person.setComment(line.get(header.indexOf(column)).replace("\"", ""));
            notEmpty = true;
        }
        column = ColumnName.PASS_LOCAL_NUM.name();
        if (findColumn(column, header, line)) {
            person.setPassLocalNum(line.get(header.indexOf(column)).replace("\"", ""));
            notEmpty = true;
        }
        column = ColumnName.PASS_LOCAL_SERIAL.name();
        if (findColumn(column, header, line)) {
            person.setPassLocalSerial(line.get(header.indexOf(column)).replace("\"", ""));
            notEmpty = true;
        }
        column = ColumnName.PASS_LOCAL_ISSUER.name();
        if (findColumn(column, header, line)) {
            person.setPassLocalIssuer(line.get(header.indexOf(column)).replace("\"", ""));
            notEmpty = true;
        }
        column = ColumnName.PASS_LOCAL_ISSUE_DATE.name();
        if (findColumn(column, header, line)) {
            person.setPassLocalIssueDate(line.get(header.indexOf(column)).replace("\"", ""));
            notEmpty = true;
        }
        column = ColumnName.PASS_INT_NUM.name();
        if (findColumn(column, header, line)) {
            person.setPassIntNum(line.get(header.indexOf(column)).replace("\"", ""));
            notEmpty = true;
        }
        column = ColumnName.PASS_INT_REC_NUM.name();
        if (findColumn(column, header, line)) {
            person.setPassIntRecNum(line.get(header.indexOf(column)).replace("\"", ""));
            notEmpty = true;
        }
        column = ColumnName.PASS_INT_ISSUER.name();
        if (findColumn(column, header, line)) {
            person.setPassIntIssuer(line.get(header.indexOf(column)).replace("\"", ""));
            notEmpty = true;
        }
        column = ColumnName.PASS_INT_ISSUE_DATE.name();
        if (findColumn(column, header, line)) {
            person.setPassIntIssueDate(line.get(header.indexOf(column)).replace("\"", ""));
            notEmpty = true;
        }
        column = ColumnName.PASS_ID_NUM.name();
        if (findColumn(column, header, line)) {
            person.setPassIdNum(line.get(header.indexOf(column)).replace("\"", ""));
            notEmpty = true;
        }
        column = ColumnName.PASS_ID_REC_NUM.name();
        if (findColumn(column, header, line)) {
            person.setPassIdRecNum(line.get(header.indexOf(column)).replace("\"", ""));
            notEmpty = true;
        }
        column = ColumnName.PASS_ID_ISSUER.name();
        if (findColumn(column, header, line)) {
            person.setPassIdIssuer(line.get(header.indexOf(column)).replace("\"", ""));
            notEmpty = true;
        }
        column = ColumnName.PASS_ID_ISSUE_DATE.name();
        if (findColumn(column, header, line)) {
            person.setPassIdIssueDate(line.get(header.indexOf(column)).replace("\"", ""));
            notEmpty = true;
        }

        column = ColumnName.MK_ID.name();
        if (header.contains(column)) {
            Set<ManualTag> tags = new HashSet<>();
            List<Integer> idTagIndexesList = findDuplicateColumn(ColumnName.MK_ID.name(), header);
            List<Integer> eventDateIndexesList = findDuplicateColumn(ColumnName.MK_EVENT_DATE.name(), header);
            List<Integer> startIndexesList = findDuplicateColumn(ColumnName.MK_START.name(), header);
            List<Integer> expireIndexesList = findDuplicateColumn(ColumnName.MK_EXPIRE.name(), header);
            List<Integer> numberValueIndexesList = findDuplicateColumn(ColumnName.MK_NUMBER_VALUE.name(), header);
            List<Integer> textValueIndexesList = findDuplicateColumn(ColumnName.MK_TEXT_VALUE.name(), header);
            List<Integer> descriptionIndexesList = findDuplicateColumn(ColumnName.MK_DESCRIPTION.name(), header);
            List<Integer> sourceIndexesList = findDuplicateColumn(ColumnName.MK_SOURCE.name(), header);

            StringBuilder sb = new StringBuilder();
            if (idTagIndexesList.size() != eventDateIndexesList.size())
                sb.append("Не вистачає колонки MK_EVENT_DATE").append(";");
            if (idTagIndexesList.size() != startIndexesList.size())
                sb.append("Не вистачає колонки MK_START").append(";");
            if (idTagIndexesList.size() != expireIndexesList.size())
                sb.append("Не вистачає колонки MK_EXPIRE").append(";");
            if (idTagIndexesList.size() != numberValueIndexesList.size())
                sb.append("Не вистачає колонки MK_NUMBER_VALUE").append(";");
            if (idTagIndexesList.size() != textValueIndexesList.size())
                sb.append("Не вистачає колонки MK_TEXT_VALUE").append(";");
            if (idTagIndexesList.size() != descriptionIndexesList.size())
                sb.append("Не вистачає колонки MK_DESCRIPTION").append(";");
            if (idTagIndexesList.size() != sourceIndexesList.size())
                sb.append("Не вистачає колонки MK_SOURCE").append(";");
            file.setWrongColumn(sb.toString().trim());
            fileDescriptionRepository.save(file);

            if (sb.toString().length() == 0) {
                for (int i = 0; i < idTagIndexesList.size(); i++) {
                    boolean tagNotEmpty = false;
                    ManualTag tag = new ManualTag();
                    if (!StringUtils.isBlank(line.get(idTagIndexesList.get(i)))) {
                        if (Arrays.stream(Tag.values()).map(Enum::name).collect(Collectors.toList())
                                .contains(line.get(idTagIndexesList.get(i))))
                            tag.setName(Tag.valueOf(line.get(idTagIndexesList.get(i))).getName());
                        tag.setMkId(line.get(idTagIndexesList.get(i)).replace("\"", ""));
                        tagNotEmpty = true;
                    }
                    if (!StringUtils.isBlank(line.get(eventDateIndexesList.get(i)))) {
                        tag.setMkEventDate(line.get(eventDateIndexesList.get(i)).replace("\"", ""));
                        tagNotEmpty = true;
                    }
                    if (!StringUtils.isBlank(line.get(startIndexesList.get(i)))) {
                        tag.setMkStart(line.get(startIndexesList.get(i)).replace("\"", ""));
                        tagNotEmpty = true;
                    }
                    if (!StringUtils.isBlank(line.get(expireIndexesList.get(i)))) {
                        tag.setMkExpire(line.get(expireIndexesList.get(i)).replace("\"", ""));
                        tagNotEmpty = true;
                    }
                    if (!StringUtils.isBlank(line.get(numberValueIndexesList.get(i)))) {
                        tag.setMkNumberValue(line.get(numberValueIndexesList.get(i)).replace("\"", ""));
                        tagNotEmpty = true;
                    }
                    if (!StringUtils.isBlank(line.get(textValueIndexesList.get(i)))) {
                        tag.setMkTextValue(line.get(textValueIndexesList.get(i)).replace("\"", ""));
                        tagNotEmpty = true;
                    }
                    if (!StringUtils.isBlank(line.get(descriptionIndexesList.get(i)))) {
                        tag.setMkDescription(line.get(descriptionIndexesList.get(i)).replace("\"", ""));
                        tagNotEmpty = true;
                    }
                    if (!StringUtils.isBlank(line.get(sourceIndexesList.get(i)))) {
                        tag.setMkSource(line.get(sourceIndexesList.get(i)).replace("\"", ""));
                        tagNotEmpty = true;
                    }
                    if (tagNotEmpty) {
                        person = manualPersonRepository.save(person);
                        tag.setPerson(person);
                        tags.add(tag);
                    }
                }
                if (!tags.isEmpty()) {
                    person.setTags(tags);
                    notEmpty = true;
                }
            }
        }
        if (notEmpty) {
            manualPersonRepository.save(person);
            count++;
        }
    }

    private boolean findColumn(String column, List<String> header, List<String> line) {
        return header.contains(column)
                && !StringUtils.isBlank(line.get(header.indexOf(column)));
    }

    public ValidatedManualPersonResponse getUploadedManualPerson(UUID uuid) {
        FileDescription description = fileDescriptionRepository.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException(FileDescription.class, uuid.toString()));

        List<ManualPerson> manualPeople = manualPersonRepository.findByUuid(description);

        List<ManualPersonStatus> peopleStatus = ManualPersonValidator.manualPersonValidate(manualPeople);

        List<ManualTagStatus> tagsStatus = ManualTagValidator.manualTagValidator(
                manualPeople.stream()
                        .flatMap(p -> p.getTags().stream())
                        .collect(Collectors.toList()));

        List<ManualPersonDto> peopleDto = manualPeople.stream()
                .map(p -> modelMapper.map(p, ManualPersonDto.class))
                .collect(Collectors.toList());

        for (ManualPersonDto personDto : peopleDto)
            personDto.setTags(manualPersonRepository.findById(personDto.getId())
                    .orElseThrow(() -> new EntityNotFoundException(ManualPerson.class, String.valueOf(personDto.getId())))
                    .getTags().stream()
                    .map(t -> modelMapper.map(t, ManualTagDto.class))
                    .collect(Collectors.toSet()));

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

    private void addWrongColumn(FileDescription fileDescription, List<String> header) {
        StringBuilder sb = new StringBuilder();
        for (String columnName : header)
            if (!Arrays.stream(ColumnName.values()).map(Enum::name).collect(Collectors.toList()).contains(columnName))
                sb.append(columnName).append(";");

        fileDescription.setWrongColumn(sb.toString());
        fileDescriptionRepository.save(fileDescription);
    }

    public void delete(UUID id) {
        FileDescription fileDescription = fileDescriptionRepository.findByUuid(id)
                .orElseThrow(() -> new RuntimeException("Can't find FileDescription with id= " + id));
        List<ManualPerson> people = manualPersonRepository.findByUuid(fileDescription);

        people.stream().flatMap(p -> p.tags.stream()).forEach(manualTagRepository::delete);

        manualPersonRepository.deleteAll(people);

        fileDescriptionRepository.delete(fileDescription);
    }

    public void enrich(UUID id) {
        String jo;
        try {
            jo = new ObjectMapper().writeValueAsString(new EnricherMessage(MANUAL_PERSON, id));
            log.info("Emit to " + enricherQueue);
            template.convertAndSend(enricherQueue, jo);
        } catch (JsonProcessingException e) {
            log.error("Couldn't convert json: {}", e.getMessage());
        }
    }

    public ValidatedManualPersonResponse update(Long id, int index, String value) {
        if (manualPersonRepository.findById(id).isPresent())
            return updateManualPerson(id, index, value);
        return updateManualTag(id, index, value);
    }


    public ValidatedManualPersonResponse updateManualPerson(Long id, int index, String value) {
        ManualPerson person = manualPersonRepository.getById(id);
        List<Consumer<ManualPerson>> consumerList = List.of(
                p -> p.setCnum(value),
                p -> p.setLnameUk(value),
                p -> p.setFnameUk(value),
                p -> p.setPnameUk(value),
                p -> p.setLnameRu(value),
                p -> p.setFnameRu(value),
                p -> p.setPnameRu(value),
                p -> p.setLnameEn(value),
                p -> p.setFnameEn(value),
                p -> p.setPnameEn(value),
                p -> p.setBirthday(value),
                p -> p.setOkpo(value),
                p -> p.setCountry(value),
                p -> p.setAddress(value),
                p -> p.setPhone(value),
                p -> p.setEmail(value),
                p -> p.setBirthPlace(value),
                p -> p.setSex(value),
                p -> p.setComment(value),
                p -> p.setPassLocalNum(value),
                p -> p.setPassLocalSerial(value),
                p -> p.setPassLocalIssuer(value),
                p -> p.setPassLocalIssueDate(value),
                p -> p.setPassIntNum(value),
                p -> p.setPassIntRecNum(value),
                p -> p.setPassIntIssuer(value),
                p -> p.setPassIntIssueDate(value),
                p -> p.setPassIdNum(value),
                p -> p.setPassIdRecNum(value),
                p -> p.setPassIdIssuer(value),
                p -> p.setPassIdIssueDate(value));
        consumerList.get(index).accept(person);
        manualPersonRepository.save(person);
        return getUploadedManualPerson(person.getUuid().getUuid());
    }

    public ValidatedManualPersonResponse updateManualTag(Long id, int index, String value) {
        ManualTag tag = manualTagRepository.getById(id);
        List<Consumer<ManualTag>> consumerList = List.of(
                t -> t.setMkId(value),
                t -> t.setName(value),
                t -> t.setMkEventDate(value),
                t -> t.setMkStart(value),
                t -> t.setMkExpire(value),
                t -> t.setMkNumberValue(value),
                t -> t.setMkTextValue(value),
                t -> t.setMkDescription(value),
                t -> t.setMkSource(value));
        consumerList.get(index).accept(tag);
        manualTagRepository.save(tag);
        return getUploadedManualPerson(tag.getPerson().getUuid().getUuid());
    }
}
