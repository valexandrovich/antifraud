package ua.com.solidity.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.com.solidity.db.entities.FileDescription;
import ua.com.solidity.db.entities.PhysicalPerson;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PhysicalPersonRepository extends JpaRepository<PhysicalPerson, Long> {

    @Query("SELECT p.uuid FROM PhysicalPerson p GROUP BY p.uuid")
    List<UUID> findUuidGroupByUuid();

    List<PhysicalPerson> findByUuid(FileDescription uuid);

    List<PhysicalPerson> findByNameUkEquals(String name);

    @Query("SELECT p FROM PhysicalPerson p WHERE ((:surnameUk is not null and p.surnameUk = :surnameUk)" +
            " or (:surnameRu is not null and p.surnameRu = :surnameRu)" +
            " or (:surnameEn is not null and p.surnameEn = :surnameEn))" +
            " and ((:nameUk is not null and p.nameUk = :nameUk)" +
            " or (:nameRu is not null and p.nameRu = :nameRu)" +
            " or (:nameEn is not null and p.nameEn = :nameEn))" +
            " and ((:patronymicUk is not null and p.patronymicUk = :patronymicUk)" +
            " or (:patronymicRu is not null and p.patronymicRu = :patronymicRu)" +
            " or (:patronymicEn is not null and p.patronymicEn = :patronymicEn))")
    List<PhysicalPerson> findBySurnameUkOrNameUkOrPatronymicUkOrSurnameRuOrNameRuOrPatronymicRuOrSurnameEnOrNameEnOrPatronymicEn(
            @Param("surnameUk") String surnameUk,
            @Param("nameUk") String nameUk,
            @Param("patronymicUk") String patronymicUk,
            @Param("surnameRu") String surnameRu,
            @Param("nameRu") String nameRu,
            @Param("patronymicRu") String patronymicRu,
            @Param("surnameEn") String surnameEn,
            @Param("nameEn") String nameEn,
            @Param("patronymicEn") String patronymicEn);

//    @Query("SELECT p FROM PhysicalPerson p WHERE (:day is not null and p.day = :day)" +
//            " and (:month is not null and p.month = :month)")
//    List<PhysicalPerson> findByFields(@Param("day") String day,
//                                      @Param("month") String month,
//                                      @Param("year") String year,
//                                      @Param("age") String age,
//                                      @Param("phone") String phone,
//                                      @Param("address") String address,
//                                      @Param("passportNumber") String passportNumber,
//                                      @Param("passportSeria") String passportSeria,
//                                      @Param("id_documentNumber") String id_documentNumber,
//                                      @Param("id_registryNumber") String id_registryNumber,
//                                      @Param("foreignP_documentNumber") String foreignP_documentNumber,
//                                      @Param("foreignP_registryNumber") String foreignP_registryNumber,
//                                      @Param("inn") String inn
//                                      );

}
