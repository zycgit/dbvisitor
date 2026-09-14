/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import io.milvus.grpc.FieldSchema;
import io.milvus.param.ParamUtils;
import io.milvus.v2.service.collection.request.DescribeCollectionReq;
import io.milvus.v2.service.collection.request.ListCollectionsReq;
import io.milvus.v2.service.collection.response.DescribeCollectionResp;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandKeys;
import net.hasor.dbvisitor.adapter.milvus.mapping.MilvusSchema;
import net.hasor.dbvisitor.driver.*;

/** Native collection schemas within the connected Milvus database. */
final class MilvusMetadata implements MetadataSupport {
    private final MilvusCmd  command;
    private final Connection owner;

    MilvusMetadata(MilvusCmd command, Connection owner) {
        this.command = command;
        this.owner = owner;
    }

    @Override
    public Set<MetadataType> supportedTypes() {
        return Set.of(MetadataType.CATALOG, MetadataType.TABLE, MetadataType.COLUMN);
    }

    @Override
    public List<MetadataNode> query(MetadataPath path) throws SQLException {
        if (path.type() == MetadataType.CATALOG && path.levels().isEmpty()) {
            return List.of(new MetadataNode(MetadataType.CATALOG, command.getCatalog()));
        }
        if (!Objects.equals(path.name(MetadataType.CATALOG), command.getCatalog())) {
            return List.of();
        }
        if (path.type() == MetadataType.TABLE && path.levels().size() == 1) {
            List<MetadataNode> nodes = new ArrayList<>();
            for (String table : command.listCollectionsV2(ListCollectionsReq.builder().databaseName(command.getCatalog()).build()).getCollectionNames()) {
                Map<String, Object> attributes = new HashMap<>();
                attributes.put(MetadataNode.REMARKS, describe(table).getDescription());
                nodes.add(new MetadataNode(MetadataType.TABLE, table, attributes));
            }
            return nodes;
        }
        String table = path.name(MetadataType.TABLE);
        if (path.type() != MetadataType.COLUMN || table == null || path.levels().size() != 2) {
            return List.of();
        }
        TypeSupport types = owner.unwrap(TypeSupport.class);
        List<MetadataNode> nodes = new ArrayList<>();
        int ordinal = 0;
        for (FieldSchema field : MilvusSchema.collectionFields(describe(table))) {
            ordinal++;
            if (field.getIsDynamic()) {
                continue;
            }
            JdbcColumn column = MilvusSchema.column(field, table, command.getCatalog());
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(MetadataNode.JDBC_TYPE, types.getTypeNumber(column.type));
            attributes.put(MetadataNode.TYPE_NAME, field.getDataType().name());
            attributes.put(MetadataNode.NULLABLE, field.getNullable());
            attributes.put(MetadataNode.AUTO_INCREMENT, field.getAutoID());
            attributes.put(MetadataNode.ORDINAL, ordinal);
            attributes.put(MetadataNode.REMARKS, field.getDescription());
            if (field.hasDefaultValue()) {
                Object value = ParamUtils.valueFieldToObject(field.getDefaultValue(), field.getDataType());
                attributes.put(MetadataNode.DEFAULT_VALUE, value == null ? null : value.toString());
            }
            field.getTypeParamsList().stream().filter(p -> MilvusCommandKeys.MAX_LENGTH.equals(p.getKey())).findFirst().ifPresent(p -> attributes.put(MetadataNode.OCTET_LENGTH, Integer.valueOf(p.getValue())));
            nodes.add(new MetadataNode(MetadataType.COLUMN, field.getName(), attributes));
        }
        return nodes;
    }

    private DescribeCollectionResp describe(String table) throws SQLException {
        return command.describeCollection(DescribeCollectionReq.builder().databaseName(command.getCatalog()).collectionName(table).build());
    }
}
