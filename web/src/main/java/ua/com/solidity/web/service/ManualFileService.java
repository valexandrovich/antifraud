package ua.com.solidity.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ua.com.solidity.common.model.EnricherPortionMessage;
import ua.com.solidity.db.entities.FileDescription;
import ua.com.solidity.db.entities.ManualPerson;
import ua.com.solidity.db.entities.ManualTag;
import ua.com.solidity.db.repositories.FileDescriptionRepository;
import ua.com.solidity.db.repositories.ManualPersonRepository;
import ua.com.solidity.db.repositories.ManualTagRepository;
import ua.com.solidity.web.dto.ManualPersonDto;
import ua.com.solidity.web.dto.ManualTagDto;
import ua.com.solidity.web.exception.EntityNotFoundException;
import ua.com.solidity.web.exception.IllegalApiArgumentException;
import ua.com.solidity.web.response.ValidatedManualPersonResponse;
import ua.com.solidity.web.response.secondary.ManualPersonStatus;
import ua.com.solidity.web.response.secondary.ManualTagStatus;
import ua.com.solidity.web.security.service.Extractor;
import ua.com.solidity.web.service.dynamicfile.ColumnName;
import ua.com.solidity.web.service.validator.ManualPersonValidator;
import ua.com.solidity.web.service.validator.ManualTagValidator;
import ua.com.solidity.web.utils.UtilString;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
@Service
public class ManualFileService {
	private final Extractor extractor;
	private final ModelMapper modelMapper;
	private final FileDescriptionRepository fileDescriptionRepository;
	private final ManualPersonRepository manualPersonRepository;
	private final ManualTagRepository manualTagRepository;
	private final ManualTagValidator manualTagValidator;
	private int count;

	private static final String MANUAL_PERSON = "manual_person";

	@Value("${enricher.rabbitmq.name}")
	private String enricherQueue;
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

