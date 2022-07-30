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
package net.hasor.dbvisitor.faker.generator.action;

import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.RandomUtils;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.faker.generator.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DELETE 生成器
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class DeleteAction implements Action {
    private final FakerTable        tableInfo;
    private final boolean           useQualifier;
    private final SqlDialect        dialect;
    private final List<FakerColumn> whereFullCols;
    private final List<FakerColumn> whereKeyCols;
    private final DataLoader        dataLoader;

    public DeleteAction(FakerTable tableInfo, SqlDialect dialect, List<FakerColumn> whereColumns, DataLoader dataLoader) {
        this.tableInfo = tableInfo;
        this.useQualifier = tableInfo.isUseQualifier();
        this.dialect = dialect;
        this.dataLoader = dataLoader;
        this.whereFullCols = whereColumns;
        this.whereKeyCols = whereColumns.stream().filter(FakerColumn::isKey).collect(Collectors.toList());
    }

    @Override
    public List<BoundQuery> generatorAction(int batchSize) throws SQLException {
        switch (this.tableInfo.getWherePolitic()) {
            case RandomKeyCol:
                if (!this.whereKeyCols.isEmpty()) {
                    return generatorByRandomCol(batchSize, this.whereKeyCols);
                }
            case RandomCol:
                return generatorByRandomCol(batchSize, this.whereFullCols);
            case KeyCol:
                if (!this.whereKeyCols.isEmpty()) {
                    return buildAction(batchSize, this.whereKeyCols);
                }
            case FullCol:
                return buildAction(batchSize, this.whereFullCols);
            default:
                throw new UnsupportedOperationException("deletePolitic '" + this.tableInfo.getWherePolitic() + "' Unsupported.");
        }
    }

    private List<BoundQuery> generatorByRandomCol(int batchSize, List<FakerColumn> useCols) throws SQLException {
        List<FakerColumn> useColumns = new ArrayList<>(useCols);

        int maxCut = RandomUtils.nextInt(0, useColumns.size());
        for (int i = 0; i < maxCut; i++) {
            useColumns.remove(RandomUtils.nextInt(0, useColumns.size()));
        }

        // maker sure is not empty delete.
        if (useColumns.isEmpty()) {
            useColumns.add(useCols.get(RandomUtils.nextInt(0, useCols.size())));
        }

        return buildAction(batchSize, useColumns);
    }

    private List<BoundQuery> buildAction(int batchSize, List<FakerColumn> useColumns) throws SQLException {
        // fetch some data used for delete
        List<String> fetchCols = useColumns.stream().map(FakerColumn::getColumn).collect(Collectors.toList());
        List<Map<String, Object>> fetchDataList = this.dataLoader.loadSomeData(UseFor.UpdateWhere, this.tableInfo, fetchCols, batchSize);
        if (CollectionUtils.isEmpty(fetchDataList)) {
            return Collections.emptyList();
        }

        // build delete sql
        String catalog = this.tableInfo.getCatalog();
        String schema = this.tableInfo.getSchema();
        String table = this.tableInfo.getTable();
        String tableName = this.dialect.tableName(this.useQualifier, catalog, schema, table);

        StringBuilder where = new StringBuilder();
        for (FakerColumn colInfo : useColumns) {
            if (where.length() > 0) {
                where.append(" and ");
            }
            where.append(this.dialect.columnName(this.useQualifier, catalog, schema, table, colInfo.getColumn()));
            where.append(" = ?");
        }
        StringBuilder builder = new StringBuilder();
        builder.append("delete from ");
        builder.append(tableName);
        builder.append(" where " + where);

        // build args
        List<BoundQuery> boundQueries = new ArrayList<>();
        for (Map<String, Object> objectMap : fetchDataList) {
            SqlArg[] args = new SqlArg[useColumns.size()];
            for (int i = 0; i < useColumns.size(); i++) {
                FakerColumn colInfo = useColumns.get(i);
                Object value = objectMap.get(colInfo.getColumn());
                args[i] = colInfo.buildData(value);
            }

            boundQueries.add(new BoundQuery(builder, args));
        }
        return boundQueries;
    }
}