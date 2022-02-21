package ua.com.solidity.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PhysicalPersonDto {

    private Long id;

    private UUID uuid;

    private String customer; // Кастомер (index0)
    private String surnameUk; // Фамилия укр (index1)
    private String nameUk; // Имя укр (index2)
    private String patronymicUk; // Отчество укр (index3)
    private String surnameRu; // Фамилия рус (index4)
    private String nameRu; // Имя рус (index5)
    private String patronymicRu; // Отчество рус (index6)
    private String surnameEn; // Фамилия англ (index7)
    private String nameEn; // Имя англ (index8)
    private String patronymicEn; // Отчество англ (index9)
    private String birthDay; // Дата рождения (index10)
    private String INN; // ИНН (index11)
    private String localPassportCode; // Номер паспорта книжки (index12)
    private String localPassportSeries; // Серия паспорта книжки (index13)
    private String localPassportAuthority; // Орган выдачи паспорта книжки (index14)
    private String localPassportDate; // Дата выдачи паспорта книжки (index15)
    private String foreignPassportNumber; // Номер заграничного паспорта (index16)
    private String foreignPassportRecordNumber; // Номер записи заграничного паспорта (index17)
    private String foreignPassportAuthority; // Орган выдачи заграничного паспорта (index18)
    private String foreignPassportDate; // Дата выдачи заграничного паспорта (index19)
    private String idPassportNumber; // Номер ID паспорта (index20)
    private String idPassportRecordNumber; // Номер записи ID паспорта (index21)
    private String idPassportAuthority; // Орган выдачи ID паспорта (index22)
    private String idPassportDate; // Дата выдачи ID паспорта (index23)
    private String deathTag; // Метка умершего (index24)
    private String deathDate; // Дата смерти (index25)
    private String deathNotificationDate; // Дата получения информации о смерти (index26)
    private String deathNotificationSource; // Источник информации о смерти (index27)
    private String blackListTagN; // Метка черного списка N (index28)
    private String blackListDateFromN; // Дата начала действия черного списка N (index29)
    private String blackListDateToN; // Дата окончания действия черного списка N (index30)
    private String ellipsis; // … (index31)
    private String comment; // Комментарий (index32)
    private String citizenship; // Гражданство (index33)
    private String livingAddress; // Адрес проживания (index34)
    private String phoneNumber; // Номер телефона (index35)
    private String email; // Адрес эл. почты (index36)
    private String birthPlace; // место рождения (index37)
    private String sex; // пол (index38)
    private String sensitiveInformationTag; // метка (вид) чувствительной информации (index39)
    private String relationTag; // метка связи (index40)
    private String bankProducts; // продукты банка (index41)
}
