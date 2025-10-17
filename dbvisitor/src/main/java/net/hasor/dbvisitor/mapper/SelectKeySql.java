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
import java.lang.annotation.*;

/**
 * 该注解用于标记在 insert 操作中使用的 SelectKey SQL 语句。
 * SelectKey 通常用于在插入数据前后执行 SQL 语句，以获取自增主键等信息。
 * （该注解的使用方式与 MyBatis 中的 SelectKey 注解类似）
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-05-19
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SelectKeySql {
    /**
     * 获取 SelectKey 要执行的 SQL 语句数组。
     * 可以包含一条或多条 SQL 语句，具体执行取决于实现。
     * @return SelectKey 要执行的 SQL 语句数组
     */
    String[] value();

    /**
     * 获取 SQL 语句的执行类型，默认为预编译语句。
     * @return SQL 语句的执行类型
     */
    StatementType statementType() default StatementType.Prepared;

    /**
     * 获取 SQL 语句执行的超时时间，单位为秒。
     * -1 表示不设置超时时间。
     * @return SQL 语句执行的超时时间
     */
    int timeout() default -1;

    /**
     * 获取每次从数据库获取的记录数，默认为 256 条。
     * 该值可用于优化大数据量查询时的性能。
     * @return 每次从数据库获取的记录数
     */
    int fetchSize() default 256;

    /**
     * 获取结果集的类型，默认为默认类型。
     * 结果集类型决定了结果集的游标移动方式和对数据变化的敏感度。
     * @return 结果集的类型
     */
    ResultSetType resultSetType() default ResultSetType.DEFAULT;

    /**
     * 获取用于存储生成键的属性名。
     * 执行 SelectKey 语句后，生成的键会被设置到该属性中。
     * @return 用于存储生成键的属性名
     */
    String keyProperty();

    /**
     * 获取用于存储生成键的列名，默认为空字符串。
     * 如果设置了该值，生成的键会从指定列中获取。
     * @return 用于存储生成键的列名
     */
    String keyColumn() default "";

    /**
     * 获取 SelectKey 语句的执行顺序，是在 insert 之前还是之后执行。
     * @return SelectKey 语句的执行顺序
     */
    Order order();
}
