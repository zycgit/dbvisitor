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
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.faker.generator.action.DeleteAction;
import net.hasor.dbvisitor.faker.generator.action.InsertAction;
import net.hasor.dbvisitor.faker.generator.action.UpdateAction;
import net.hasor.dbvisitor.faker.generator.loader.DataLoader;
import net.hasor.dbvisitor.faker.generator.loader.DataLoaderFactory;
import net.hasor.dbvisitor.faker.generator.loader.DefaultDataLoaderFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final FakerFactory             fakerFactory;
    //
    private       SqlPolitic               insertPolitic;
    private       SqlPolitic               updateSetPolitic;
    private       SqlPolitic               wherePolitic;
    private       Action                   insertGenerator;
    private       Action                   updateGenerator;
    private       Action                   deleteGenerator;
    private       boolean                  useQualifier;

    FakerTable(String catalog, String schema, String table, FakerFactory fakerFactory) {
        this.catalog = catalog;
        this.schema = schema;
        this.table = table;
        this.columnMap = new LinkedHashMap<>();
        this.columnList = new ArrayList<>();
        this.fakerFactory = fakerFactory;
        this.insertPolitic = SqlPolitic.RandomCol;
        this.updateSetPolitic = SqlPolitic.RandomCol;
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

    /** 添加一个列 */
    public void addColumn(FakerColumn fakerColumn) {
        this.columnMap.put(fakerColumn.getColumn(), fakerColumn);
        this.columnList.add(fakerColumn);
    }

    /** 获取所有列 */
    public List<String> getColumns() {
        return this.columnList.stream().map(FakerColumn::getColumn).collect(Collectors.toList());
    }

    /** 查找某个列 */
    public FakerColumn findColumn(String columnName) {
        return this.columnMap.get(columnName);
    }

    /** 应用最新配置，并且创建 IUD 生成器 */
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
            fakerColumn.applyConfig();
        }

        DataLoaderFactory dataLoaderFactory = this.fakerFactory.getFakerConfig().getDataLoaderFactory();
        dataLoaderFactory = dataLoaderFactory == null ? new DefaultDataLoaderFactory() : dataLoaderFactory;
        SqlDialect dialect = this.fakerFactory.getSqlDialect();

        DataLoader dataLoader = dataLoaderFactory.createDataLoader(this.fakerFactory.getFakerConfig(), this.fakerFactory.getJdbcTemplate(), dialect);
        this.insertGenerator = new InsertAction(this, dialect, insertColumns);
        this.updateGenerator = new UpdateAction(this, dialect, updateSetColumns, updateWhereColumns, dataLoader);
        this.deleteGenerator = new DeleteAction(this, dialect, deleteWhereColumns, dataLoader);
    }

    /** 生成一批 insert，每批语句都是相同的语句模版 */
    protected List<BoundQuery> buildInsert(int batchSize) throws SQLException {
        return this.insertGenerator.generatorAction(batchSize);
    }

    /** 生成一批 update，每批语句都是相同的语句模版 */
    protected List<BoundQuery> buildUpdate(int batchSize) throws SQLException {
        return this.updateGenerator.generatorAction(batchSize);
    }

    /** 生成一批 delete，每批语句都是相同的语句模版 */
    protected List<BoundQuery> buildDelete(int batchSize) throws SQLException {
        return this.deleteGenerator.generatorAction(batchSize);
    }
}