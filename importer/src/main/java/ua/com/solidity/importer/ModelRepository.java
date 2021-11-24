package ua.com.solidity.importer;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ua.com.solidity.common.Utils;

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
    public void insertRow(JsonNode node) {
        try {
            entityManager.createNativeQuery("INSERT INTO \"tmpResource\" (name, kod_pdv, dat_reestr, d_reestr_sg, dat_anul, name_anul, name_oper, kved, d_anul_sg, d_pdv_sg) VALUES (?,?,?,?,?,?,?,?,?,?)")
                    .setParameter(1, Utils.getNodeValue(node, "name", String.class))
                    .setParameter(2, Utils.getNodeValue(node, "kod_pdv", String.class))
                    .setParameter(3, Utils.getNodeValue(node, "dat_reestr", String.class))
                    .setParameter(4, Utils.getNodeValue(node, "d_reestr_sg", String.class))
                    .setParameter(5, Utils.getNodeValue(node, "dat_anul", String.class))
                    .setParameter(6, Utils.getNodeValue(node, "name_anul", String.class))
                    .setParameter(7, Utils.getNodeValue(node, "name_oper", String.class))
                    .setParameter(8, Utils.getNodeValue(node, "kved", String.class))
                    .setParameter(9, Utils.getNodeValue(node,"d_anul_sg", String.class))
                    .setParameter(10, Utils.getNodeValue(node, "d_pdv_sg", String.class))
                    .executeUpdate();
        } catch (Exception e) {
            log.warn("DB insert failed {}.", e.getMessage());
        }
    }
}