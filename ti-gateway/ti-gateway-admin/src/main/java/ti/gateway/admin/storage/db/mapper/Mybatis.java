package ti.gateway.admin.storage.db.mapper;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Mybatis Component
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Mybatis {
}
