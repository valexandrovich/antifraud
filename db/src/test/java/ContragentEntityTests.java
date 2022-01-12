import org.junit.Test;
import ua.com.solidity.db.entities.ContragentEntity;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ContragentEntityTests {
    @Test
    public void contragentEntityCouldBeConstructedTest() {
        ContragentEntity ce = new ContragentEntity();
        ce.setId(984270L);
        ce.setName("БЄЛОЄНКО ВАЛЕРІЙ ОЛЕКСАНДРОВИЧ");
        ce.setClientName("ВАЛЕРІЙ");
        ce.setClientPatronymicName("ОЛЕКСАНДРОВИЧ");
        ce.setClientLastName("БЄЛОЄНКО");
        ce.setIdentifyCode("3435603818");
        ce.setAddrCountryId(804L);
        ce.setAddrPostCode("00000");
        ce.setAddrCity("КИЇВ");
        ce.setAddrStreet("Марічанська");
        ce.setAddrHouseNo("13");
        ce.setAddrFlat("72");
        ce.setAddress("00000, м. КИЇВ, вул. Марічанська, буд. 13, кв. 72");
        ce.setPassportType(1L);
        ce.setPassportIssueDate(LocalDate.of(2010, 3, 23));
        ce.setPassportIssuePlace("Калінінським РВ Горлівського ГУ МВС України в Донецькій області");
        ce.setInsiderId(99L);
        ce.setCountryId(804L);
        ce.setContragentTypeId("5");
        ce.setBusinessType1(0L);
        ce.setOwnershipTypeId(10L);
        ce.setRegisterDate(LocalDate.of(2013, 11, 29));
        ce.setGender("M");
        ce.setClientBirthday(LocalDate.of(1994, 1, 23));
        ce.setContragentStateId(2L);
        ce.setAlternateName("VALERII BIELOIENKO");
        ce.setBirthplace("Україна, Горлівка, м, Горлівка");
        ce.setEmail("beloenko23@gmail.com");
        ce.setJurAddrCity("ГОРЛІВКА");
        ce.setJurAddrCountryId(804L);
        ce.setJurAddrFlat("311");
        ce.setJurAddrHouseNo("7");
        ce.setJurAddrPostcode("00000");
        ce.setJurAddrRegion("ДОНЕЦЬКА ОБЛАСТЬ");
        ce.setJurAddrStreet("Магістральна");
        ce.setJuridicalAddress("00000, ГОРЛІВКА, вул. Магістральна, буд. 7, кв. 311");
        ce.setLastModified(LocalDate.of(2021, 7, 26)); // TODO: 2021-07-26 11:34:16
        ce.setMobilePhone("+380638702767");
        ce.setPhones("+380661029212");
        ce.setFamilyStatusId(1L);
        ce.setPassportNo("702215");
        ce.setPassportSerial("ВК");
        ce.setPhoneHome("380638702767");
        ce.setIsPublicPerson(0L);
        ce.setPassportEndDate(LocalDate.of(2039, 2, 23));
        ce.setFop("0");
        ce.setWorkPosition("Висококваліфік. фахівець");
        ce.setCitizenshipCountryId(804L);
        ce.setWorkplace("ОТП Банк");
        assertEquals(Long.valueOf(984270L), ce.getId());
    }

    @Test
    public void emptyContragentEntitiesAreNotEqualTest() {
        ContragentEntity contragentEntity1 = new ContragentEntity();
        ContragentEntity contragentEntity2 = new ContragentEntity();
        assertNotEquals(contragentEntity1, contragentEntity2);
    }
}
