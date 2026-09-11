/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dialect.features;
import net.hasor.dbvisitor.dialect.SqlDialect;

/**
 * 生成查询序列的 SQL 语句方言接口
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public interface SeqSqlDialect extends SqlDialect {
    /** 生成查询序列的 SQL 语句 */
    String selectSeq(boolean useQualifier, String catalog, String schema, String seqName);
}
