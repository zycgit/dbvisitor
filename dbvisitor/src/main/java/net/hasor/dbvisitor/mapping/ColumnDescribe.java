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
 * （可选）标记在字段或者 get/set 方法上表示列信息，用于生成 DDL 语句
 * @author 赵永春 (zyc@hasor.net)
 * @version 2023-01-07
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ColumnDescribe {
    /**
     * 列数据类型（优先级最高，设置后length/precision/scale将失效）
     * 示例：VARCHAR(255)/NUMBER(10,2)
     */
    String sqlType();

    /** 字段长度（适用于字符串类型） */
    String length() default "";

    /** 数字类型总精度 */
    String precision() default "";

    /** 数字类型小数位数 */
    String scale() default "";

    /** 字符集（如utf8mb4） */
    String characterSet() default "";

    /** 排序规则（如utf8mb4_general_ci） */
    String collation() default "";

    /** 是否允许为空（默认true） */
    boolean nullable() default true;

    /** 默认值（需符合数据库语法） */
    String defaultValue() default "";

    /** 列注释/说明 */
    String comment() default "";

    /** 在生成建表语句的时候用于拼接的其它信息，开发者可以随意指定。会在 'create table' / 'alter table' 语句生成时自动追加 */
    String other() default "";
}