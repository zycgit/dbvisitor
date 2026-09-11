/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapping.def;
import net.hasor.dbvisitor.mapping.DdlAuto;

/**
 * 表的 DDL 补充信息，用于补充生成 DDL 语句
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-12-06
 */
public interface TableDescription {
    /** DDL 同步策略 */
    DdlAuto getDdlAuto();

    /** 字符集 */
    String getCharacterSet();

    /** 排序规则 */
    String getCollation();

    /** 表备注 */
    String getComment();

    /** 在生成建表语句的时候用于拼接的其它信息，开发者可以随意指定。会在 'create table' / 'alter table' 语句生成时自动追加 */
    String getOther();
}
