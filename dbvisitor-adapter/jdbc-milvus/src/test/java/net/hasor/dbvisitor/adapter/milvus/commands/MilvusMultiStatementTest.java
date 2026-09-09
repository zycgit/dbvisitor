package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import io.milvus.grpc.QueryResults;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.QueryIteratorReq;
import io.milvus.v2.service.vector.request.QueryReq;
import net.hasor.dbvisitor.adapter.milvus.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.After;
import org.junit.Test;
import static net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.v2Response;
import static org.junit.Assert.*;

public class MilvusMultiStatementTest extends AbstractJdbcTest {
    @After
    public void cleanup() {
        MilvusCommandInterceptor.resetInterceptor();
    }

    @Test
    public void allCommandsExecuteWithIndependentParameterOffsets() throws Exception {
        for (boolean literalFirst : new boolean[] { false, true }) {
            List<Object> calls = new ArrayList<>();
            MilvusCommandInterceptor.resetInterceptor();
            MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
                if ("describeCollection".equals(method.getName())) {
                    return v2Response(method.getName(), io.milvus.grpc.DescribeCollectionResponse.newBuilder()
                            .setSchema(io.milvus.grpc.CollectionSchema.newBuilder().addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("book_id").setDataType(io.milvus.grpc.DataType.Int64).setIsPrimaryKey(true)).addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("word_count").setDataType(io.milvus.grpc.DataType.Int32)).addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("title").setDataType(io.milvus.grpc.DataType.VarChar)).addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("book_intro").setDataType(io.milvus.grpc.DataType.FloatVector))).build());
                }
                if ("query".equals(method.getName()) || "queryIterator".equals(method.getName())) {
                    calls.add(args[0]);
                    return v2Response(method.getName(), QueryResults.newBuilder().build());
                }
                return null;
            });
            Properties props = new Properties();
            props.setProperty(MilvusKeys.CUSTOM_MILVUS, MilvusCustomClient.class.getName());
            props.setProperty(MilvusKeys.INTERCEPTOR, MilvusCommandInterceptor.class.getName());
            String sql = "SELECT book_id FROM books WHERE book_id = " + (literalFirst ? "101" : "?") + ";" + "SELECT book_id FROM books WHERE book_id = ? LIMIT ?;" + "SELECT book_id FROM books WHERE book_id = 303;";
            try (Connection conn = new JdbcDriver().connect("jdbc:dbvisitor:milvus://test:19530", props); PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int repeat = 0; repeat < 2; repeat++) {
                    calls.clear();
                    int index = 1;
                    if (!literalFirst) {
                        ps.setInt(index++, 101);
                    }
                    ps.setInt(index++, 202 + repeat);
                    ps.setInt(index, 7 + repeat);
                    assertTrue(ps.execute());
                    for (int result = 0; result < 3; result++) {
                        try (ResultSet rs = ps.getResultSet()) {
                            assertFalse(rs.next());
                        }
                        assertEquals(result < 2, ps.getMoreResults());
                    }
                    assertEquals(-1, ps.getUpdateCount());
                    assertEquals(3, calls.size());
                    assertEquals(literalFirst ? "book_id == 101" : "book_id == {arg1}", ((QueryIteratorReq) calls.get(0)).getExpr());
                    assertEquals(literalFirst ? java.util.Map.of() : java.util.Map.of("arg1", 101), ((QueryIteratorReq) calls.get(0)).getFilterTemplateValues());
                    String secondArg = literalFirst ? "arg1" : "arg2";
                    assertEquals("book_id == {" + secondArg + "}", ((QueryReq) calls.get(1)).getFilter());
                    assertEquals(java.util.Map.of(secondArg, 202 + repeat), ((QueryReq) calls.get(1)).getFilterTemplateValues());
                    assertEquals(7L + repeat, ((QueryReq) calls.get(1)).getLimit());
                    assertEquals("book_id == 303", ((QueryIteratorReq) calls.get(2)).getExpr());
                }
            }
        }
    }
}
