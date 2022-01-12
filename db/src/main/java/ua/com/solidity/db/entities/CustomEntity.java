package ua.com.solidity.db.entities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.solidity.common.Utils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CustomEntity {

    protected CustomEntity() {
        // nothing
    }

    private static final Map<Class<?>, Object> beans = new HashMap<>();

    protected static <T> T lookupBean(Class<T> beanType) {
        Object rep = beans.getOrDefault(beanType, null);
        if (rep == null) {
            if (Utils.checkApplicationContext()) {
                T r = Utils.getApplicationContext().getBean(beanType);
                beans.put(beanType, r);
                return r;
            }
        } else {
            return beanType.cast(rep);
        }
        return null;
    }

    protected static <T> T doSave(T entity, Class<? extends JpaRepository<T, ?>> repositoryType) {
        JpaRepository<T, ?> repository = lookupBean(repositoryType);
        if (repository != null) {
            try {
                return repository.save(entity);
            } catch (Exception e) {
                log.error("Error on save data.", e);
            }
        }
        return null;
    }
}
