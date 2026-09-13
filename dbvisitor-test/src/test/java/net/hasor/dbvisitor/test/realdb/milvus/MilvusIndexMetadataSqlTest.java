/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusIndexMetadataSqlTest extends MilvusSqlContractSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_INDEX_LISTING)
    public void indexListingShouldDistinguishUnindexedAndMultiIndexCollections() throws SQLException {
        createCollection("id INT64 PRIMARY KEY,v FLOAT_VECTOR(2),w FLOAT_VECTOR(2)");
        try (Statement statement = this.connection.createStatement()) {
            assertEquals(Set.of(), indexNames(statement));
            assertArrayEquals(new long[] { 0, 0 }, progress(statement, ""));
            createIndex("v", "FLAT", "L2");
            createIndex("w", "FLAT", "L2");
            assertEquals(Set.of("idx_v", "idx_w"), indexNames(statement));
            try (ResultSet index = statement.executeQuery("SHOW INDEX idx_v ON " + this.collection)) {
                assertTrue(index.next());
                assertEquals("idx_v", index.getString("INDEX"));
                assertEquals("v", index.getString("FIELD"));
                assertFalse(index.next());
            }
            statement.executeUpdate("DROP INDEX idx_v ON " + this.collection);
            assertEquals(Set.of("idx_w"), indexNames(statement));
            statement.executeUpdate("DROP INDEX idx_w ON " + this.collection);
            assertEquals(Set.of(), indexNames(statement));
            assertThrows(SQLException.class, () -> statement.executeQuery("SHOW INDEX missing ON " + this.collection));
            assertEquals(Set.of(), indexNames(statement));
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_INDEX_PROGRESS_TOTAL)
    public void unnamedProgressShouldSumNativePerIndexWorkCounters() throws SQLException {
        createCollection("id INT64 PRIMARY KEY,v FLOAT_VECTOR(2),w FLOAT_VECTOR(2)");
        try (Statement statement = this.connection.createStatement()) {
            statement.executeUpdate("INSERT INTO " + this.collection + " (id,v,w) VALUES (1,[1,0],[0,1]),(2,[0,1],[1,0])");
            statement.executeUpdate("FLUSH " + this.collection);
            createIndex("v", "FLAT", "L2");
            createIndex("w", "FLAT", "L2");
            long[] first = progress(statement, "idx_v");
            long[] second = progress(statement, "idx_w");
            long[] all = progress(statement, "");
            assertEquals(2L, first[0]);
            assertEquals(2L, second[0]);
            assertEquals(first[0] + second[0], all[0]);
            assertEquals(first[1] + second[1], all[1]);
            assertEquals(4L, all[0]);
        }
    }

    private Set<String> indexNames(Statement statement) throws SQLException {
        Set<String> names = new HashSet<>();
        try (ResultSet indexes = statement.executeQuery("SHOW INDEXES FROM " + this.collection)) {
            assertEquals(4, indexes.getMetaData().getColumnCount());
            while (indexes.next()) {
                names.add(indexes.getString("INDEX"));
                assertNotNull(indexes.getString("FIELD"));
                assertTrue(indexes.getString("PARAMS").contains("index_type=FLAT"));
            }
        }
        return names;
    }

    private long[] progress(Statement statement, String name) throws SQLException {
        try (ResultSet result = statement.executeQuery("SHOW PROGRESS OF INDEX " + name + " ON " + this.collection)) {
            assertTrue(result.next());
            long total = result.getLong("TOTAL");
            long indexed = result.getLong("INDEXED");
            assertFalse(result.next());
            return new long[] { total, indexed };
        }
    }
}
