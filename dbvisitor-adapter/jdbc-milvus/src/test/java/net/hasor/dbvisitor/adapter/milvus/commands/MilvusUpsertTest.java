package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;
import io.milvus.grpc.ErrorCode;
import io.milvus.grpc.MutationResult;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.UpsertReq;
import net.hasor.dbvisitor.adapter.milvus.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.Test;
import static net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.v2Response;

public class MilvusUpsertTest extends AbstractJdbcTest {

    private Connection getConnection() throws SQLException {
        Properties prop = new Properties();
        prop.setProperty(MilvusKeys.CUSTOM_MILVUS, MilvusCustomClient.class.getName());
        prop.setProperty(MilvusKeys.INTERCEPTOR, MilvusCommandInterceptor.class.getName());
        return new JdbcDriver().connect("jdbc:dbvisitor:milvus://xxxxxx:19530", prop);
    }

    @Test
    public void testUpsertCommand() throws SQLException {
        // 1. Setup Interceptor
        final Ref<UpsertReq> capturedParam = new Ref<>();
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if ("upsert".equals(method.getName())) {
                capturedParam.set((UpsertReq) args[0]);
                return v2Response(method.getName(), MutationResult.newBuilder().setUpsertCnt(1).build());
            }
            if ("describeCollection".equals(method.getName())) {
                return v2Response(method.getName(), io.milvus.grpc.DescribeCollectionResponse.newBuilder().setStatus(io.milvus.grpc.Status.newBuilder().setErrorCode(ErrorCode.Success).build()).setSchema(
                        io.milvus.grpc.CollectionSchema.newBuilder().setName("test_collection").addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("id").setDataType(io.milvus.grpc.DataType.Int64).setIsPrimaryKey(true).build()).addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("name").setDataType(io.milvus.grpc.DataType.VarChar).addTypeParams(io.milvus.grpc.KeyValuePair.newBuilder().setKey(MilvusCommandKeys.MAX_LENGTH).setValue("100").build()).build())
                                .addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("vector").setDataType(io.milvus.grpc.DataType.FloatVector).addTypeParams(io.milvus.grpc.KeyValuePair.newBuilder().setKey(MilvusCommandKeys.DIMENSION).setValue("1").build()).build()).build()).build());
            }
            if ("close".equals(method.getName())) {
                return null;
            }
            return null;
        });

        // 2. Execute SQL
        String sql = "UPSERT INTO test_collection (id, name, vector) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, 100L);
            ps.setObject(2, "test_name");
            ps.setObject(3, Collections.singletonList(0.1f));

            int result = ps.executeUpdate();

            // 3. Verify Result
            if (result != 1) {
                throw new RuntimeException("Expected 1 update count, got " + result);
            }
        }

        // 4. Verify Parameter
        UpsertReq param = capturedParam.get();
        if (param == null) {
            throw new RuntimeException("Upsert method was not called on MilvusClient");
        }

        if (!"test_collection".equals(param.getCollectionName())) {
            throw new RuntimeException("Collection name mismatch");
        }
    }

    // Simple container for capturing
    static class Ref<T> {
        private T value;

        void set(T value) {
            this.value = value;
        }

        T get() {
            return value;
        }
    }
}
