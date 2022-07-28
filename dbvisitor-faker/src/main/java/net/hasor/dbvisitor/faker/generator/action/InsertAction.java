package net.hasor.dbvisitor.faker.generator.action;

import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.faker.generator.*;
import net.hasor.dbvisitor.faker.seed.RandomUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InsertAction implements Action {
    private final FakerTable            tableInfo;
    private final SqlDialect            dialect;
    private final List<GeneratorColumn> insertColumns;
    private final List<GeneratorColumn> canCutColumns;

    public InsertAction(GeneratorTable tableInfo, SqlDialect dialect, List<GeneratorColumn> insertColumns) {
        this.tableInfo = tableInfo.getTableInfo();
        this.dialect = dialect;
        this.insertColumns = insertColumns;
        this.canCutColumns = insertColumns.stream().filter(c -> c.getColumnInfo().isCanBeCut()).collect(Collectors.toList());
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
        List<GeneratorColumn> useColumns = new ArrayList<>(this.insertColumns);
        List<GeneratorColumn> cutColumns = new ArrayList<>();

        int maxCut = RandomUtils.nextInt(0, this.canCutColumns.size());
        while (cutColumns.size() < maxCut) {
            GeneratorColumn cutColumn = this.canCutColumns.get(RandomUtils.nextInt(0, maxCut));
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

    private List<BoundQuery> buildAction(int batchSize, List<GeneratorColumn> useColumns) {
        String catalog = this.tableInfo.getCatalog();
        String schema = this.tableInfo.getSchema();
        String table = this.tableInfo.getTable();

        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        for (GeneratorColumn colInfo : useColumns) {
            if (columns.length() > 0) {
                columns.append(", ");
                values.append(", ");
            }
            String colName = colInfo.getColumnInfo().getColumn();
            columns.append(this.dialect.columnName(true, catalog, schema, table, colName));
            values.append("?");
        }

        String tableName = this.dialect.tableName(true, catalog, schema, table);
        StringBuilder builder = new StringBuilder();
        builder.append("insert into " + tableName);
        builder.append("(" + columns + ")");
        builder.append(" values ");
        builder.append("(" + values + ")");

        List<BoundQuery> boundQueries = new ArrayList<>();
        for (int i = 0; i < batchSize; i++) {
            SqlArg[] args = new SqlArg[useColumns.size()];
            for (int argIdx = 0; argIdx < useColumns.size(); argIdx++) {
                GeneratorColumn colInfo = useColumns.get(argIdx);
                args[argIdx] = colInfo.generatorData();
            }

            boundQueries.add(new BoundQuery(builder, args));
        }
        return boundQueries;
    }
}