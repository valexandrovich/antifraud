package ua.com.solidity.web.dto;

import lombok.Data;
import ua.com.solidity.db.entities.ImportSource;
import ua.com.solidity.db.entities.YAddress;
import ua.com.solidity.db.entities.YAltPerson;
import ua.com.solidity.db.entities.YCompanyRelation;
import ua.com.solidity.db.entities.YEmail;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.db.entities.YPassport;
import ua.com.solidity.db.entities.YPhone;
import ua.com.solidity.db.entities.YRelatedPerson;
import ua.com.solidity.db.entities.YTag;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
public class YPersonDto {
	private UUID id;
	private String lastName;
	private String firstName;
	private String patName;
	private LocalDate birthdate;
	private Set<YRelatedPerson> relatedPeople;
	private Set<YCompanyRelation> companyRelations;
	private Set<YINN> inns;
	private Set<YAddress> addresses;
	private Set<YAltPerson> altPeople;
	private Set<YPassport> passports;
	private Set<YTag> tags;
	private Set<YEmail> emails;
	private Set<YPhone> phones;
	private Set<ImportSource> sources;
	private boolean subscribe;
}
