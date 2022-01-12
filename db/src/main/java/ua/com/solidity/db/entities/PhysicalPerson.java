package ua.com.solidity.db.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "physical_person")
@Entity
public class PhysicalPerson {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", unique = true)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "uuid", referencedColumnName = "uuid")
    private FileDescription uuid;

    @Column(name = "customer")
    private String customer; // Кастомер (index0)
    @Column(name = "surname_uk")
    private String surnameUk; // Фамилия укр (index1)
    @Column(name = "name_uk")
    private String nameUk; // Имя укр (index2)
    @Column(name = "patronymic_uk")
    private String patronymicUk; // Отчество укр (index3)
    @Column(name = "surname_ru")
    private String surnameRu; // Фамилия рус (index4)
    @Column(name = "name_ru")
    private String nameRu; // Имя рус (index5)
    @Column(name = "patronymic_ru")
    private String patronymicRu; // Отчество рус (index6)
    @Column(name = "surname_en")
    private String surnameEn; // Фамилия англ (index7)
    @Column(name = "name_en")
    private String nameEn; // Имя англ (index8)
    @Column(name = "patronymic_en")
    private String patronymicEn; // Отчество англ (index9)
    @Column(name = "birth_day")
    private String birthDay; // Дата рождения (index10)
    @Column(name = "INN")
    private String INN; // ИНН (index11)
    @Column(name = "local_passport_code")
    private String localPassportCode; // Номер паспорта книжки (index12)
    @Column(name = "local_passport_series")
    private String localPassportSeries; // Серия паспорта книжки (index13)
    @Column(name = "local_passport_authority")
    private String localPassportAuthority; // Орган выдачи паспорта книжки (index14)
    @Column(name = "local_passport_date")
    private String localPassportDate; // Дата выдачи паспорта книжки (index15)
    @Column(name = "foreign_passport_number")
    private String foreignPassportNumber; // Номер заграничного паспорта (index16)
    @Column(name = "foreign_passport_record_number")
    private String foreignPassportRecordNumber; // Номер записи заграничного паспорта (index17)
    @Column(name = "foreign_passport_authority")
    private String foreignPassportAuthority; // Орган выдачи заграничного паспорта (index18)
    @Column(name = "foreign_passport_date")
    private String foreignPassportDate; // Дата выдачи заграничного паспорта (index19)
    @Column(name = "id_passport_number")
    private String idPassportNumber; // Номер ID паспорта (index20)
    @Column(name = "id_passport_record_number")
    private String idPassportRecordNumber; // Номер записи ID паспорта (index21)
    @Column(name = "id_passport_authority")
    private String idPassportAuthority; // Орган выдачи ID паспорта (index22)
    @Column(name = "id_passport_date")
    private String idPassportDate; // Дата выдачи ID паспорта (index23)
    @Column(name = "death_tag")
    private String deathTag; // Метка умершего (index24)
    @Column(name = "death_date")
    private String deathDate; // Дата смерти (index25)
    @Column(name = "death_notification_date")
    private String deathNotificationDate; // Дата получения информации о смерти (index26)
    @Column(name = "death_notification_source")
    private String deathNotificationSource; // Источник информации о смерти (index27)
    @Column(name = "black_list_tag_n")
    private String blackListTagN; // Метка черного списка N (index28)
    @Column(name = "black_list_date_from_n")
    private String blackListDateFromN; // Дата начала действия черного списка N (index29)
    @Column(name = "black_list_date_to_n")
    private String blackListDateToN; // Дата окончания действия черного списка N (index30)
    @Column(name = "ellipsis")
    private String ellipsis; // … (index31)
    @Column(name = "comment")
    private String comment; // Комментарий (index32)
    @Column(name = "citizenship")
    private String citizenship; // Гражданство (index33)
    @Column(name = "living_addres")
    private String livingAddress; // Адрес проживания (index34)
    @Column(name = "phone_number")
    private String phoneNumber; // Номер телефона (index35)
    @Column(name = "email")
    private String email; // Адрес эл. почты (index36)
    @Column(name = "birth_place")
    private String birthPlace; // место рождения (index37)
    @Column(name = "sex")
    private String sex; // пол (index38)
    @Column(name = "sensitive_information_tag")
    private String sensitiveInformationTag; // метка (вид) чувствительной информации (index39)
    @Column(name = "relation_tag")
    private String relationTag; // метка связи (index40)
    @Column(name = "bank_products")
    private String bankProducts; // продукты банка (index41)

}
