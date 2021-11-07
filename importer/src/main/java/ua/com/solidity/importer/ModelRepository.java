package ua.com.solidity.importer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Slf4j
@Repository
public class ModelRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public boolean truncate() {
        try {
            entityManager.createNativeQuery("truncate table \"tmpResource\" RESTART IDENTITY").executeUpdate();
            return true;
        } catch (Exception e) {
            log.error("DB tmpResource truncate failed", e);
            return false;
        }
    }

    @Transactional
    public void insertRow(CSVParser parser) {
        try {
            entityManager.createNativeQuery("INSERT INTO \"tmpResource\" (name, kod_pdv, dat_reestr, d_reestr_sg, dat_anul, name_anul, name_oper, kved, d_anul_sg, d_pdv_sg) VALUES (?,?,?,?,?,?,?,?,?,?)")
                    .setParameter(1, parser.getFieldByName("name"))
                    .setParameter(2, parser.getFieldByName("kod_pdv"))
                    .setParameter(3, parser.getFieldByName("dat_reestr"))
                    .setParameter(4, parser.getFieldByName("d_reestr_sg"))
                    .setParameter(5, parser.getFieldByName("dat_anul"))
                    .setParameter(6, parser.getFieldByName("name_anul"))
                    .setParameter(7, parser.getFieldByName("name_oper"))
                    .setParameter(8, parser.getFieldByName("kved"))
                    .setParameter(9, parser.getFieldByName("d_anul_sg"))
                    .setParameter(10, parser.getFieldByName("d_pdv_sg"))
                    .executeUpdate();
        } catch (Exception e) {
            log.warn("DB insert failed {}.", e.getMessage());
        }
    }
}