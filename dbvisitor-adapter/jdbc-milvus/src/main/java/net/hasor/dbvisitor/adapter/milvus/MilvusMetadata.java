/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus;

import java.sql.SQLException;
import java.util.*;
import io.milvus.grpc.FieldSchema;
import io.milvus.param.ParamUtils;
import io.milvus.v2.service.collection.request.DescribeCollectionReq;
import io.milvus.v2.service.collection.request.ListCollectionsReq;
import io.milvus.v2.service.collection.response.DescribeCollectionResp;
import net.hasor.dbvisitor.adapter.milvus.mapping.MilvusSchema;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandKeys;
import net.hasor.dbvisitor.driver.*;

/** JDBC discovery of the current Milvus database's stored collection schema. */
final class MilvusMetadata {
    private final MilvusCmd command;

    MilvusMetadata(MilvusCmd command) {
        this.command = command;
    }

    AdapterCursor tables(String catalog, String schema, String pattern, String[] types) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (matchesNamespace(catalog, schema) && (types == null || Arrays.asList(types).contains("TABLE"))) {
            for (String table : tables(pattern)) {
                Map<String, Object> row = identity(table);
                row.put("TABLE_TYPE", "TABLE");
                row.put("REMARKS", describe(table).getDescription());
                rows.add(row);
            }
        }
        return AdapterMetadata.tables(rows);
    }

    AdapterCursor columns(String catalog, String schema, String tablePattern, String columnPattern, TypeSupport types) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (matchesNamespace(catalog, schema)) {
            for (String table : tables(tablePattern)) {
                int ordinal = 0;
                for (FieldSchema field : MilvusSchema.collectionFields(describe(table))) {
                    ordinal++;
                    if (field.getIsDynamic() || !AdapterMetadata.matchesPattern(field.getName(), columnPattern)) {
                        continue;
                    }
                    JdbcColumn column = MilvusSchema.column(field, table, command.getCatalog());
                    Map<String, Object> row = identity(table);
                    row.put("COLUMN_NAME", field.getName());
                    row.put("DATA_TYPE", types.getTypeNumber(column.type));
                    row.put("TYPE_NAME", field.getDataType().name());
                    row.put("NULLABLE", column.nullable);
                    row.put("IS_NULLABLE", field.getNullable() ? "YES" : "NO");
                    row.put("IS_AUTOINCREMENT", field.getAutoID() ? "YES" : "NO");
                    row.put("ORDINAL_POSITION", ordinal);
                    row.put("REMARKS", field.getDescription());
                    if (field.hasDefaultValue()) {
                        Object value = ParamUtils.valueFieldToObject(field.getDefaultValue(), field.getDataType());
                        row.put("COLUMN_DEF", value == null ? null : value.toString());
                    }
                    field.getTypeParamsList().stream().filter(p -> MilvusCommandKeys.MAX_LENGTH.equals(p.getKey())).findFirst()
                            .ifPresent(p -> row.put("CHAR_OCTET_LENGTH", Integer.valueOf(p.getValue())));
                    rows.add(row);
                }
            }
        }
        return AdapterMetadata.columns(rows);
    }

    private boolean matchesNamespace(String catalog, String schema) {
        return (catalog == null || Objects.equals(catalog, command.getCatalog())) && AdapterMetadata.matchesPattern("", schema);
    }

    private List<String> tables(String pattern) throws SQLException {
        List<String> names = new ArrayList<>(command.listCollectionsV2(ListCollectionsReq.builder().databaseName(command.getCatalog()).build()).getCollectionNames());
        names.removeIf(name -> !AdapterMetadata.matchesPattern(name, pattern));
        Collections.sort(names);
        return names;
    }

    private DescribeCollectionResp describe(String table) throws SQLException {
        return command.describeCollection(DescribeCollectionReq.builder().databaseName(command.getCatalog()).collectionName(table).build());
    }

    private Map<String, Object> identity(String table) {
        Map<String, Object> row = new HashMap<>();
        row.put("TABLE_CAT", command.getCatalog());
        row.put("TABLE_NAME", table);
        return row;
    }
}
