/*
 * Copyright 2008-2009 the original author or authors.
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
package net.hasor.db.dal.dynamic;
import net.hasor.db.dialect.SqlBuilder;

import java.sql.SQLException;
import java.util.Map;

/**
 * 本处理器，兼容 @{...}、#{...}、${...} 三种写法。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-03-28
 */
public interface DynamicSql {
    /** 是否包含替换占位符，如果包含替换占位符那么不能使用批量模式 */
    public boolean isHavePlaceholder();

    public void buildQuery(Map<String, Object> data, DynamicContext context, SqlBuilder sqlBuilder) throws SQLException;

    public default SqlBuilder buildQuery(Map<String, Object> data, DynamicContext context) throws SQLException {
        SqlBuilder fxBuilder = new SqlBuilder();
        this.buildQuery(data, context, fxBuilder);
        return fxBuilder;
    }
}