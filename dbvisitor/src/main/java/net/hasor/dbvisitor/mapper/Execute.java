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
 * 用于执行任意SQL语句的注解
 * 可以标注在Mapper接口的方法上，指定要执行的SQL语句
 * @author 赵永春 (zyc@hasor.net)
 * @version 2024-09-19
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Execute {
    /**
     * 要执行的SQL语句
     */
    String[] value();

    /**
     * 指定SQL语句的执行类型
     * @return 语句类型枚举，默认为预编译语句(Prepared)
     */
    StatementType statementType() default StatementType.Prepared;

    /**
     * 设置SQL执行超时时间(秒)
     * @return 超时秒数，-1表示不设置超时
     */
    int timeout() default -1;

    /**
     * 指定需要绑定的输出参数名称
     * @return 输出参数名称数组
     */
    String[] bindOut() default {};
}
