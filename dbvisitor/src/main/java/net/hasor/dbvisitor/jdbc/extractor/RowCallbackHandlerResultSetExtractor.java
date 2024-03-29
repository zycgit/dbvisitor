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
package net.hasor.dbvisitor.jdbc.extractor;
import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.jdbc.RowCallbackHandler;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 使用 {@link RowCallbackHandler} 类型循环处理每一行记录的适配器。
 * @author 赵永春 (zyc@hasor.net)
 */
public class RowCallbackHandlerResultSetExtractor implements ResultSetExtractor<Void> {
    private final RowCallbackHandler rch;

    public RowCallbackHandlerResultSetExtractor(final RowCallbackHandler rch) {
        this.rch = rch;
    }

    @Override
    public Void extractData(final ResultSet rs) throws SQLException {
        int rowNum = 0;
        while (rs.next()) {
            this.rch.processRow(rs, rowNum++);
        }
        return null;
    }
}