	public UUID uploadDynamicFile(MultipartFile multipartFile, String delimiter, String code, HttpServletRequest request) {
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
		try (InputStream stream = file.getInputStream()) {
			XSSFWorkbook wb = new XSSFWorkbook(stream);
			XSSFSheet sheet = wb.getSheetAt(0);
			List<String> header = new ArrayList<>();
			List<String> line = new ArrayList<>();
			if (sheet.getRow(0) != null)
				for (Cell cell : sheet.getRow(0))
					header.add(cell.toString());

			addWrongColumn(fileDescription, header);

			if (fileDescription.getWrongColumn().length() == 0 && sheet.getLastRowNum() > 0) {
				for (int i = 1; i <= sheet.getLastRowNum(); i++) {
					if (sheet.getRow(i) != null)
						checkXlsxFieldFormat(line, header, sheet.getRow(i), fileDescription);
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
			} else {
				line.add(Objects.toString(cell, ""));
			}
		}
		addManualPerson(line, header, file);
	}

	private void addManualPerson(List<String> line, List<String> header, FileDescription file) {
		ManualPerson person = new ManualPerson();
		person.setUuid(file);
		var isEmpty = new Object() {
			boolean state = true;
		};
		List<ObjectFieldSetter> executorList = List.of(
				ObjectFieldSetter.of(ColumnName.CNUM.name(), person::setCnum),
				ObjectFieldSetter.of(ColumnName.LNAME_UK.name(), person::setLnameUk),
				ObjectFieldSetter.of(ColumnName.FNAME_UK.name(), person::setFnameUk),
				ObjectFieldSetter.of(ColumnName.PNAME_UK.name(), person::setPnameUk),
				ObjectFieldSetter.of(ColumnName.LNAME_RU.name(), person::setLnameRu),
				ObjectFieldSetter.of(ColumnName.FNAME_RU.name(), person::setFnameRu),
				ObjectFieldSetter.of(ColumnName.PNAME_RU.name(), person::setPnameRu),
				ObjectFieldSetter.of(ColumnName.LNAME_EN.name(), person::setLnameEn),
				ObjectFieldSetter.of(ColumnName.FNAME_EN.name(), person::setFnameEn),
				ObjectFieldSetter.of(ColumnName.PNAME_EN.name(), person::setPnameEn),
				ObjectFieldSetter.of(ColumnName.BIRTHDAY.name(), person::setBirthday),
				ObjectFieldSetter.of(ColumnName.OKPO.name(), person::setOkpo),
				ObjectFieldSetter.of(ColumnName.COUNTRY.name(), person::setCountry),
				ObjectFieldSetter.of(ColumnName.ADDRESS.name(), person::setAddress),
				ObjectFieldSetter.of(ColumnName.PHONE.name(), person::setPhone),
				ObjectFieldSetter.of(ColumnName.EMAIL.name(), person::setEmail),
				ObjectFieldSetter.of(ColumnName.BIRTH_PLACE.name(), person::setBirthPlace),
				ObjectFieldSetter.of(ColumnName.SEX.name(), person::setSex),
				ObjectFieldSetter.of(ColumnName.COMMENT.name(), person::setComment),
				ObjectFieldSetter.of(ColumnName.PASS_LOCAL_NUM.name(), person::setPassLocalNum),
				ObjectFieldSetter.of(ColumnName.PASS_LOCAL_SERIAL.name(), person::setPassLocalSerial),
				ObjectFieldSetter.of(ColumnName.PASS_LOCAL_ISSUER.name(), person::setPassLocalIssuer),
				ObjectFieldSetter.of(ColumnName.PASS_LOCAL_ISSUE_DATE.name(), person::setPassLocalIssueDate),
				ObjectFieldSetter.of(ColumnName.PASS_INT_NUM.name(), person::setPassIntNum),
				ObjectFieldSetter.of(ColumnName.PASS_INT_REC_NUM.name(), person::setPassIntRecNum),
				ObjectFieldSetter.of(ColumnName.PASS_INT_ISSUER.name(), person::setPassIntIssuer),
				ObjectFieldSetter.of(ColumnName.PASS_INT_ISSUE_DATE.name(), person::setPassIntIssueDate),
				ObjectFieldSetter.of(ColumnName.PASS_ID_NUM.name(), person::setPassIdNum),
				ObjectFieldSetter.of(ColumnName.PASS_ID_REC_NUM.name(), person::setPassIdRecNum),
				ObjectFieldSetter.of(ColumnName.PASS_ID_ISSUER.name(), person::setPassIdIssuer),
				ObjectFieldSetter.of(ColumnName.PASS_ID_ISSUE_DATE.name(), person::setPassIdIssueDate)
		);
		executorList.forEach(executor -> {
			String column = executor.getColumn();
			if (header.contains(column) && !StringUtils.isBlank(line.get(header.indexOf(column)))) {
				String value = line.get(header.indexOf(column)).replace("\"", "");
				executor.getFieldSetter().accept(value);
				isEmpty.state = false;
			}
		});
		String column = ColumnName.MK_ID.name();
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

	private Set<ManualTag> addManualTag(List<String> line, List<String> header, ManualPerson person,
	                                    String column, FileDescription file) {
		Set<ManualTag> tags = new HashSet<>();
		if (header.contains(column) && checkTagColumns(header, file).equals("")) {
			List<Integer> idTagIndexesList = findDuplicateColumn(ColumnName.MK_ID.name(), header);
			for (int i = 0; i < idTagIndexesList.size(); i++) {
				var isEmptyTag = new Object() {
					boolean state = true;
				};
				ManualTag tag = new ManualTag();
				int index = i;
				List<ObjectFieldSetter> executorTagList = List.of(
						ObjectFieldSetter.of(ColumnName.MK_ID.name(), tag::setMkId),
						ObjectFieldSetter.of(ColumnName.MK_EVENT_DATE.name(), tag::setMkEventDate),
						ObjectFieldSetter.of(ColumnName.MK_START.name(), tag::setMkStart),
						ObjectFieldSetter.of(ColumnName.MK_EXPIRE.name(), tag::setMkExpire),
						ObjectFieldSetter.of(ColumnName.MK_NUMBER_VALUE.name(), tag::setMkNumberValue),
						ObjectFieldSetter.of(ColumnName.MK_TEXT_VALUE.name(), tag::setMkTextValue),
						ObjectFieldSetter.of(ColumnName.MK_DESCRIPTION.name(), tag::setMkDescription),
						ObjectFieldSetter.of(ColumnName.MK_SOURCE.name(), tag::setMkSource));

				executorTagList.forEach(executor -> {
					String tagColumn = executor.getColumn();
					List<Integer> listValue = findDuplicateColumn(tagColumn, header);
					if (header.contains(tagColumn) && !StringUtils.isBlank(line.get(listValue.get(index)))) {
						String value = line.get(listValue.get(index)).replace("\"", "");
						executor.getFieldSetter().accept(value);
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

	private String checkTagColumns(List<String> header, FileDescription file) {
		List<Integer> idTagIndexesList = findDuplicateColumn(ColumnName.MK_ID.name(), header);
		List<Integer> eventDateIndexesList = findDuplicateColumn(ColumnName.MK_EVENT_DATE.name(), header);
		List<Integer> startIndexesList = findDuplicateColumn(ColumnName.MK_START.name(), header);
		List<Integer> expireIndexesList = findDuplicateColumn(ColumnName.MK_EXPIRE.name(), header);
		List<Integer> numberValueIndexesList = findDuplicateColumn(ColumnName.MK_NUMBER_VALUE.name(), header);
		List<Integer> textValueIndexesList = findDuplicateColumn(ColumnName.MK_TEXT_VALUE.name(), header);
		List<Integer> descriptionIndexesList = findDuplicateColumn(ColumnName.MK_DESCRIPTION.name(), header);
		List<Integer> sourceIndexesList = findDuplicateColumn(ColumnName.MK_SOURCE.name(), header);

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

		List<ManualPersonStatus> peopleStatus = ManualPersonValidator.manualPersonValidate(manualPeople);

		List<ManualTagStatus> tagsStatus = manualTagValidator.validate(
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
        FileDescription description = fileDescriptionRepository.findByUuid(id).orElseThrow(() ->
                new EntityNotFoundException(FileDescription.class, id));
        if (!description.isValidated()) throw new IllegalApiArgumentException("Файл містить помилки");
		String jo;
		try {
			jo = new ObjectMapper().writeValueAsString(new EnricherPortionMessage(MANUAL_PERSON, id));
			log.info("Sending task to {}", enricherQueue);
			template.convertAndSend(enricherQueue, jo);
		} catch (JsonProcessingException e) {
			log.error("Couldn't convert json: {}", e.getMessage());
		}
	}

	public ValidatedManualPersonResponse update(Long id, int index, String value) {
		return manualPersonRepository.findById(id).isPresent()
				? updateManualPerson(id, index, value)
				: updateManualTag(id, index, value);
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
                t -> t.setMkId(UtilString.toUpperCase(value)),
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
