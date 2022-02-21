package ua.com.solidity.dwh.entities;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.Objects;

@Table(name = "B2_OLAP.AR_CONTRAGENT")
@Entity
@Getter
@Setter
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class ArContragent {
    @Id
    @Column(name = "SESSIONID")
    private Long sessionId;
    @Column(name = "ARCDATE", columnDefinition = "DATE")
    private LocalDate arcDate;
    @Column(name = "ID", nullable = false)
    private Long id; // Код контрагента
    @Column(name = "NAME", nullable = false)
    private String name; // Полное наименование
    @Column(name = "CLIENTNAME")
    private String clientName; // Имя
    @Column(name = "CLIENTPATRONYMICNAME")
    private String clientPatronymicName; // Отчество
    @Column(name = "CLIENTLASTNAME")
    private String clientLastName; // Фамилия
    @Column(name = "IDENTIFYCODE")
    private String identifyCode; // Идентификационный код(ОКПО)
    @Column(name = "ADDR_COUNTRYID")
    private Long addrCountryId; // Адрес:Код страны
    @Column(name = "ADDR_POSTCODE")
    private String addrPostCode; // Адрес:Индекс
    @Column(name = "ADDR_REGION")
    private String addrRegion; // Адрес:Область
    @Column(name = "ADDR_DISTRICT")
    private String addrDistrict; // Адрес:Район
    @Column(name = "ADDR_CITY")
    private String addrCity; // Адрес:Город
    @Column(name = "ADDR_STREET")
    private String addrStreet; // Адрес:Улица
    @Column(name = "ADDR_HOUSENO")
    private String addrHouseNo; // Адрес:Номер дома
    @Column(name = "ADDR_FLAT")
    private String addrFlat; // Адрес:Номер квартиры/офиса
    @Column(name = "ADDRESS")
    private String address; // Адрес
    @Column(name = "PASSPORTTYPE")
    private Long passportType; // Тип документа, удостоверяющего личность
    @Column(name = "PASSPORTISSUEDATE", columnDefinition = "DATE")
    private LocalDate passportIssueDate; // Дата выдачи документа
    @Column(name = "PASSPORTISSUEPLACE", length = 400)
    private String passportIssuePlace; // Место выдачи документа
    @Column(name = "INSIDERID")
    private Long insiderId; // ID инсайдера (K060)
    @Column(name = "COUNTRYID")
    private Long countryId; // ID страны (K040)
    @Column(name = "CONTRAGENTTYPEID", length = 5, columnDefinition = "CHAR(5)")
    private String contragentTypeId; // ID типа контрагента
    @Column(name = "BUSINESSTYPE1")
    private Long businessType1; // ID вида экономической деятельности 1 (K110)
    @Column(name = "OWNERSHIPTYPEID")
    private Long ownershipTypeId; // ID формы собственности (K080)
    @Column(name = "REGISTERDATE", columnDefinition = "DATE")
    private LocalDate registerDate; // Дата заведения/регистрации контрагента в банке
    @Column(name = "GENDER")
    private String gender; // Пол (М\Ж)
    @Column(name = "CLIENTBIRTHDAY", columnDefinition = "DATE")
    private LocalDate clientBirthday; // День рождения \ основания
    @Column(name = "CLOSEDATE", columnDefinition = "DATE")
    private LocalDate closeDate; // Дата закрытия контрагента
    @Column(name = "CONTRAGENTSTATEID")
    private Long contragentStateId; // ID состояния контрагента
    @Column(name = "ALTERNATENAME")
    private String alternateName; // Альтернативное наименование
    @Column(name = "BAD_STATUS_FLAG")
    private Long badStatusFlag; // Флаг "Ненадежный клиент"
    @Column(name = "BIRTHPLACE")
    private String birthplace; // Место рождения
    @Column(name = "BUSINESSTYPE2")
    private Long businessType2; // ID вида экономической деятельности 2 (K110)
    @Column(name = "BUSINESSTYPE3")
    private Long businessType3; // ID вида экономической деятельности 3 (K110)
    @Column(name = "BUSINESSTYPE4")
    private Long businessType4; // ID вида экономической деятельности 4 (K110)
    @Column(name = "BUSINESSTYPE5")
    private Long businessType5; // ID вида экономической деятельности 5 (K110)
    @Column(name = "EMAIL")
    private String email; // Адрес эл. почты
    @Column(name = "JURADDR_CITY")
    private String jurAddrCity; // ЮрАдрес:Город
    @Column(name = "JURADDR_COUNTRYID")
    private Long jurAddrCountryId; // ЮрАдрес:Код страны
    @Column(name = "JURADDR_DISTRICT")
    private String jurAddrDistrict; // ЮрАдрес:Район
    @Column(name = "JURADDR_FLAT")
    private String jurAddrFlat; // ЮрАдрес:Номер квартифы/офиса
    @Column(name = "JURADDR_HOUSENO")
    private String jurAddrHouseNo; // ЮрАдрес:Номер дома
    @Column(name = "JURADDR_POSTCODE")
    private String jurAddrPostcode; // ЮрАдрес:Индекс
    @Column(name = "JURADDR_REGION")
    private String jurAddrRegion; // ЮрАдрес:Область
    @Column(name = "JURADDR_STREET")
    private String jurAddrStreet; // ЮрАдрес:Улица
    @Column(name = "JURIDICALADDRESS")
    private String juridicalAddress; // Юридический адрес
    @Column(name = "LASTMODIFIED", columnDefinition = "DATE")
    private LocalDate lastModified; // Дата последнего изменения
    @Column(name = "MOBILEPHONE")
    private String mobilePhone; // Мобильный телефон
    @Column(name = "NALOGREGISTERDATE", columnDefinition = "DATE")
    private LocalDate nalogRegisterDate; // Дата регистрации в налоговой
    @Column(name = "PHONES")
    private String phones; // Контактная информация
    @Column(name = "STATEREGISTERDATE", columnDefinition = "DATE")
    private LocalDate stateRegisterDate; // Дата регистрации в госадминистрации
    @Column(name = "STATEREGISTERNO")
    private String stateRegisterNo; // Номер свидетельства о регистрации в госадминистрации
    @Column(name = "STATEREGISTERPLACE")
    private String stateRegisterPlace; // Место регистрации в госадминистрации
    @Column(name = "FAMILY_STATUSID")
    private Long familyStatusId; // Семейный статус
    @Column(name = "PASSPORT_NO")
    private String passportNo; // Паспорт: номер
    @Column(name = "PASSPORT_SERIAL")
    private String passportSerial; // Паспорт: серия
    @Column(name = "PHONE_HOME")
    private String phoneHome; // Домашний телефон
    @Column(name = "ISPUBLICPERSON")
    private Long isPublicPerson; // Признак "Публичное лицо"
    @Column(name = "PASSPORTENDDATE", columnDefinition = "DATE")
    private LocalDate passportEndDate; // Дата окончания действия Документа
    @Column(name = "FOP")
    private Long fop; // ФОП // Not present in B2 description
    @Column(name = "WORKPOSITION") // Not present in B2 description
    private String workPosition; // Должность
    @Column(name = "CITIZENSHIPCOUNTRYID")
    private Long citizenshipCountryId; // Страна гражданства // Not present in B2 description
    @Column(name = "WORKPLACE") // Not present in B2 description
    private String workplace; // Место работы

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ArContragent that = (ArContragent) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
