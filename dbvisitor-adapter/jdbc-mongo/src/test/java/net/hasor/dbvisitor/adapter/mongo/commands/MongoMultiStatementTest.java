package net.hasor.dbvisitor.adapter.mongo.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.hasor.dbvisitor.adapter.mongo.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.mongo.MongoCommandInterceptor;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.After;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;

public class MongoMultiStatementTest extends AbstractJdbcTest {
    @After
    public void cleanup() {
        MongoCommandInterceptor.resetInterceptor();
    }

    @Test
    public void allCommandsExecuteWithIndependentParameterOffsets() throws Exception {
        for (boolean literalFirst : new boolean[] { false, true }) {
            List<Document> calls = new ArrayList<>();
            MongoCommandInterceptor.resetInterceptor();
            MongoCommandInterceptor.addInterceptor(MongoDatabase.class, (proxy, method, args) -> {
                if ("getCollection".equals(method.getName())) {
                    MongoCollection collection = PowerMockito.mock(MongoCollection.class);
                    PowerMockito.when(collection.countDocuments(any(Bson.class))).thenAnswer(invocation -> {
                        calls.add(invocation.getArgument(0));
                        return 1L;
                    });
                    return collection;
                }
                return null;
            });
            String sql = "db.books.count({a:" + (literalFirst ? "101" : "?") + "});" + "db.books.count({a:?,b:?});db.books.count({a:303})";
            try (Connection conn = redisConnection("testdb"); PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int repeat = 0; repeat < 2; repeat++) {
                    calls.clear();
                    int index = 1;
                    if (!literalFirst)
                        ps.setInt(index++, 101);
                    ps.setInt(index++, 202 + repeat);
                    ps.setInt(index, 7 + repeat);
                    assertTrue(ps.execute());
                    for (int result = 0; result < 3; result++) {
                        try (ResultSet rs = ps.getResultSet()) {
                            assertTrue(rs.next());
                            assertEquals(1L, rs.getLong(1));
                            assertFalse(rs.next());
                        }
                        assertEquals(result < 2, ps.getMoreResults());
                    }
                    assertEquals(3, calls.size());
                    assertEquals(101, ((Number) calls.get(0).get("a")).intValue());
                    assertEquals(202 + repeat, ((Number) calls.get(1).get("a")).intValue());
                    assertEquals(7 + repeat, ((Number) calls.get(1).get("b")).intValue());
                    assertEquals(303, ((Number) calls.get(2).get("a")).intValue());
                }
            }
        }
    }
}
