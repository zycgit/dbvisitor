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

public class UpdateAction implements Action {
    private final FakerTable            tableInfo;
    private final SqlDialect            dialect;
    private final List<GeneratorColumn> updateSetColumns;
    private final List<GeneratorColumn> whereFullColumns;
    private final List<GeneratorColumn> whereKeyColumns;
    private final DataLoader            dataLoader;

    public UpdateAction(GeneratorTable tableInfo, SqlDialect dialect, List<GeneratorColumn> updateSetColumns, List<GeneratorColumn> whereColumns, DataLoader dataLoader) {
        this.tableInfo = tableInfo.getTableInfo();
        this.dialect = dialect;
        this.dataLoader = dataLoader;

        this.updateSetColumns = updateSetColumns;
        this.whereFullColumns = whereColumns;
        this.whereKeyColumns = whereColumns.stream().filter(c -> c.getColumnInfo().isKey()).collect(Collectors.toList());
    }

    @Override
    public List<BoundQuery> generatorAction(int batchSize) throws SQLException {
        List<GeneratorColumn> setColumns = null;
        switch (this.tableInfo.getUpdateSetPolitic()) {
            case RandomKeyCol:
            case RandomCol: {
                setColumns = new ArrayList<>(this.updateSetColumns);
                if (!this.updateSetColumns.isEmpty()) {
                    List<GeneratorColumn> cutColumns = randomCol(this.updateSetColumns);
                    setColumns.removeAll(cutColumns);
                }
                // maker sure is not empty insert.
                if (setColumns.isEmpty()) {
                    setColumns.add(this.updateSetColumns.get(RandomUtils.nextInt(0, this.updateSetColumns.size())));
                }
                break;
            }
            case KeyCol:
            case FullCol:
                setColumns = this.updateSetColumns;
                break;
            default:
                throw new UnsupportedOperationException("updateSetPolitic '" + this.tableInfo.getInsertPolitic() + "' Unsupported.");
        }

        List<GeneratorColumn> whereColumns = null;
        switch (this.tableInfo.getWherePolitic()) {
            case RandomKeyCol:
                if (!this.whereKeyColumns.isEmpty()) {
                    whereColumns = randomCol(whereKeyColumns);
                    break;
                }
            case RandomCol:
                whereColumns = randomCol(whereFullColumns);
                break;
            case KeyCol:
                if (!this.whereKeyColumns.isEmpty()) {
                    whereColumns = whereKeyColumns;
                    break;
                }
            case FullCol:
                whereColumns = whereFullColumns;
                break;
            default:
                throw new UnsupportedOperationException("updateWherePolitic '" + this.tableInfo.getWherePolitic() + "' Unsupported.");
        }

        return buildAction(batchSize, setColumns, whereColumns);
    }

    private List<GeneratorColumn> randomCol(List<GeneratorColumn> useCols) {
        List<GeneratorColumn> useColumns = new ArrayList<>(useCols);

        int maxCut = RandomUtils.nextInt(0, useColumns.size());
        for (int i = 0; i < maxCut; i++) {
            useColumns.remove(RandomUtils.nextInt(0, useColumns.size()));
        }

        // maker sure is not empty delete.
        if (useColumns.isEmpty()) {
            useColumns.add(useCols.get(RandomUtils.nextInt(0, useCols.size())));
        }

        return useColumns;
    }

    private List<BoundQuery> buildAction(int batchSize, List<GeneratorColumn> setColumns, List<GeneratorColumn> whereColumns) throws SQLException {
        // fetch some data used for delete
        List<String> fetchCols = whereColumns.stream().map(c -> c.getColumnInfo().getColumn()).collect(Collectors.toList());
        List<Map<String, Object>> fetchDataList = this.dataLoader.loadSomeData(UseFor.UpdateWhere, this.tableInfo, fetchCols, batchSize);
        if (fetchDataList == null || fetchDataList.isEmpty()) {
            return Collections.emptyList();
        }

        // build delete sql
        String catalog = this.tableInfo.getCatalog();
        String schema = this.tableInfo.getSchema();
        String table = this.tableInfo.getTable();

        // build set
        StringBuilder set = new StringBuilder();
        for (GeneratorColumn colInfo : setColumns) {
            if (set.length() > 0) {
                set.append(", ");
            }
            set.append(this.dialect.columnName(true, catalog, schema, table, colInfo.getColumnInfo().getColumn()));
            set.append(" = ?");
        }

        // build where
        StringBuilder where = new StringBuilder();
        for (GeneratorColumn colInfo : whereColumns) {
            if (where.length() > 0) {
                where.append(" and ");
            }
            where.append(this.dialect.columnName(true, catalog, schema, table, colInfo.getColumnInfo().getColumn()));
            where.append(" = ?");
        }
        StringBuilder builder = new StringBuilder();
        builder.append("update " + table);
        builder.append(" set " + set);
        builder.append(" where " + where);

        // build args
        List<BoundQuery> boundQueries = new ArrayList<>();
        for (Map<String, Object> objectMap : fetchDataList) {
            SqlArg[] args = new SqlArg[setColumns.size() + whereColumns.size()];
            int index = 0;
            for (GeneratorColumn colInfo : setColumns) {
                args[index++] = colInfo.generatorData();
            }
            for (GeneratorColumn colInfo : whereColumns) {
                Object value = objectMap.get(colInfo.getColumnInfo().getColumn());
                args[index++] = colInfo.buildData(value);
            }

            boundQueries.add(new BoundQuery(builder, args));
        }
        return boundQueries;
    }

}