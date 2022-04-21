package ua.com.solidity.web.service.validator;

import ua.com.solidity.db.entities.PhysicalPerson;
import ua.com.solidity.web.response.secondary.CellStatus;
import static ua.com.solidity.web.service.validator.PhysicalPersonRegex.*;

import java.util.ArrayList;
import java.util.List;

public class PhysicalPersonValidator {

	public static List<CellStatus> validatePhysicalPerson(List<PhysicalPerson> personList) {
		List<CellStatus> cellStatusList = new ArrayList<>();

		for (PhysicalPerson person : personList) {
			Long personId = person.getId();
			if (!valid(person.getCustomer(), BOOLEAN_TAG.getRegex()))
				cellStatusList.add(new CellStatus(personId, 0, BOOLEAN_TAG.getMessage())); // 0
			if (!valid(person.getSurnameUk(), NAME_UK.getRegex()))
				cellStatusList.add(new CellStatus(personId, 1, NAME_UK.getMessage())); // 1
			if (!valid(person.getNameUk(), NAME_UK.getRegex()))
				cellStatusList.add(new CellStatus(personId, 2, NAME_UK.getMessage())); // 2
			if (!valid(person.getPatronymicUk(), NAME_UK.getRegex()))
				cellStatusList.add(new CellStatus(personId, 3, NAME_UK.getMessage())); // 3
			if (!valid(person.getSurnameRu(), NAME_RU.getRegex()))
				cellStatusList.add(new CellStatus(personId, 4, NAME_RU.getMessage())); // 4
			if (!valid(person.getNameRu(), NAME_RU.getRegex()))
				cellStatusList.add(new CellStatus(personId, 5, NAME_RU.getMessage())); // 5
			if (!valid(person.getPatronymicRu(), NAME_RU.getRegex()))
				cellStatusList.add(new CellStatus(personId, 6, NAME_RU.getMessage())); // 6
			if (!valid(person.getSurnameEn(), NAME_EN.getRegex()))
				cellStatusList.add(new CellStatus(personId, 7, NAME_EN.getMessage())); // 7
			if (!valid(person.getNameEn(), NAME_EN.getRegex()))
				cellStatusList.add(new CellStatus(personId, 8, NAME_EN.getMessage())); // 8
			if (!valid(person.getPatronymicEn(), NAME_EN.getRegex()))
				cellStatusList.add(new CellStatus(personId, 9, NAME_EN.getMessage())); // 9
			if (!valid(person.getBirthDay(), DATE.getRegex()))
				cellStatusList.add(new CellStatus(personId, 10, DATE.getMessage())); // 10
			if (!valid(person.getINN(), INN.getRegex()))
				cellStatusList.add(new CellStatus(personId, 11, INN.getMessage())); // 11
			if (!valid(person.getLocalPassportCode(), LOCAL_PASSPORT_NUMBER.getRegex()))
				cellStatusList.add(new CellStatus(personId, 12, LOCAL_PASSPORT_NUMBER.getMessage())); // 12
			if (!valid(person.getLocalPassportSeries(), LOCAL_PASSPORT_SERIES.getRegex()))
				cellStatusList.add(new CellStatus(personId, 13, LOCAL_PASSPORT_SERIES.getMessage())); // 13
			if (!valid(person.getLocalPassportAuthority(), UK_RU_MULTIPLE.getRegex()))
				cellStatusList.add(new CellStatus(personId, 14, UK_RU_MULTIPLE.getMessage())); // 14
			if (!valid(person.getLocalPassportDate(), DATE.getRegex()))
				cellStatusList.add(new CellStatus(personId, 15, DATE.getMessage())); // 15
			if (!valid(person.getForeignPassportNumber(), FOREIGN_PASSPORT_NUMBER.getRegex()))
				cellStatusList.add(new CellStatus(personId, 16, FOREIGN_PASSPORT_NUMBER.getMessage())); // 16
			if (!valid(person.getForeignPassportRecordNumber(), FOREIGN_PASSPORT_RECORD_NUMBER.getRegex()))
				cellStatusList.add(new CellStatus(personId, 17, FOREIGN_PASSPORT_RECORD_NUMBER.getMessage())); // 17
			if (!valid(person.getForeignPassportAuthority(), FOREIGN_PASSPORT_AUTHORITY.getRegex()))
				cellStatusList.add(new CellStatus(personId, 18, FOREIGN_PASSPORT_AUTHORITY.getMessage())); // 18
			if (!valid(person.getForeignPassportDate(), DATE.getRegex()))
				cellStatusList.add(new CellStatus(personId, 19, DATE.getMessage())); // 19
			if (!valid(person.getIdPassportNumber(), ID_PASSPORT_NUMBER.getRegex()))
				cellStatusList.add(new CellStatus(personId, 20, ID_PASSPORT_NUMBER.getMessage())); // 20
			if (!valid(person.getIdPassportRecordNumber(), ID_PASSPORT_RECORD_NUMBER.getRegex()))
				cellStatusList.add(new CellStatus(personId, 21, ID_PASSPORT_RECORD_NUMBER.getMessage())); // 21
			if (!valid(person.getIdPassportAuthority(), ID_PASSPORT_AUTHORITY.getRegex()))
				cellStatusList.add(new CellStatus(personId, 22, ID_PASSPORT_AUTHORITY.getMessage())); // 22
			if (!valid(person.getIdPassportDate(), DATE.getRegex()))
				cellStatusList.add(new CellStatus(personId, 23, DATE.getMessage())); // 23
			if (!valid(person.getDeathTag(), BOOLEAN_TAG.getRegex()))
				cellStatusList.add(new CellStatus(personId, 24, BOOLEAN_TAG.getMessage())); // 24
			if (!valid(person.getDeathDate(), DATE.getRegex()))
				cellStatusList.add(new CellStatus(personId, 25, DATE.getMessage())); // 25
			if (!valid(person.getDeathNotificationDate(), DATE.getRegex()))
				cellStatusList.add(new CellStatus(personId, 26, DATE.getMessage())); // 26
			if (!valid(person.getDeathNotificationSource(), null))
				cellStatusList.add(new CellStatus(personId, 27, "")); // 27
			if (!valid(person.getBlackListTagN(), BOOLEAN_TAG.getRegex()))
				cellStatusList.add(new CellStatus(personId, 28, BOOLEAN_TAG.getMessage())); // 28
			if (!valid(person.getBlackListDateFromN(), DATE.getRegex()))
				cellStatusList.add(new CellStatus(personId, 29, DATE.getMessage())); // 29
			if (!valid(person.getBlackListDateToN(), DATE.getRegex()))
				cellStatusList.add(new CellStatus(personId, 30, DATE.getMessage())); // 30
			if (!valid(person.getEllipsis(), null))
				cellStatusList.add(new CellStatus(personId, 31, "")); // 31
			if (!valid(person.getComment(), UK_RU_EN_MULTIPLE.getRegex()))
				cellStatusList.add(new CellStatus(personId, 32, UK_RU_EN_MULTIPLE.getMessage())); // 32
			if (!valid(person.getCitizenship(), UK_RU_EN_SINGLE.getRegex()))
				cellStatusList.add(new CellStatus(personId, 33, UK_RU_EN_SINGLE.getMessage())); // 33
			if (!valid(person.getLivingAddress(), UK_RU_EN_MULTIPLE.getRegex()))
				cellStatusList.add(new CellStatus(personId, 34, UK_RU_EN_MULTIPLE.getMessage())); // 34
			if (!valid(person.getPhoneNumber(), PHONE_NUMBER.getRegex()))
				cellStatusList.add(new CellStatus(personId, 35, PHONE_NUMBER.getMessage())); // 35
			if (!valid(person.getEmail(), EMAIL.getRegex()))
				cellStatusList.add(new CellStatus(personId, 36, EMAIL.getMessage())); // 36
			if (!valid(person.getBirthPlace(), UK_RU_EN_SINGLE.getRegex()))
				cellStatusList.add(new CellStatus(personId, 37, UK_RU_EN_SINGLE.getMessage())); // 37
			if (!valid(person.getSex(), GENDER.getRegex()))
				cellStatusList.add(new CellStatus(personId, 38, GENDER.getMessage())); // 38
			if (!valid(person.getSensitiveInformationTag(), UK_RU_EN_MULTIPLE.getRegex()))
				cellStatusList.add(new CellStatus(personId, 39, UK_RU_EN_MULTIPLE.getMessage())); // 39
			if (!valid(person.getRelationTag(), BOOLEAN_TAG.getRegex()))
				cellStatusList.add(new CellStatus(personId, 40, BOOLEAN_TAG.getMessage())); // 40
			if (!valid(person.getBankProducts(), UK_RU_EN_MULTIPLE.getRegex()))
				cellStatusList.add(new CellStatus(personId, 41, UK_RU_EN_MULTIPLE.getMessage())); // 41
		}
		return cellStatusList;
	}

