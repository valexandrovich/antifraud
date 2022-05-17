package ua.com.solidity.web.service;

import com.google.common.base.CharMatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ua.com.solidity.db.entities.FileDescription;
import ua.com.solidity.db.entities.PhysicalPerson;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.db.repositories.FileDescriptionRepository;
import ua.com.solidity.db.repositories.PhysicalPersonRepository;
import ua.com.solidity.db.repositories.YPersonRepository;
import ua.com.solidity.web.dto.PhysicalPersonDto;
import ua.com.solidity.web.dto.YPersonDto;
import ua.com.solidity.db.entities.User;
import ua.com.solidity.web.exception.EntityNotFoundException;
import ua.com.solidity.web.exception.IllegalApiArgumentException;
import ua.com.solidity.db.repositories.UserRepository;
import ua.com.solidity.web.request.SearchRequest;
import ua.com.solidity.web.response.ValidatedPhysicalPersonResponse;
import ua.com.solidity.web.response.secondary.CellStatus;
import ua.com.solidity.web.search.GenericSpecification;
import ua.com.solidity.web.search.SearchCriteria;
import ua.com.solidity.web.search.SearchOperation;
import ua.com.solidity.web.security.service.Extractor;
import ua.com.solidity.web.service.converter.YPersonConverter;
import ua.com.solidity.web.service.validator.PhysicalPersonValidator;
import ua.com.solidity.web.utils.UtilString;

