/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapping.def;
import java.util.List;

/**
 * 表的 Index 信息，用于补充生成 DDL 语句
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-12-06
 */
public interface IndexDescription {
    /** 索引名 */
    String getName();

    /** 是否是唯一索引 */
    boolean isUnique();

    /** 索引包含的列 */
    List<String> getColumns();

    /** 创建索引语句生成后在整个语句的自后添加的自定义代码 */
    String getOther();

    /** 索引备注 */
    String getComment();
}
