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
 * update 语句注解，用于标记一个方法对应一条或多条 SQL 的 UPDATE 语句。
 * 当在接口方法上使用该注解时，框架会根据注解中定义的 SQL 语句执行数据库更新操作。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-05-19
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Update {
    /**
     * 获取要执行的 UPDATE SQL 语句数组。
     * 可以定义一条或多条 SQL 语句，框架会按照数组顺序依次执行。
     * @return UPDATE SQL 语句数组
     */
    String[] value();

    /**
     * 获取 SQL 语句的执行类型，默认为预编译语句。
     * 不同的执行类型会影响 SQL 语句的执行方式和性能。
     * @return SQL 语句的执行类型
     */
    StatementType statementType() default StatementType.Prepared;

    /**
     * 获取 SQL 语句执行的超时时间，单位为秒。
     * -1 表示不设置超时时间，即一直等待直到 SQL 语句执行完成。
     * @return SQL 语句执行的超时时间
     */
    int timeout() default -1;
}
