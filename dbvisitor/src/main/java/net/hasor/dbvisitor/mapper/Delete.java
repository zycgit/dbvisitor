/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapper;
import java.lang.annotation.*;

/**
 * 用于标注删除操作的注解。
 * 该注解通常用在Mapper接口的方法上，用于执行数据库删除操作。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-05-19
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Delete {
    /**
     * 删除SQL语句，支持多条语句
     * @return SQL语句数组
     */
    String[] value();

    /**
     * 语句执行类型
     * @return 语句类型，默认为预编译语句
     */
    StatementType statementType() default StatementType.Prepared;

    /**
     * 执行超时时间（秒）
     * @return 超时时间，默认-1表示不设置超时
     */
    int timeout() default -1;
}