import static ua.com.solidity.web.utils.UtilString.toLowerCase;
import static ua.com.solidity.web.utils.UtilString.toUpperCase;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class XslxService {

	private final Extractor extractor;
	private final ModelMapper modelMapper;
	private final PhysicalPersonRepository personRepository;
	private final FileDescriptionRepository fileDescriptionRepository;
	private final YPersonRepository ypr;
	private final YPersonConverter yPersonConverter;
	private final UserRepository userRepository;

    private static final String ALT_PEOPLE = "altPeople";
    private static final String PASSPORTS = "passports";
    private static final String NUMBER = "number";
    private static final String BIRTHDATE = "birthdate";
    private static final String RECORD_NUMBER = "recordNumber";
    private static final String LAST_NAME = "lastName";
    private static final String FIRST_NAME = "firstName";
    private static final String PAT_NAME = "patName";
    private static final String ADDRESS = "address";
    private static final String SERIES = "series";
    private static final String INN = "inn";
    private static final String INNS = "inns";
    private static final String ADDRESSES = "addresses";


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
		return fileDescriptionRepository.findAll();
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

	public List<YPersonDto> search(SearchRequest searchRequest, HttpServletRequest httpServletRequest) {
		boolean criteriaFound = false;
		GenericSpecification<YPerson> gs = new GenericSpecification<>();

		String firstName = Objects.toString(searchRequest.getName().toUpperCase().trim(), ""); // Protection from null
		if (!firstName.equals("")) {
			criteriaFound = true;
            if (ypr.findByFirstName(firstName).isEmpty()) {
                gs.add(new SearchCriteria(FIRST_NAME, firstName, ALT_PEOPLE, SearchOperation.EQUALS));
            } else {
                gs.add(new SearchCriteria(FIRST_NAME, firstName, null, SearchOperation.EQUALS));
            }
		}

		String surName = Objects.toString(searchRequest.getSurname().toUpperCase().trim(), "");
		if (!surName.equals("")) {
			criteriaFound = true;
            if (ypr.findByLastName(surName).isEmpty()) {
                gs.add(new SearchCriteria(LAST_NAME, surName, ALT_PEOPLE, SearchOperation.EQUALS));
            } else {
                gs.add(new SearchCriteria(LAST_NAME, surName, null, SearchOperation.EQUALS));
            }
		}

		String patName = Objects.toString(searchRequest.getPatronymic().toUpperCase().trim(), "");
		if (!patName.equals("")) {
			criteriaFound = true;
            if (ypr.findByPatName(patName).isEmpty()) {
                gs.add(new SearchCriteria(PAT_NAME, patName, ALT_PEOPLE, SearchOperation.EQUALS));
            } else {
                gs.add(new SearchCriteria(PAT_NAME, patName, null, SearchOperation.EQUALS));
            }
		}

		String year = Objects.toString(searchRequest.getYear(), "");
		String month = Objects.toString(searchRequest.getMonth(), "");
		String day = Objects.toString(searchRequest.getDay(), "");
		if (!year.equals("") && !month.equals("") && !day.equals("")) {
			criteriaFound = true;
			gs.add(new SearchCriteria(BIRTHDATE,
			                          LocalDate.of(
					                          Integer.parseInt(year),
					                          Integer.parseInt(month),
					                          Integer.parseInt(day)),
                                      null,
			                          SearchOperation.EQUALS));
		}

		String inn = Objects.toString(searchRequest.getInn(), "");
		if (!inn.equals("")) {
			criteriaFound = true;
            gs.add(new SearchCriteria(INN, inn, INNS, SearchOperation.EQUALS));
		}

		String passportNumber = Objects.toString(searchRequest.getPassportNumber(), "");
		String passportSeries = Objects.toString(searchRequest.getPassportSeria(), "");
		if (!passportNumber.equals("") && !passportSeries.equals("")) {
			criteriaFound = true;
            gs.add(new SearchCriteria(NUMBER, passportNumber, PASSPORTS, SearchOperation.EQUALS));
            gs.add(new SearchCriteria(SERIES, passportSeries, PASSPORTS, SearchOperation.EQUALS));
		}

		String idpassportNumber = Objects.toString(searchRequest.getId_documentNumber(), "");
		String idpassportRecord = Objects.toString(searchRequest.getId_registryNumber(), "");
		if (!idpassportNumber.equals("") && !idpassportRecord.equals("")) {
			criteriaFound = true;
            gs.add(new SearchCriteria(NUMBER, idpassportNumber, PASSPORTS, SearchOperation.EQUALS));
            gs.add(new SearchCriteria(RECORD_NUMBER, idpassportRecord, PASSPORTS, SearchOperation.EQUALS));
		}

        String foreignPassportNumber = Objects.toString(searchRequest.getForeignP_documentNumber(), "");
        String foreignPassportRecord = Objects.toString(searchRequest.getForeignP_registryNumber(), "");
        if (!foreignPassportNumber.equals("") && !foreignPassportRecord.equals("")) {
            criteriaFound = true;
            gs.add(new SearchCriteria(NUMBER, foreignPassportNumber, PASSPORTS, SearchOperation.EQUALS));
            gs.add(new SearchCriteria(RECORD_NUMBER, foreignPassportRecord, PASSPORTS, SearchOperation.EQUALS));
        }

        String address = Objects.toString(UtilString.toUpperCase(searchRequest.getAddress()), "");
        if (!address.equals("")) {
            criteriaFound = true;
            gs.add(new SearchCriteria(ADDRESS, address, ADDRESSES, SearchOperation.MATCH));
        }

        String age = Objects.toString(searchRequest.getAge(), "");
        if (!age.equals("")) {
            criteriaFound = true;
            LocalDate finishDate = LocalDate.now().minusYears(Integer.parseInt(age));
            gs.add(new SearchCriteria(BIRTHDATE, finishDate, null, SearchOperation.BETWEEN));
        }

		if (criteriaFound) {
			List<YPersonDto> yPersonDtoList = ypr.findAll(gs)
					.stream()
					.map(yPersonConverter::toDto)
					.collect(Collectors.toList());
			User user = extractor.extractUser(httpServletRequest);
			yPersonDtoList.forEach(dto -> {
				for (YPerson yPerson : user.getPeople()) {
					if (yPerson.getId() == dto.getId()) dto.setSubscribe(true);
				}
			});
			return yPersonDtoList;
		} else {
			return new ArrayList<>();
		}
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

	public YPersonDto findById(UUID id, HttpServletRequest request) {
		YPerson yPerson = ypr.findById(id)
				.orElseThrow(() -> new EntityNotFoundException(YPerson.class, id));
		YPersonDto dto = yPersonConverter.toDto(yPerson);
		User user = extractor.extractUser(request);
		Optional<YPerson> personOptional = user.getPeople()
				.stream()
				.filter(e -> e.getId() == id)
				.findAny();
		if (personOptional.isPresent()) dto.setSubscribe(true);
		return dto;
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
}