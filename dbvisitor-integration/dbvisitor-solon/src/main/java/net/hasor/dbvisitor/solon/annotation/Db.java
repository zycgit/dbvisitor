package net.hasor.dbvisitor.solon.annotation;

import java.lang.annotation.*;

/**
 * 数据工厂注解
 * 例：
 * @author noear
 * @Db("db1")
 * @since 1.6
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Db {
    /**
     * datsSource bean name
     */
    String value() default "";
}
