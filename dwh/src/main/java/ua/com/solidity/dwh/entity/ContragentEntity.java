package ua.com.solidity.dwh.entity;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Table(name = "contragent")
@Entity
@Getter
@Setter
@AllArgsConstructor
@ToString
@RequiredArgsConstructor
public class ContragentEntity {
    @Id
    private UUID uuid;
    @Column(nullable = false)
    private Long id; // Код контрагента
    @Column(length = 254, nullable = false)
    private String name; // Полное наименование
    @Column(name = "contragenttypeid", length = 5, columnDefinition = "CHAR(5)")
    private String contragentTypeId; // ID типа контрагента
    @Column(name = "insiderid")
    private Long insiderId; // ID инсайдера (K060)
    @Column(name = "countryid")
    private Long countryId; // ID страны (K040)
    @Column(name = "ownershiptypeid")
    private Long ownershipTypeId; // ID формы собственности (K080)
    @Column(name = "identifycode", length = 14)
    private String identifyCode; // Идентификационный код(ОКПО)
    @Column(length = 254)
    private String address; // Адрес
    @Column(name = "businesstype1")
    private Long businessType1; // ID вида экономической деятельности 1 (K110)
    @Column(name = "businesstype2")
    private Long businessType2; // ID вида экономической деятельности 2 (K110)
    @Column(name = "businesstype3")
    private Long businessType3; // ID вида экономической деятельности 3 (K110)
    @Column(name = "businesstype4")
    private Long businessType4; // ID вида экономической деятельности 4 (K110)
    @Column(name = "businesstype5")
    private Long businessType5; // ID вида экономической деятельности 5 (K110)
    @Column(name = "contragentstateid")
    private Long contragentStateId; // ID состояния контрагента
    @Column(name = "alternatename", length = 38)
    private String alternateName; // Альтернативное наименование
    @Column(name = "registerdate", columnDefinition = "DATE")
    private LocalDate registerDate; // Дата заведения/регистрации контрагента в банке
    @Column(name = "nalogregisterdate", columnDefinition = "DATE")
    private LocalDate nalogRegisterDate; // Дата регистрации в налоговой
    @Column(name = "juridicaladdress", length = 254)
    private String juridicalAddress; // Юридический адрес
    @Column(name = "stateregisterno", length = 38)
    private String stateRegisterNo; // Номер свидетельства о регистрации в госадминистрации
    @Column(name = "stateregisterdate", columnDefinition = "DATE")
    private LocalDate stateRegisterDate; // Дата регистрации в госадминистрации
    @Column(name = "stateregisterplace", length = 254)
    private String stateRegisterPlace; // Место регистрации в госадминистрации
    @Column(name = "addr_countryid")
    private Long addrCountryId; // Адрес:Код страны
    @Column(name = "addr_postcode", length = 10)
    private String addrPostCode; // Адрес:Индекс
    @Column(name = "addr_region", length = 40)
    private String addrRegion; // Адрес:Область
    @Column(name = "addr_district", length = 40)
    private String addrDistrict; // Адрес:Район
    @Column(name = "addr_city", length = 60)
    private String addrCity; // Адрес:Город
    @Column(name = "addr_street", length = 50)
    private String addrStreet; // Адрес:Улица
    @Column(name = "addr_houseno", length = 30)
    private String addrHouseNo; // Адрес:Номер дома
    @Column(name = "addr_flat", length = 20)
    private String addrFlat; // Адрес:Номер квартиры/офиса
    @Column(name = "juraddr_countryid")
    private Long jurAddrCountryId; // ЮрАдрес:Код страны
    @Column(name = "juraddr_postcode", length = 10)
    private String jurAddrPostcode; // ЮрАдрес:Индекс
    @Column(name = "juraddr_region", length = 40)
    private String jurAddrRegion; // ЮрАдрес:Область
    @Column(name = "juraddr_district", length = 40)
    private String jurAddrDistrict; // ЮрАдрес:Район
    @Column(name = "juraddr_city", length = 60)
    private String jurAddrCity; // ЮрАдрес:Город
    @Column(name = "juraddr_street", length = 50)
    private String jurAddrStreet; // ЮрАдрес:Улица
    @Column(name = "juraddr_houseno", length = 30)
    private String jurAddrHouseNo; // ЮрАдрес:Номер дома
    @Column(name = "juraddr_flat", length = 20)
    private String jurAddrFlat; // ЮрАдрес:Номер квартифы/офиса
    @Column(name = "closedate", columnDefinition = "DATE")
    private LocalDate closeDate; // Дата закрытия контрагента
    @Column(name = "lastmodified", columnDefinition = "DATE")
    private LocalDate lastModified; // Дата последнего изменения
    @Column(name = "passporttype")
    private Long passportType; // Тип документа, удостоверяющего личность
    @Column(name = "passportissuedate", columnDefinition = "DATE")
    private LocalDate passportIssueDate; // Дата выдачи документа
    @Column(name = "passportissueplace", length = 120)
    private String passportIssuePlace; // Место выдачи документа
    @Column(name = "clientname", length = 38)
    private String clientName; // Имя
    @Column(name = "clientpatronymicname", length = 38)
    private String clientPatronymicName; // Отчество
    @Column(name = "clientlastname", length = 38)
    private String clientLastName; // Фамилия
    @Column(name = "clientbirthday", columnDefinition = "DATE")
    private LocalDate clientBirthday; // День рождения \ основания
    @Column(length = 60)
    private String birthplace; // Место рождения
    @Column(length = 1)
    private String gender; // Пол (М\Ж)
    @Column(length = 120)
    private String phones; // Контактная информация
    @Column(name = "mobilephone", length = 60)
    private String mobilePhone; // Мобильный телефон
    @Column(length = 60)
    private String email; // Адрес эл. почты
    @Column(name = "bad_status_flag")
    private Long badStatusFlag; // Флаг "Ненадежный клиент"
    @Column(name = "passport_serial", length = 9)
    private String passportSerial; // Паспорт: серия
    @Column(name = "passport_no", length = 25)
    private String passportNo; // Паспорт: номер
    @Column(name = "phone_home", length = 25)
    private String phoneHome; // Домашний телефон
    @Column(name = "family_statusid")
    private Long familyStatusId; // Семейный статус
    @Column(name = "lastname_lat", length = 38) // Not present in B2 description
    private String lastNameLat; // Фамилия латиницей
    @Column(name = "firstname_lat", length = 38) // Not present in B2 description
    private String firstNameLat; // Имя латиницей
    @Column(name = "citizenshipcountryid")
    private Long citizenshipCountryId; // Страна гражданства // Not present in B2 description
    @Column(length = 254) // Not present in B2 description
    private String workplace; // Место работы
    @Column(name = "ispublicperson")
    private Long isPublicPerson; // Признак "Публичное лицо"
    @Column(name = "workposition", length = 254) // Not present in B2 description
    private String workPosition; // Должность
    @Column(name = "passportphotoexists")
    private Integer passportPhotoExists; // Вклеена фотография в Укр. паспорт: 1-  16лет, 2- 25 лет, 3 - 45 лет, NULL  -не известно  // Not present in B2 description
    @Column(name = "passportenddate", columnDefinition = "DATE")
    private LocalDate passportEndDate; // Дата окончания действия Документа
    @Column(name = "businesstype6")
    private Long businessType6; // ID вида экономической деятельности 6 // Not present in B2 description
    @Column(name = "businesstype7")
    private Long businessType7; // ID вида экономической деятельности 7 // Not present in B2 description
    @Column(name = "businesstype8")
    private Long businessType8; // ID вида экономической деятельности 8 // Not present in B2 description
    @Column(name = "businesstype9")
    private Long businessType9; // ID вида экономической деятельности 9 // Not present in B2 description
    @Column(name = "businesstype10")
    private Long businessType10; // ID вида экономической деятельности 10 // Not present in B2 description
    @Column(name = "citizenshipcountryid2")
    private Long citizenshipCountryId2; // страна 2го гражданства // Not present in B2 description
    @Column
    private Long fop; // ФОП // Not present in B2 description
    @Column(name = "arcdate", columnDefinition = "DATE")
    private LocalDate arcDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ContragentEntity that = (ContragentEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
