package net.hasor.dbvisitor.faker.generator.action;

import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.faker.generator.*;
import net.hasor.dbvisitor.faker.seed.RandomUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DeleteAction implements Action {
    private final FakerTable            tableInfo;
    private final SqlDialect            dialect;
    private final List<GeneratorColumn> whereFullCols;
    private final List<GeneratorColumn> whereKeyCols;
    private final DataLoader            dataLoader;

    public DeleteAction(GeneratorTable tableInfo, SqlDialect dialect, List<GeneratorColumn> whereColumns, DataLoader dataLoader) {
        this.tableInfo = tableInfo.getTableInfo();
        this.dialect = dialect;
        this.dataLoader = dataLoader;
        this.whereFullCols = whereColumns;
        this.whereKeyCols = whereColumns.stream().filter(c -> c.getColumnInfo().isKey()).collect(Collectors.toList());
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

    private List<BoundQuery> generatorByRandomCol(int batchSize, List<GeneratorColumn> useCols) throws SQLException {
        List<GeneratorColumn> useColumns = new ArrayList<>(useCols);

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

    private List<BoundQuery> buildAction(int batchSize, List<GeneratorColumn> useColumns) throws SQLException {
        // fetch some data used for delete
        List<String> fetchCols = useColumns.stream().map(c -> c.getColumnInfo().getColumn()).collect(Collectors.toList());
        List<Map<String, Object>> fetchDataList = this.dataLoader.loadSomeData(UseFor.UpdateWhere, this.tableInfo, fetchCols, batchSize);
        if (fetchDataList == null || fetchDataList.isEmpty()) {
            return Collections.emptyList();
        }

        // build delete sql
        String catalog = this.tableInfo.getCatalog();
        String schema = this.tableInfo.getSchema();
        String table = this.tableInfo.getTable();
        StringBuilder where = new StringBuilder();
        for (GeneratorColumn colInfo : useColumns) {
            if (where.length() > 0) {
                where.append(" and ");
            }
            where.append(this.dialect.columnName(true, catalog, schema, table, colInfo.getColumnInfo().getColumn()));
            where.append(" = ?");
        }
        StringBuilder builder = new StringBuilder();
        builder.append("delete from ");
        builder.append(this.dialect.tableName(true, catalog, schema, table));
        builder.append(" where " + where);

        // build args
        List<BoundQuery> boundQueries = new ArrayList<>();
        for (Map<String, Object> objectMap : fetchDataList) {
            SqlArg[] args = new SqlArg[useColumns.size()];
            for (int i = 0; i < useColumns.size(); i++) {
                GeneratorColumn colInfo = useColumns.get(i);
                Object value = objectMap.get(colInfo.getColumnInfo().getColumn());
                args[i] = colInfo.buildData(value);
            }

            boundQueries.add(new BoundQuery(builder, args));
        }
        return boundQueries;
    }
}