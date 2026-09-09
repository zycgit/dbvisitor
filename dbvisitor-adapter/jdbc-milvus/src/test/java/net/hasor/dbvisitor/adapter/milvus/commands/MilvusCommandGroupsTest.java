package net.hasor.dbvisitor.adapter.milvus.commands;

import static net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.v2Response;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import org.junit.After;
import org.junit.Test;

import io.milvus.grpc.*;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.partition.request.LoadPartitionsReq;
import io.milvus.v2.service.partition.request.ReleasePartitionsReq;
import io.milvus.v2.service.utility.request.CreateAliasReq;
import io.milvus.v2.service.utility.request.DropAliasReq;
import io.milvus.v2.service.vector.request.DeleteReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.QueryReq;
import io.milvus.v2.service.vector.response.DeleteResp;
import io.milvus.v2.service.vector.response.InsertResp;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;

/** Verify package regrouping through JDBC and the existing SDK interceptor, not helper-only APIs. */
public class MilvusCommandGroupsTest {
    @After
    public void cleanup() {
        MilvusCommandInterceptor.resetInterceptor();
    }

    @Test
    public void mixedCommandGroupsKeepBindingOrderAndJdbcResults() throws Exception {
        List<String> calls = new ArrayList<>();
        Map<String, Object> requests = new LinkedHashMap<>();
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            String operation = method.getName();
            if ("getServerVersion".equals(operation)) {
                return "v2.6.2";
            }
            if ("describeCollection".equals(operation)) {
                return v2Response(operation, DescribeCollectionResponse.newBuilder().setSchema(CollectionSchema.newBuilder().setName("books").addFields(FieldSchema.newBuilder().setName("id").setDataType(DataType.Int64).setIsPrimaryKey(true))).build());
            }
            calls.add(operation);
            if (args.length > 0) {
                requests.put(operation, args[0]);
            }
            return switch (operation) {
                case "insert" -> InsertResp.builder().InsertCnt(1).build();
                case "query" -> v2Response(operation, QueryResults.newBuilder().build());
                case "delete" -> DeleteResp.builder().deleteCnt(1).build();
                default -> null;
            };
        });

        Properties properties = new Properties();
        properties.setProperty(MilvusKeys.CUSTOM_MILVUS, MilvusCustomClient.class.getName());
        properties.setProperty(MilvusKeys.INTERCEPTOR, MilvusCommandInterceptor.class.getName());
        String sql = """
                CREATE ALIAS books_alias FOR books;
                INSERT INTO books (id) VALUES (?);
                SELECT id FROM books WHERE id = ? LIMIT ?;
                DELETE FROM books WHERE id = ?;
                /*+ sync=? */ LOAD TABLE books PARTITION p;
                /*+ sync=? */ RELEASE TABLE books PARTITION p;
                FLUSH books;
                DROP ALIAS books_alias;
                """;
        try (Connection connection = new JdbcDriver().connect("jdbc:dbvisitor:milvus://test:19530/db1", properties); PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int repeat = 0; repeat < 2; repeat++) {
                calls.clear();
                requests.clear();
                long id = 101L + repeat;
                statement.setLong(1, id);
                statement.setLong(2, id + 1);
                statement.setLong(3, 5L + repeat);
                statement.setLong(4, id + 2);
                statement.setBoolean(5, false);
                statement.setBoolean(6, false);

                long[] counts = { 0, 1, -1, 1, 0, 0, 0, 0 };
                boolean resultSet = statement.execute();
                for (int index = 0; index < counts.length; index++) {
                    if (index > 0) {
                        resultSet = statement.getMoreResults();
                    }
                    if (index == 2) {
                        assertTrue(resultSet);
                        try (ResultSet result = statement.getResultSet()) {
                            assertFalse(result.next());
                        }
                    } else {
                        assertFalse(resultSet);
                    }
                    assertEquals(counts[index], statement.getLargeUpdateCount());
                }
                assertFalse(statement.getMoreResults());
                assertEquals(-1, statement.getLargeUpdateCount());

                assertEquals(Arrays.asList("createAlias", "insert", "query", "delete", "loadPartitions", "releasePartitions", "flush", "dropAlias"), calls);
                assertEquals("books_alias", ((CreateAliasReq) requests.get("createAlias")).getAlias());
                assertEquals(id, ((InsertReq) requests.get("insert")).getData().get(0).get("id").getAsLong());
                QueryReq query = (QueryReq) requests.get("query");
                assertEquals("id == {arg2}", query.getFilter());
                assertEquals(java.util.Map.of("arg2", id + 1), query.getFilterTemplateValues());
                assertEquals(5L + repeat, query.getLimit());
                assertEquals("id == {arg4}", ((DeleteReq) requests.get("delete")).getFilter());
                assertEquals(java.util.Map.of("arg4", id + 2), ((DeleteReq) requests.get("delete")).getFilterTemplateValues());
                assertEquals(List.of("p"), ((LoadPartitionsReq) requests.get("loadPartitions")).getPartitionNames());
                assertEquals(List.of("p"), ((ReleasePartitionsReq) requests.get("releasePartitions")).getPartitionNames());
                assertEquals("books_alias", ((DropAliasReq) requests.get("dropAlias")).getAlias());
            }
        }
    }
}