	private static boolean valid(String value, String regex) {
		if (value == null || regex == null) return true;
		return value.matches(regex);
	}

}
// + toUpperCase
// - toLowerCase

//	A   0  BOOLEAN_TAG
//+	B   1  NAME_UK
//+	C   2  NAME_UK
//+	D   3  NAME_UK
//+	E   4  NAME_RU
//+	F   5  NAME_RU
//+	G   6  NAME_RU
//+	H   7  NAME_EN
//+	I   8  NAME_EN
//+	J   9  NAME_EN
//	K   10 DATE
//	L   11 INN
//	M   12 LOCAL_PASSPORT_NUMBER
//+	N   13 LOCAL_PASSPORT_SERIES
//+	O   14 UK_RU_MULTIPLE
//	P   15 DATE
//+	Q   16 FOREIGN_PASSPORT_NUMBER
//	R   17 FOREIGN_PASSPORT_RECORD_NUMBER
//	S   18 FOREIGN_PASSPORT_AUTHORITY
//	T   19 DATE
//	U   20 ID_PASSPORT_NUMBER
//	V   21 ID_PASSPORT_RECORD_NUMBER
//	W   22 ID_PASSPORT_AUTHORITY
//  X   23 DATE
//  Y   24 BOOLEAN_TAG
//  Z   25 DATE
//  AA  26 DATE
//+ AB  27 *** not needed ***
//  AC  28 BOOLEAN_TAG
//  AD  29 DATE
//  AE  30 DATE
//  AF  31 *** not needed ***
//+ AG  32 UK_RU_EN_MULTIPLE
//+ AH  33 UK_RU_EN_SINGLE
//+ AI  34 UK_RU_EN_MULTIPLE
//  AJ  35 PHONE_NUMBER
//- AK  36 EMAIL
//+ AL  37 UK_RU_EN_SINGLE
//+ AM  38 GENDER
//+ AN  39 UK_RU_EN_MULTIPLE
//  AO  40 BOOLEAN_TAG
//+ AP  41 UK_RU_EN_MULTIPLE