/*
 * Copyright 2002-2010 the original author or authors.
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
 * 标记在类型上表示映射到的表
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {

    /** Schema，对于 mysql 来说 schema 相当于 db */
    String schema() default "";

    /** 表名，为空的话表示采用类名为表名 see: {@link #name()} */
    String value() default "";

    /** 表名，为空的话表示采用类名为表名 see: {@link #value()} */
    String name() default "";

    /** 是否将类型下的所有字段都自动和数据库中的列进行映射匹配，true 表示自动。false 表示必须通过 @Column 注解声明 */
    boolean autoMapping() default true;

    /** 表名和属性名，根据驼峰规则转换为带有下划线的表名和列名 */
    boolean mapUnderscoreToCamelCase() default false;

    /** 当表/列名中 存在关键字时候需要设置为 true */
    boolean useDelimited() default false;

    /** 是否对表名列名敏感，默认 true 不敏感 */
    boolean caseInsensitive() default true;

}
