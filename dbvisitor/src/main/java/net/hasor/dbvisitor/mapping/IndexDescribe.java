/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapping;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 标记在类型上用于配置对应的索引，该注解能力有限。并不能替代纯 SQL 方式。
 * - 如果存在 xml 和注解共用的情况下，注解配置将会失效。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-12-06
 */
@Repeatable(IndexDescribeSet.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface IndexDescribe {
    /** 索引名，如果不指定则会自动生成 */
    String name() default "";

    /** 唯一索引 */
    boolean unique() default false;

    /** 索引包含的列名 */
    String[] columns();

    /** 索引备注信息 */
    String comment() default "";

    /** 在生成所以创建语句的时候用于拼接的其它信息，开发者可以随意指定。会在 'create index' 语句生成的最后时自动追加 */
    String other() default "";
}
