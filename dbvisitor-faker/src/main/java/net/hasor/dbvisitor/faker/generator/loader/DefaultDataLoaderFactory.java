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
import net.hasor.dbvisitor.faker.generator.SqlPolitic;
import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.page.PageObject;

import java.sql.SQLException;
import java.util.*;

/**
 * 反查数据加载器
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class DefaultDataLoaderFactory implements DataLoaderFactory {
    @Override
    public DataLoader createDataLoader(FakerConfig fakerConfig, JdbcTemplate jdbcTemplate, SqlDialect dialect) {
        return (useFor, fakerTable, batchSize) -> {
            boolean onlyKey = fakerTable.getWherePolitic() == SqlPolitic.KeyCol || fakerTable.getWherePolitic() == SqlPolitic.RandomKeyCol && fakerTable.hasKey();
            List<String> includeColumns = new ArrayList<>();
            Map<String, String> includeColumnTerms = new HashMap<>();

            for (String col : fakerTable.getColumns()) {
                FakerColumn column = fakerTable.findColumn(col);
                if (onlyKey && !column.isKey()) {
                    continue;
                }

                String template = column.getSelectTemplate();
                includeColumns.add(col);
                if (StringUtils.isNotBlank(template) && !StringUtils.equals(template, col)) {
                    includeColumnTerms.put(column.getColumn(), template);
                }
            }

            // type1 use randomQuery.
            if (dialect instanceof ConditionSqlDialect) {
                try {
                    return loadForRandomQuery(dialect, jdbcTemplate, fakerTable, includeColumns, includeColumnTerms, batchSize);
                } catch (UnsupportedOperationException ignored) {
                }
            }

            // type2 use random page
            if (dialect instanceof PageSqlDialect) {
                try {
                    return loadForPageQuery(dialect, jdbcTemplate, fakerTable, includeColumns, includeColumnTerms, batchSize);
                } catch (UnsupportedOperationException ignored) {
                }
            }

            // type3 use random data (there is a hit rate problem)
            return loadForRandomData(dialect, jdbcTemplate, fakerTable, includeColumns, includeColumnTerms, batchSize);
        };
    }

    protected List<Map<String, SqlArg>> loadForRandomQuery(SqlDialect dialect, JdbcTemplate jdbcTemplate, //
            FakerTable fakerTable, List<String> includeColumns, Map<String, String> includeColumnTerms, int batchSize) throws SQLException {
        boolean useQualifier = fakerTable.isUseQualifier();
        String catalog = fakerTable.getCatalog();
        String schema = fakerTable.getSchema();
        String table = fakerTable.getTable();

        String queryString = ((ConditionSqlDialect) dialect).randomQuery(useQualifier, catalog, schema, table, includeColumns, includeColumnTerms, batchSize);
        return jdbcTemplate.queryForList(queryString, convertRow(fakerTable, includeColumns));
    }

    protected List<Map<String, SqlArg>> loadForPageQuery(SqlDialect dialect, JdbcTemplate jdbcTemplate, //
            FakerTable fakerTable, List<String> includeColumns, Map<String, String> includeColumnTerms, int batchSize) throws SQLException {
        boolean useQualifier = fakerTable.isUseQualifier();
        String catalog = fakerTable.getCatalog();
        String schema = fakerTable.getSchema();
        String table = fakerTable.getTable();

        StringBuilder selectApply = new StringBuilder();
        for (int i = 0; i < includeColumns.size(); i++) {
            String colName = includeColumns.get(i);
            if (i > 0) {
                selectApply.append(", ");
                selectApply.append(", ");
            }

            String selectTerm = includeColumnTerms != null ? includeColumnTerms.get(colName) : null;
            if (StringUtils.isNotBlank(selectTerm)) {
                selectApply.append(selectTerm);
            } else {
                selectApply.append(dialect.fmtName(useQualifier, colName));
            }
        }

        BoundSql boundSql = new LambdaTemplate(jdbcTemplate)//
                .lambdaQuery(catalog, schema, table)        //
                .applySelect(selectApply.toString())        //
                .getBoundSql(dialect);

        BoundSql countSql = ((PageSqlDialect) dialect).countSql(boundSql);
        long count = jdbcTemplate.queryForLong(countSql.getSqlString(), countSql.getArgs());
        PageObject pageInfo = new PageObject(batchSize, count);
        pageInfo.setPageSize(batchSize);
        long totalPage = pageInfo.getTotalPage();
        pageInfo.setCurrentPage(RandomUtils.nextLong(0, totalPage));

        BoundSql pageSql = ((PageSqlDialect) dialect).pageSql(boundSql, pageInfo.getFirstRecordPosition(), batchSize);
        return jdbcTemplate.queryForList(pageSql.getSqlString(), pageSql.getArgs(), convertRow(fakerTable, includeColumns));
    }

    protected List<Map<String, SqlArg>> loadForRandomData(SqlDialect dialect, JdbcTemplate jdbcTemplate, //
            FakerTable fakerTable, List<String> includeColumns, Map<String, String> includeColumnTerms, int batchSize) throws SQLException {
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