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
package net.hasor.dbvisitor.dynamic.segment;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.SqlBuilder;

import java.sql.SQLException;

/**
 * 纯文本 SQL 片段实现类，用于处理静态 SQL 文本
 * 功能特点：
 * 1. 实现 {@link SqlSegment} 接口，提供 SQL 片段构建功能
 * 2. 支持文本内容的动态追加
 * 3. 直接输出文本内容，不做特殊处理
 * @author 赵永春 (zyc@hasor.net)
 * @version 2024-09-25
 */
public class TextSqlSegment implements SqlSegment {
    private final StringBuilder textString;

    /**
     * 构造函数
     * @param exprString 初始SQL文本
     */
    public TextSqlSegment(String exprString) {
        this.textString = new StringBuilder(exprString);
    }

    /**
     * 追加文本内容
     * @param append 要追加的文本
     */
    public void append(String append) {
        this.textString.append(append);
    }

    /** 构建 SQL 查询 */
    @Override
    public void buildQuery(SqlArgSource data, QueryContext context, SqlBuilder sqlBuilder) throws SQLException {
        sqlBuilder.appendSql(this.textString.toString());
    }

    /** 克隆当前对象，返回新的 {@link TextSqlSegment} 实例 */
    @Override
    public TextSqlSegment clone() {
        return new TextSqlSegment(this.textString.toString());
    }

    @Override
    public String toString() {
        return "Text [" + this.textString + "]";
    }
}
