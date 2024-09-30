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
import net.hasor.dbvisitor.dynamic.DynamicParsed;
import net.hasor.dbvisitor.dynamic.segment.DefaultSqlSegment;
import net.hasor.dbvisitor.jdbc.PreparedStatementCallback;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@link PreparedStatementCallback} 接口实现类用于处理存储过程的参数传递和调用。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2024-09-29
 */
public class PreparedMultipleResultSetExtractor extends AbstractMultipleResultSetExtractor implements PreparedStatementCallback<Map<String, Object>> {

    public PreparedMultipleResultSetExtractor() {
        super(null);
    }

    public PreparedMultipleResultSetExtractor(String originalSql) {
        super(DynamicParsed.getParsedSql(originalSql));
    }

    public PreparedMultipleResultSetExtractor(DefaultSqlSegment parsedSql) {
        super(parsedSql);
    }

    @Override
    public Map<String, Object> doInPreparedStatement(PreparedStatement ps) throws SQLException {
        return super.doInStatement(ps);
    }

    public List<Object> doInPreparedStatementAsList(PreparedStatement ps) throws SQLException {
        return new ArrayList<>(super.doInStatement(ps).values());
    }

    @Override
    protected void beforeStatement(Statement s) {

    }

    @Override
    protected void beforeExecute(Statement s) {

    }

    @Override
    protected boolean doExecute(Statement s) throws SQLException {
        return ((PreparedStatement) s).execute();
    }

    @Override
    protected void afterExecute(Statement s) {

    }

    @Override
    protected void beforeFetchResult(Statement s, Map<String, Object> resultMap) {

    }

    @Override
    protected void afterFetchResult(Statement s, Map<String, Object> resultMap) {

    }

    @Override
    protected void afterStatement(Statement s) {

    }
}
