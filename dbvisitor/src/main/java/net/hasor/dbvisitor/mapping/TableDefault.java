/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.mapping;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记在包上,用于配置 @Table 注解中的默认值。
 * - @TableDefault 注解可以在父包和子包中同时配置
 * - 属性的查找和生效策略是，子包优先
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2022-12-06
 */
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableDefault {
    /** catalog */
    String catalog() default "";

    /** Schema */
    String schema() default "";

    /** 是否将类型下的所有字段都自动和数据库中的列进行映射匹配，true 表示自动。false 表示必须通过 @Column 注解声明 */
    boolean autoMapping() default true;

    /** 强制在生成 表名/列名/索引名 时候增加标识符限定，例如：通过设置该属性来解决列名为关键字的问题。默认是 false 不设置。 */
    boolean useDelimited() default false;

    /** 是否对表名列名敏感，默认 true 不敏感 */
    boolean caseInsensitive() default true;

    /** 表名和属性名，根据驼峰规则转换为带有下划线的表名和列名 */
    boolean mapUnderscoreToCamelCase() default false;

    /** DDL生成和执行规则，默认关闭 */
    DdlAuto ddlAuto() default DdlAuto.None;
}