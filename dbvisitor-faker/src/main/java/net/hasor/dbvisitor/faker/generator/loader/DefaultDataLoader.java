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
package net.hasor.dbvisitor.faker.generator.loader;

import net.hasor.cobble.RandomUtils;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.ConditionSqlDialect;
import net.hasor.dbvisitor.dialect.PageSqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.faker.generator.DataLoader;
import net.hasor.dbvisitor.faker.generator.FakerColumn;
import net.hasor.dbvisitor.faker.generator.FakerTable;
import net.hasor.dbvisitor.faker.generator.UseFor;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.page.PageObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 反查数据加载器
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class DefaultDataLoader implements DataLoader {
    private final JdbcTemplate jdbcTemplate;
    private final SqlDialect   dialect;

    public DefaultDataLoader(JdbcTemplate jdbcTemplate, SqlDialect dialect) {
        this.jdbcTemplate = jdbcTemplate;
        this.dialect = dialect;
    }

    @Override
    public List<Map<String, Object>> loadSomeData(UseFor useFor, FakerTable fakerTable, List<String> includeColumns, int batchSize) throws SQLException {
        // type1 use randomQuery.
        if (dialect instanceof ConditionSqlDialect) {
            try {
                return loadForRandomQuery(dialect, jdbcTemplate, fakerTable, includeColumns, batchSize);
            } catch (UnsupportedOperationException ignored) {
            }
        }

        // type2 use random page
        if (dialect instanceof PageSqlDialect) {
            try {
                return loadForPageQuery(dialect, jdbcTemplate, fakerTable, includeColumns, batchSize);
            } catch (UnsupportedOperationException ignored) {
            }
        }

        // type3 use random data (there is a hit rate problem)
        return loadForRandomData(dialect, jdbcTemplate, fakerTable, includeColumns, batchSize);
    }

    protected List<Map<String, Object>> loadForRandomQuery(SqlDialect dialect, JdbcTemplate jdbcTemplate,//
            FakerTable fakerTable, List<String> includeColumns, int batchSize) throws SQLException {
        String catalog = fakerTable.getCatalog();
        String schema = fakerTable.getSchema();
        String table = fakerTable.getTable();
        String queryString = ((ConditionSqlDialect) dialect).randomQuery(true, catalog, schema, table, includeColumns, batchSize);
        return jdbcTemplate.queryForList(queryString);
    }

    protected List<Map<String, Object>> loadForPageQuery(SqlDialect dialect, JdbcTemplate jdbcTemplate,//
            FakerTable fakerTable, List<String> includeColumns, int batchSize) throws SQLException {
        String catalog = fakerTable.getCatalog();
        String schema = fakerTable.getSchema();
        String table = fakerTable.getTable();
        BoundSql boundSql = new LambdaTemplate(jdbcTemplate).lambdaQuery(catalog, schema, table).select(includeColumns.toArray(new String[0])).getBoundSql(dialect);

        BoundSql countSql = ((PageSqlDialect) dialect).countSql(boundSql);
        long count = jdbcTemplate.queryForLong(countSql.getSqlString(), countSql.getArgs());
        PageObject pageInfo = new PageObject(batchSize, count);
        pageInfo.setPageSize(batchSize);
        long totalPage = pageInfo.getTotalPage();
        pageInfo.setCurrentPage(RandomUtils.nextLong(0, totalPage));

        BoundSql pageSql = ((PageSqlDialect) dialect).pageSql(boundSql, pageInfo.getFirstRecordPosition(), batchSize);
        return jdbcTemplate.queryForList(pageSql.getSqlString(), pageSql.getArgs());
    }

    protected List<Map<String, Object>> loadForRandomData(SqlDialect dialect, JdbcTemplate jdbcTemplate,//
            FakerTable fakerTable, List<String> includeColumns, int batchSize) throws SQLException {
        List<Map<String, Object>> resultData = new ArrayList<>();
        for (int i = 0; i < batchSize; i++) {
            Map<String, Object> record = new LinkedHashMap<>();
            for (String colName : includeColumns) {
                FakerColumn col = fakerTable.findColumns(colName);
                record.put(colName, col.generatorData());
            }
            resultData.add(record);
        }
        return resultData;
    }

}
