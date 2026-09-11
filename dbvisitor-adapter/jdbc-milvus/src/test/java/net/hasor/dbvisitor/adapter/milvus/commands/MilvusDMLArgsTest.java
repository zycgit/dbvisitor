/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import io.milvus.grpc.*;
import io.milvus.orm.iterator.QueryIterator;
import io.milvus.response.QueryResultsWrapper;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.DeleteReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.QueryIteratorReq;
import io.milvus.v2.service.vector.request.UpsertReq;
import io.milvus.v2.service.vector.response.UpsertResp;
import net.hasor.dbvisitor.adapter.milvus.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import static net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.v2Response;

public class MilvusDMLArgsTest extends AbstractJdbcTest {

    private Connection getConnection() throws SQLException {
        Properties prop = new Properties();
        prop.setProperty(MilvusKeys.CUSTOM_MILVUS, MilvusCustomClient.class.getName());
        prop.setProperty(MilvusKeys.INTERCEPTOR, MilvusCommandInterceptor.class.getName());
        return new JdbcDriver().connect("jdbc:dbvisitor:milvus://xxxxxx:19530", prop);
    }

    @Test
    public void testInsertWithArgs() {
        List<Object> argList = new ArrayList<>();
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if ("insert".equals(method.getName())) {
                argList.addAll(Arrays.asList(args));
                MutationResult.Builder builder = MutationResult.newBuilder();
                builder.setInsertCnt(1);
                builder.addSuccIndex(0);
                return v2Response(method.getName(), builder.build());
            } else if ("describeCollection".equals(method.getName())) {
                CollectionSchema.Builder schemaBuilder = CollectionSchema.newBuilder();
                schemaBuilder.addFields(FieldSchema.newBuilder().setName("book_id").setDataType(DataType.Int64).setIsPrimaryKey(true).build());
                schemaBuilder.addFields(FieldSchema.newBuilder().setName("word_count").setDataType(DataType.Int32).build());
                schemaBuilder.addFields(FieldSchema.newBuilder().setName("book_intro").setDataType(DataType.FloatVector).addTypeParams(KeyValuePair.newBuilder().setKey(MilvusCommandKeys.DIMENSION).setValue("2").build()).build());

                DescribeCollectionResponse.Builder descBuilder = DescribeCollectionResponse.newBuilder();
                descBuilder.setSchema(schemaBuilder.build());
                descBuilder.setStatus(io.milvus.grpc.Status.newBuilder().setErrorCode(ErrorCode.Success).build());
                return v2Response(method.getName(), descBuilder.build());
            } else if ("close".equals(method.getName())) {
                return null;
            }
            return null;
        });

        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO book_vectors (book_id, word_count, book_intro) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, 2001L);
                ps.setInt(2, 600);
                ps.setObject(3, Arrays.asList(0.8f, 0.9f));
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        assert argList.size() == 1;
        InsertReq param = (InsertReq) argList.get(0);
        assert param.getCollectionName().equals("book_vectors");

        assert param.getData().get(0).get("book_id").getAsLong() == 2001L;
    }

    @Test
    public void testDeleteWithArgs() {
        List<Object> argList = new ArrayList<>();
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if ("delete".equals(method.getName())) {
                argList.addAll(Arrays.asList(args));
                MutationResult.Builder builder = MutationResult.newBuilder();
                builder.setDeleteCnt(1);
                return v2Response(method.getName(), builder.build());
            } else if ("close".equals(method.getName())) {
                return null;
            }
            return null;
        });

        try (Connection conn = getConnection()) {
            // SQL: DELETE FROM table WHERE id = ?
            String sql = "DELETE FROM book_vectors WHERE book_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, 3001L);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        assert argList.size() == 1;
        DeleteReq param = (DeleteReq) argList.get(0);
        assert param.getCollectionName().equals("book_vectors");
        // Check expr
        assert param.getFilter().contains("book_id");
        assert param.getFilter().equals("book_id == {arg1}");
        assert param.getFilterTemplateValues().get("arg1").equals(3001L);
    }

    @Test
    public void testDeleteWithInArgs() {
        List<Object> argList = new ArrayList<>();
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if ("delete".equals(method.getName())) {
                argList.addAll(Arrays.asList(args));
                MutationResult.Builder builder = MutationResult.newBuilder();
                builder.setDeleteCnt(3);
                return v2Response(method.getName(), builder.build());
            } else if ("close".equals(method.getName())) {
                return null;
            }
            return null;
        });

        try (Connection conn = getConnection()) {
            // SQL: DELETE FROM table WHERE id IN ?
            String sql = "DELETE FROM book_vectors WHERE book_id IN ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, Arrays.asList(4001L, 4002L));
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        assert argList.size() == 1;
        DeleteReq param = (DeleteReq) argList.get(0);
        assert param.getFilter().contains("book_id in");
        assert param.getFilter().equals("book_id in {arg1}");
        assert param.getFilterTemplateValues().get("arg1").equals(Arrays.asList(4001L, 4002L));
    }

    @Test
    public void testUpsertArguments() {
        List<Object> argList = new ArrayList<>();
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if ("upsert".equals(method.getName())) {
                argList.addAll(Arrays.asList(args));
                MutationResult.Builder builder = MutationResult.newBuilder();
                builder.setInsertCnt(1);
                builder.addSuccIndex(0);
                return v2Response(method.getName(), builder.build());
            } else if ("insert".equals(method.getName())) {
                System.err.println("Unexpected call to insert instead of upsert");
                return v2Response(method.getName(), MutationResult.newBuilder().setInsertCnt(0).build());
            } else if ("describeCollection".equals(method.getName())) {
                CollectionSchema.Builder schemaBuilder = CollectionSchema.newBuilder();
                schemaBuilder.addFields(FieldSchema.newBuilder().setName("book_id").setDataType(DataType.Int64).setIsPrimaryKey(true).build());
                schemaBuilder.addFields(FieldSchema.newBuilder().setName("word_count").setDataType(DataType.Int32).build());
                schemaBuilder.addFields(FieldSchema.newBuilder().setName("book_intro").setDataType(DataType.FloatVector).addTypeParams(KeyValuePair.newBuilder().setKey(MilvusCommandKeys.DIMENSION).setValue("2").build()).build());

                DescribeCollectionResponse.Builder descBuilder = DescribeCollectionResponse.newBuilder();
                descBuilder.setSchema(schemaBuilder.build());
                descBuilder.setStatus(io.milvus.grpc.Status.newBuilder().setErrorCode(ErrorCode.Success).build());
                return v2Response(method.getName(), descBuilder.build());
            } else if ("close".equals(method.getName())) {
                return null;
            }
            return null;
        });

        try (Connection conn = getConnection()) {
            String sql = "UPSERT INTO book_vectors (book_id, word_count, book_intro) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, 2002L);
                ps.setInt(2, 700);
                ps.setObject(3, Arrays.asList(0.1f, 0.1f));
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        assert argList.size() == 1;
        assert argList.get(0) instanceof UpsertReq : "Expected UpsertReq but got " + argList.get(0).getClass().getName();
    }

    @Test
    public void testUpdateWithArgs() {
        List<Object> upsertArgs = new ArrayList<>();
        List<Object> queryArgs = new ArrayList<>();
        QueryIterator queryIterator = PowerMockito.mock(QueryIterator.class);
        QueryResultsWrapper.RowRecord row = new QueryResultsWrapper.RowRecord();
        row.put("book_id", 999L);
        row.put("word_count", 100);
        PowerMockito.when(queryIterator.next()).thenReturn(List.of(row), new ArrayList<>());

        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if ("queryIterator".equals(method.getName())) {
                queryArgs.addAll(Arrays.asList(args));
                return v2Response(method.getName(), queryIterator);
            } else if ("describeCollection".equals(method.getName())) {
                CollectionSchema schema = CollectionSchema.newBuilder()//
                        .addFields(FieldSchema.newBuilder().setName("book_id").setDataType(DataType.Int64).setIsPrimaryKey(true))//
                        .addFields(FieldSchema.newBuilder().setName("word_count").setDataType(DataType.Int32))//
                        .build();
                return v2Response(method.getName(), DescribeCollectionResponse.newBuilder()//
                        .setStatus(io.milvus.grpc.Status.newBuilder().setErrorCode(ErrorCode.Success))//
                        .setSchema(schema)//
                        .build());
            } else if ("close".equals(method.getName())) {
                return null;
            }
            if ("upsert".equals(method.getName())) {
                upsertArgs.addAll(Arrays.asList(args));
                return UpsertResp.builder().upsertCnt(((UpsertReq) args[0]).getData().size()).build();
            }
            return null;
        });

        try (Connection conn = getConnection()) {
            String sql = "UPDATE book_vectors SET word_count = ? WHERE book_id = ? LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, 888);
                ps.setLong(2, 999L);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        assert queryArgs.size() == 1;
        assert ((QueryIteratorReq) queryArgs.get(0)).getLimit() == 1;
        assert upsertArgs.size() == 1;
        UpsertReq uParam = (UpsertReq) upsertArgs.get(0);
        assert uParam.isPartialUpdate();
        assert uParam.getData().get(0).get("word_count").getAsInt() == 888;
        assert uParam.getData().get(0).get("book_id").getAsLong() == 999L;
        assert uParam.getData().get(0).size() == 2;
        assert ((QueryIteratorReq) queryArgs.get(0)).getOutputFields().equals(List.of("book_id"));
    }
}
