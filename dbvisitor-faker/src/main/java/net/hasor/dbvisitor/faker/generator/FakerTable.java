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
import net.hasor.cobble.RandomUtils;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.ConditionSqlDialect;
import net.hasor.dbvisitor.dialect.PageSqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.faker.generator.action.DeleteAction;
import net.hasor.dbvisitor.faker.generator.action.InsertAction;
import net.hasor.dbvisitor.faker.generator.action.UpdateAction;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.page.PageObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 要生成数据的表基本信息和配置信息
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class FakerTable {
    private final String                   catalog;
    private final String                   schema;
    private final String                   table;
    private final Map<String, FakerColumn> columnMap;
    private final List<FakerColumn>        columnList;
    private       SqlPolitic               insertPolitic;
    private       SqlPolitic               updateSetPolitic;
    private       SqlPolitic               wherePolitic;
    private       Action                   insertGenerator;
    private       Action                   updateGenerator;
    private       Action                   deleteGenerator;
    private       boolean                  useQualifier;
    //
    private       FakerFactory             fakerFactory;
    private       SqlDialect               dialect;

    FakerTable(String catalog, String schema, String table) {
        this.catalog = catalog;
        this.schema = schema;
        this.table = table;
        this.columnMap = new LinkedHashMap<>();
        this.columnList = new ArrayList<>();
        this.insertPolitic = SqlPolitic.FullCol;
        this.updateSetPolitic = SqlPolitic.FullCol;
        this.wherePolitic = SqlPolitic.KeyCol;
        this.useQualifier = true;
    }

    public String getCatalog() {
        return catalog;
    }

    public String getSchema() {
        return schema;
    }

    public String getTable() {
        return table;
    }

    public SqlPolitic getInsertPolitic() {
        return insertPolitic;
    }

    public void setInsertPolitic(SqlPolitic insertPolitic) {
        this.insertPolitic = insertPolitic;
    }

    public SqlPolitic getUpdateSetPolitic() {
        return updateSetPolitic;
    }

    public void setUpdateSetPolitic(SqlPolitic updateSetPolitic) {
        this.updateSetPolitic = updateSetPolitic;
    }

    public SqlPolitic getWherePolitic() {
        return wherePolitic;
    }

    public void setWherePolitic(SqlPolitic wherePolitic) {
        this.wherePolitic = wherePolitic;
    }

    public boolean isUseQualifier() {
        return useQualifier;
    }

    public void setUseQualifier(boolean useQualifier) {
        this.useQualifier = useQualifier;
    }

    public void addColumn(FakerColumn fakerColumn) {
        this.columnMap.put(fakerColumn.getColumn(), fakerColumn);
        this.columnList.add(fakerColumn);
    }

    public FakerColumn findColumns(String columnName) {
        return this.columnMap.get(columnName);
    }

    public void initTable(FakerFactory fakerFactory, SqlDialect dialect) {
        this.fakerFactory = fakerFactory;
        this.dialect = dialect;
        this.apply();
    }

    public void apply() {
        List<FakerColumn> insertColumns = new ArrayList<>();
        List<FakerColumn> updateSetColumns = new ArrayList<>();
        List<FakerColumn> updateWhereColumns = new ArrayList<>();
        List<FakerColumn> deleteWhereColumns = new ArrayList<>();

        for (FakerColumn fakerColumn : this.columnList) {
            if (fakerColumn.isGenerator(UseFor.Insert)) {
                insertColumns.add(fakerColumn);
            }
            if (fakerColumn.isGenerator(UseFor.UpdateSet)) {
                updateSetColumns.add(fakerColumn);
            }
            if (fakerColumn.isGenerator(UseFor.UpdateWhere)) {
                updateWhereColumns.add(fakerColumn);
            }
            if (fakerColumn.isGenerator(UseFor.DeleteWhere)) {
                deleteWhereColumns.add(fakerColumn);
            }
        }

        DataLoader dataLoader = this.fakerFactory.getFakerConfig().getDataLoader();
        dataLoader = dataLoader != null ? dataLoader : defaultDataLoader(this.fakerFactory.getJdbcTemplate(), this.dialect);
        this.insertGenerator = new InsertAction(this, this.dialect, insertColumns);
        this.updateGenerator = new UpdateAction(this, this.dialect, updateSetColumns, updateWhereColumns, dataLoader);
        this.deleteGenerator = new DeleteAction(this, this.dialect, deleteWhereColumns, dataLoader);
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
        String queryString = ((ConditionSqlDialect) dialect).randomQuery(true, catalog, schema, table, includeColumns, batchSize);
        return jdbcTemplate.queryForList(queryString);
    }

    protected List<Map<String, Object>> loadForPageQuery(SqlDialect dialect, JdbcTemplate jdbcTemplate,//
            FakerTable fakerTable, List<String> includeColumns, int batchSize) throws SQLException {
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
                FakerColumn col = this.columnMap.get(colName);
                record.put(colName, col.generatorData());
            }
            resultData.add(record);
        }
        return resultData;
    }

    protected List<BoundQuery> buildInsert(int batchSize) throws SQLException {
        return this.insertGenerator.generatorAction(batchSize);
    }

    protected List<BoundQuery> buildUpdate(int batchSize) throws SQLException {
        return this.updateGenerator.generatorAction(batchSize);
    }

    protected List<BoundQuery> buildDelete(int batchSize) throws SQLException {
        return this.deleteGenerator.generatorAction(batchSize);
    }

}