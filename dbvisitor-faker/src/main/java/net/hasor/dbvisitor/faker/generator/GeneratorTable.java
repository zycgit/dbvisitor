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
package net.hasor.dbvisitor.faker.generator;

import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.ConditionSqlDialect;
import net.hasor.dbvisitor.dialect.PageSqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.faker.generator.action.DeleteAction;
import net.hasor.dbvisitor.faker.generator.action.InsertAction;
import net.hasor.dbvisitor.faker.generator.action.UpdateAction;
import net.hasor.dbvisitor.faker.seed.RandomUtils;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.page.PageObject;

import java.sql.SQLException;
import java.util.*;

/**
 * 列数据生成器
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class GeneratorTable {
    private final FakerTable                   tableInfo;
    private final Map<String, GeneratorColumn> allColumns;
    private final Action                       insertGenerator;
    private final Action                       updateGenerator;
    private final Action                       deleteGenerator;

    public GeneratorTable(FakerTable tableInfo, SqlDialect dialect, JdbcTemplate jdbcTemplate) {
        Objects.requireNonNull(tableInfo, "tableInfo is null.");
        Objects.requireNonNull(dialect, "dialect is null.");
        Objects.requireNonNull(jdbcTemplate, "jdbcTemplate is null.");

        this.tableInfo = tableInfo;
        this.allColumns = new LinkedHashMap<>();

        List<GeneratorColumn> insertColumns = new ArrayList<>();
        List<GeneratorColumn> updateSetColumns = new ArrayList<>();
        List<GeneratorColumn> updateWhereColumns = new ArrayList<>();
        List<GeneratorColumn> deleteWhereColumns = new ArrayList<>();

        for (FakerColumn fakerColumn : tableInfo.getColumns()) {
            GeneratorColumn genColumn = new GeneratorColumn(fakerColumn, tableInfo.getFakerConfig().getTypeRegistry());

            this.allColumns.put(fakerColumn.getColumn(), genColumn);

            if (genColumn.isGenerator(UseFor.Insert)) {
                insertColumns.add(genColumn);
            }
            if (genColumn.isGenerator(UseFor.UpdateSet)) {
                updateSetColumns.add(genColumn);
            }
            if (genColumn.isGenerator(UseFor.UpdateWhere)) {
                updateWhereColumns.add(genColumn);
            }
            if (genColumn.isGenerator(UseFor.DeleteWhere)) {
                deleteWhereColumns.add(genColumn);
            }
        }

        DataLoader dataLoader = tableInfo.getDataLoader() != null ? tableInfo.getDataLoader() : defaultDataLoader(jdbcTemplate, dialect);
        this.insertGenerator = new InsertAction(this, dialect, insertColumns);
        this.updateGenerator = new UpdateAction(this, dialect, updateSetColumns, updateWhereColumns, dataLoader);
        this.deleteGenerator = new DeleteAction(this, dialect, deleteWhereColumns, dataLoader);
    }

    private DataLoader defaultDataLoader(final JdbcTemplate jdbcTemplate, final SqlDialect dialect) {
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
                GeneratorColumn col = this.allColumns.get(colName);
                record.put(colName, col.generatorData());
            }
            resultData.add(record);
        }
        return resultData;
    }

    public FakerTable getTableInfo() {
        return this.tableInfo;
    }

    public List<BoundQuery> buildInsert(int batchSize) throws SQLException {
        return this.insertGenerator.generatorAction(batchSize);
    }

    public List<BoundQuery> buildUpdate(int batchSize) throws SQLException {
        return this.updateGenerator.generatorAction(batchSize);
    }

    public List<BoundQuery> buildDelete(int batchSize) throws SQLException {
        return this.deleteGenerator.generatorAction(batchSize);
    }
}