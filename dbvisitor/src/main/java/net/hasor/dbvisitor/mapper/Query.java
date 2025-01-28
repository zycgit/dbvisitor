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

import net.hasor.dbvisitor.template.ResultSetExtractor;
import net.hasor.dbvisitor.template.RowCallbackHandler;
import net.hasor.dbvisitor.template.RowMapper;

import java.lang.annotation.*;

/**
 * select 语句
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-05-19
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Query {
    String[] value();

    StatementType statementType() default StatementType.Prepared;

    int timeout() default -1;

    int fetchSize() default 256;

    ResultSetType resultSetType() default ResultSetType.DEFAULT;

    /** 可为该方法配置一个 {@link ResultSetExtractor} 对象用于结果集处理（如果配置了 bindOut 那么该配置将会失效） */
    Class<?> resultSetExtractor() default Object.class;

    /** 可为该方法配置一个 {@link RowCallbackHandler} 对象用于结果集处理（如果配置了 bindOut 那么该配置将会失效） */
    Class<?> resultRowCallback() default Object.class;

    /** 可为该方法配置一个 {@link RowMapper} 对象用于结果集处理（如果配置了 bindOut 那么该配置将会失效） */
    Class<?> resultRowMapper() default Object.class;

    String[] bindOut() default {};
}
