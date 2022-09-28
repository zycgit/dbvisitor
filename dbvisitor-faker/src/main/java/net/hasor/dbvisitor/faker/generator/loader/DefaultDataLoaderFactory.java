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
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.RandomUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.ConditionSqlDialect;
import net.hasor.dbvisitor.dialect.PageSqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.faker.FakerConfig;
import net.hasor.dbvisitor.faker.generator.FakerColumn;
import net.hasor.dbvisitor.faker.generator.FakerTable;
import net.hasor.dbvisitor.faker.generator.SqlArg;
import net.hasor.dbvisitor.jdbc.RowMapper;
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
public class DefaultDataLoaderFactory implements DataLoaderFactory {
    @Override
    public DataLoader createDataLoader(FakerConfig fakerConfig, JdbcTemplate jdbcTemplate, SqlDialect dialect) {
        return (useFor, fakerTable, includeColumns, batchSize) -> {
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
        };
    }

    protected List<Map<String, SqlArg>> loadForRandomQuery(SqlDialect dialect, JdbcTemplate jdbcTemplate, //
            FakerTable fakerTable, List<String> includeColumns, int batchSize) throws SQLException {
        boolean useQualifier = fakerTable.isUseQualifier();
        String catalog = dialect.fmtName(useQualifier, fakerTable.getCatalog());
        String schema = dialect.fmtName(useQualifier, fakerTable.getSchema());
        String table = dialect.fmtName(useQualifier, fakerTable.getTable());

        List<String> afterIncludeColumns = new ArrayList<>();
        for (String col : includeColumns) {
            FakerColumn column = fakerTable.findColumn(col);
            String template = column.getSelectTemplate();
            if (!StringUtils.isBlank(template) && !StringUtils.equals(template, col)) {
                afterIncludeColumns.add(template);
            } else {
                afterIncludeColumns.add(dialect.fmtName(useQualifier, col));
            }
        }

        String queryString = ((ConditionSqlDialect) dialect).randomQuery(false, catalog, schema, table, afterIncludeColumns, batchSize);
        return jdbcTemplate.query(queryString, convertRow(fakerTable, fakerTable.getColumns()));
    }

    protected List<Map<String, SqlArg>> loadForPageQuery(SqlDialect dialect, JdbcTemplate jdbcTemplate, //
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
        return jdbcTemplate.query(pageSql.getSqlString(), pageSql.getArgs(), convertRow(fakerTable, includeColumns));
    }

    protected List<Map<String, SqlArg>> loadForRandomData(SqlDialect dialect, JdbcTemplate jdbcTemplate, //
            FakerTable fakerTable, List<String> includeColumns, int batchSize) throws SQLException {
        List<Map<String, SqlArg>> resultData = new ArrayList<>();
        for (int i = 0; i < batchSize; i++) {
            Map<String, SqlArg> record = new LinkedHashMap<>();
            for (String colName : includeColumns) {
                FakerColumn col = fakerTable.findColumn(colName);
                record.put(colName, col.generatorData());
            }
            resultData.add(record);
        }
        return resultData;
    }

    protected RowMapper<Map<String, SqlArg>> convertRow(FakerTable fakerTable, List<String> includeColumns) {
        List<String> selectColumns;
        if (CollectionUtils.isEmpty(includeColumns)) {
            selectColumns = fakerTable.getColumns();
        } else {
            selectColumns = includeColumns;
        }

        return (rs, rowNum) -> {
            Map<String, SqlArg> row = new LinkedHashMap<>();
            for (String column : selectColumns) {
                FakerColumn tableColumn = fakerTable.findColumn(column);
                if (tableColumn == null) {
                    continue;
                }
                SqlArg result = tableColumn.readData(rs);
                row.put(column, result);
            }
            return row;
        };
    }
}