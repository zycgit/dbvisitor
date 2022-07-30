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

import net.hasor.cobble.RandomUtils;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.faker.generator.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * INSERT 生成器
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class InsertAction implements Action {
    private final FakerTable        tableInfo;
    private final boolean           useQualifier;
    private final SqlDialect        dialect;
    private final List<FakerColumn> insertColumns;
    private final List<FakerColumn> canCutColumns;

    public InsertAction(FakerTable tableInfo, SqlDialect dialect, List<FakerColumn> insertColumns) {
        this.tableInfo = tableInfo;
        this.useQualifier = tableInfo.isUseQualifier();
        this.dialect = dialect;
        this.insertColumns = insertColumns;
        this.canCutColumns = insertColumns.stream().filter(FakerColumn::isCanBeCut).collect(Collectors.toList());
    }

    @Override
    public List<BoundQuery> generatorAction(int batchSize) {
        switch (this.tableInfo.getInsertPolitic()) {
            case KeyCol:
            case RandomKeyCol:
            case RandomCol:
                return generatorByRandom(batchSize);
            case FullCol:
                return generatorByFull(batchSize);
            default:
                throw new UnsupportedOperationException("insertPolitic '" + this.tableInfo.getInsertPolitic() + "' Unsupported.");
        }
    }

    private List<BoundQuery> generatorByRandom(int batchSize) {
        // try use cut
        List<FakerColumn> useColumns = new ArrayList<>(this.insertColumns);
        List<FakerColumn> cutColumns = new ArrayList<>();

        int maxCut = RandomUtils.nextInt(0, this.canCutColumns.size());
        while (cutColumns.size() < maxCut) {
            FakerColumn cutColumn = this.canCutColumns.get(RandomUtils.nextInt(0, maxCut));
            if (!cutColumns.contains(cutColumn)) {
                cutColumns.add(cutColumn);
            }
        }
        useColumns.removeAll(cutColumns);

        // maker sure is not empty insert.
        if (useColumns.isEmpty()) {
            useColumns.add(this.canCutColumns.get(RandomUtils.nextInt(0, this.canCutColumns.size())));
        }

        return buildAction(batchSize, useColumns);
    }

    private List<BoundQuery> generatorByFull(int batchSize) {
        return buildAction(batchSize, this.insertColumns);
    }

    private List<BoundQuery> buildAction(int batchSize, List<FakerColumn> useColumns) {
        String catalog = this.tableInfo.getCatalog();
        String schema = this.tableInfo.getSchema();
        String table = this.tableInfo.getTable();
        String tableName = this.dialect.tableName(this.useQualifier, catalog, schema, table);

        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        for (FakerColumn colInfo : useColumns) {
            if (columns.length() > 0) {
                columns.append(", ");
                values.append(", ");
            }
            String colName = colInfo.getColumn();
            columns.append(this.dialect.columnName(this.useQualifier, catalog, schema, table, colName));
            values.append("?");
        }

        StringBuilder builder = new StringBuilder();
        builder.append("insert into " + tableName);
        builder.append("(" + columns + ")");
        builder.append(" values ");
        builder.append("(" + values + ")");

        List<BoundQuery> boundQueries = new ArrayList<>();
        for (int i = 0; i < batchSize; i++) {
            SqlArg[] args = new SqlArg[useColumns.size()];
            for (int argIdx = 0; argIdx < useColumns.size(); argIdx++) {
                FakerColumn colInfo = useColumns.get(argIdx);
                args[argIdx] = colInfo.generatorData();
            }

            boundQueries.add(new BoundQuery(builder, args));
        }
        return boundQueries;
    }
}