/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapping;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 可以用在方法和类型上，相当于 XML 中的 resultMap 配置。
 * <li>在类型上：表示该类型作为结果集映射</li>
 * <li>在方法上：当标记在方法上时用来表示引用的 resultMap。</li>
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ResultMap {
    /** space */
    String space() default "";

    /**
     * 映射ID，同一个 space 下 ID 不能重复。配置为空表示采用类名作为 ID see: {@link #id()}
     * - value 和 id 具有同等作用，目的是为了简化配置。
     */
    String value() default "";

    /** 映射ID，同一个 space 下 ID 不能重复。配置为空表示采用类名作为 ID see: {@link #value()} */
    String id() default "";

    /** 是否将类型下的所有字段都自动和数据库中的列进行映射匹配，true 表示自动。false 表示必须通过 @Column 注解声明 */
    boolean autoMapping() default true;

    /** 是否对表名列名敏感，默认 true 不敏感 */
    boolean caseInsensitive() default true;

    /** 表名和属性名，根据驼峰规则转换为带有下划线的表名和列名 */
    boolean mapUnderscoreToCamelCase() default false;
}
