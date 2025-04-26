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
package net.hasor.dbvisitor.mapper;

import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.jdbc.RowCallbackHandler;
import net.hasor.dbvisitor.jdbc.RowMapper;

import java.lang.annotation.*;

/**
 * select 语句注解，用于标记一个方法对应一条 select 查询语句。
 * 可以通过该注解配置查询语句的各种属性，如 SQL 语句、语句类型、超时时间等。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-05-19
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Query {
    /**
     * 获取 select 查询语句数组，可包含一条或多条 SQL 语句。
     * @return select 查询语句数组
     */
    String[] value();

    /**
     * 获取 SQL 语句的类型，默认为预编译语句。
     * @return SQL 语句的类型
     */
    StatementType statementType() default StatementType.Prepared;

    /**
     * 获取查询执行的超时时间，单位为秒。
     * -1 表示不设置超时时间。
     * @return 查询执行的超时时间
     */
    int timeout() default -1;

    /**
     * 获取每次从数据库获取的记录数，默认为 256 条。
     * @return 每次从数据库获取的记录数
     */
    int fetchSize() default 256;

    /**
     * 获取结果集的类型，默认为 DEFAULT。
     * @return 结果集的类型
     */
    ResultSetType resultSetType() default ResultSetType.DEFAULT;

    /**
     * 可为该方法配置一个 {@link ResultSetExtractor} 对象用于结果集处理。
     * 如果配置了 {@code bindOut} 那么该配置将会失效。
     * @return 用于结果集处理的 {@link ResultSetExtractor} 类，默认值为 {@link Object} 类
     */
    Class<?> resultSetExtractor() default Object.class;

    /**
     * 可为该方法配置一个 {@link RowCallbackHandler} 对象用于结果集处理。
     * 如果配置了 {@code bindOut} 那么该配置将会失效。
     * @return 用于结果集处理的 {@link RowCallbackHandler} 类，默认值为 {@link Object} 类
     */
    Class<?> resultRowCallback() default Object.class;

    /**
     * 可为该方法配置一个 {@link RowMapper} 对象用于结果集处理。
     * 如果配置了 {@code bindOut} 那么该配置将会失效。
     * @return 用于结果集处理的 {@link RowMapper} 类，默认值为 {@link Object} 类
     */
    Class<?> resultRowMapper() default Object.class;

    /**
     * 获取用于绑定输出参数的参数名数组。
     * 如果配置了该属性，那么 {@code resultSetExtractor}、{@code resultRowCallback} 和 {@code resultRowMapper} 配置将会失效。
     * @return 用于绑定输出参数的参数名数组，默认值为空数组
     */
    String[] bindOut() default {};
}
