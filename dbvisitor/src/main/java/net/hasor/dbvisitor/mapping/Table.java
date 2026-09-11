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
 * 标记在类型上表示映射到的表
 * - 若注解与 xml 同时配置 XML 将会覆盖注解。
 * - 若xml 配置为 resultMap 会把 catalog/schema/table or value 设置为空。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
    /** catalog */
    String catalog() default "";

    /** Schema */
    String schema() default "";

    /** 表名，为空的话表示采用类名为表名 see: {@link #table()} */
    String value() default "";

    /** 表名，为空的话表示采用类名为表名 see: {@link #value()} */
    String table() default "";

    /** 是否将类型下的所有字段都自动和数据库中的列进行映射匹配，true 表示自动。false 表示必须通过 @Column 注解声明 */
    boolean autoMapping() default true;

    /** 当表/列名中 存在关键字时候需要设置为 true */
    boolean useDelimited() default false;

    /** 是否对表名列名敏感，默认 true 不敏感 */
    boolean caseInsensitive() default true;

    /** 表名和属性名，根据驼峰规则转换为带有下划线的表名和列名 */
    boolean mapUnderscoreToCamelCase() default false;

    /** DDL生成和执行规则，默认关闭 */
    DdlAuto ddlAuto() default DdlAuto.None;
}
