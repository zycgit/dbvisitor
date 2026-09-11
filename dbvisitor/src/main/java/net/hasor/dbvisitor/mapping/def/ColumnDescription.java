/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapping.def;
/**
 * 列的 DDL 补充信息，用于补充生成 DDL 语句
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-12-06
 */
public interface ColumnDescription {
    /** 列数据类型，如果配置了该值将会忽略其它属性项 */
    String getSqlType();

    /** 长度 */
    String getLength();

    /** 精度 */
    String getPrecision();

    /** 小数位数 */
    String getScale();

    /** 列字符集 */
    String getCharacterSet();

    /** 列排序规则 */
    String getCollation();

    /** 表示列是否允许为空 */
    boolean isNullable();

    /** 列上具有的默认值 */
    String getDefault();

    /** 列备注 */
    String getComment();

    /** 在生成建表语句的时候用于拼接的其它信息，开发者可以随意指定。会在 'create table' / 'alter table' 语句生成时自动追加 */
    String getOther();
}
