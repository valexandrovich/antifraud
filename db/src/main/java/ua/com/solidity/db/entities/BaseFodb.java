package ua.com.solidity.db.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
public class BaseFodb {
    @Id
    private UUID id;
    private UUID revision;
    private String inn;
    private String lastNameUa;
    private String lastNameRu;
    private String firstNameUa;
    private String firstNameRu;
    private String middleNameUa;
    private String middleNameRu;
    private String sex;
    private LocalDate dateInnCreate;
    private LocalDate birthdate;
    private String birthCountry;
    private String birthRegion;
    private String birthCounty;
    private String birthCityType;
    private String birthCityUa;
    private String birthCityForeign;
    private String liveCountry;
    private String liveRegion;
    private String liveCounty;
    private String liveCityType;
    private String liveCityUa;
    private String liveStreetType;
    private String liveStreet;
    private String liveBuildingNumber;
    private String liveBuildingLetter;
    private String liveBuildingPart;
    private String liveBuildingApartment;
    private String liveCityForeign;
    private String livePhone;
    private LocalDate liveCodeRegisterDate;
    @Column(name="portion_id")
    private UUID portionId;

}
