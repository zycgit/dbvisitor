/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusGroupingSqlTest extends MilvusSqlContractSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_GROUPED_SEARCH)
    public void nativeGroupingShouldReturnOneOrMoreEntitiesPerGroupWithoutSqlAggregation() throws SQLException {
        prepareGroups();
        String search = "SELECT id,category,score FROM " + this.collection + " ORDER BY v <-> [0,0] ";
        try (Statement statement = this.connection.createStatement()) {
            statement.setFetchSize(1);
            try (ResultSet rows = statement.executeQuery(search + "WITH (group_by_field='category',group_limit=3)")) {
                assertEquals(Map.of("a", 1, "b", 1, "c", 1), groupCounts(rows));
            }
            String options = "WITH (group_by_field='category',group_limit=3,group_size=2,strict_group_size=true)";
            try (ResultSet rows = statement.executeQuery(search + options)) {
                assertEquals(Map.of("a", 2, "b", 2, "c", 2), groupCounts(rows));
            }
            // Only requested columns appear; grouping does not add an implicit output column.
            assertTrue(statement.execute("SELECT id FROM " + this.collection + " ORDER BY v <-> [0,0] " + options));
            try (ResultSet rows = statement.getResultSet()) {
                assertEquals(1, rows.getMetaData().getColumnCount());
                assertEquals(6, readIds(rows).size());
            }
            assertFalse(statement.getMoreResults());
            assertEquals(-1, statement.getUpdateCount());
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_GROUPED_SEARCH_WINDOW)
    public void sqlRowWindowsAndNativeGroupWindowsShouldHonorDifferentUnits() throws SQLException {
        prepareGroups();
        String selection = "SELECT id,category FROM " + this.collection + " WHERE id>? ORDER BY v <-> ? ";
        String grouping = "WITH (group_by_field=?,group_limit=?,group_offset=?,group_size=?,strict_group_size=?)";
        List<Long> groupWindow;
        try (PreparedStatement query = this.connection.prepareStatement(selection + grouping)) {
            query.setLong(1, 0);
            query.setObject(2, new float[] { 0, 0 });
            bindGrouping(query, 3);
            try (ResultSet rows = query.executeQuery()) {
                groupWindow = readIds(rows);
                assertEquals(Arrays.asList(3L, 4L, 5L, 6L), groupWindow);
            }
        }
        try (PreparedStatement query = this.connection.prepareStatement(selection + "LIMIT ? OFFSET ? " + grouping)) {
            query.setFetchSize(1);
            query.setLong(1, 0);
            query.setObject(2, new float[] { 0, 0 });
            query.setInt(3, 2);
            query.setInt(4, 1);
            bindGrouping(query, 5);
            try (ResultSet rows = query.executeQuery()) {
                assertEquals(groupWindow.subList(1, 3), readIds(rows));
            }
            query.setMaxRows(1);
            try (ResultSet rows = query.executeQuery()) {
                assertEquals(groupWindow.subList(1, 2), readIds(rows));
            }
            query.setMaxRows(0);
            query.setInt(4, 4);
            try (ResultSet rows = query.executeQuery()) {
                assertFalse(rows.next());
            }
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_GROUPED_HYBRID)
    public void hybridGroupingShouldFuseCandidatesThenReturnGroupedEntities() throws SQLException {
        prepareGroups();
        String search = "SELECT id,category,score FROM " + this.collection + """
                 WHERE id > ? ORDER BY HYBRID (v <-> ? LIMIT 6, w <-> ? LIMIT 6)
                """;
        String grouping = "WITH (reranker='rrf',group_by_field='category',group_limit=3,group_size=2,strict_group_size=true)";
        List<Long> all;
        try (PreparedStatement query = this.connection.prepareStatement(search + grouping)) {
            bindHybrid(query);
            try (ResultSet rows = query.executeQuery()) {
                Map<String, Integer> groups = new LinkedHashMap<>();
                all = new ArrayList<>();
                while (rows.next()) {
                    all.add(rows.getLong("id"));
                    groups.merge(rows.getString("category"), 1, Integer::sum);
                    assertTrue(rows.getDouble("score") > 0);
                }
                assertEquals(Map.of("a", 2, "b", 2, "c", 2), groups);
            }
        }
        try (PreparedStatement query = this.connection.prepareStatement(search + "LIMIT 2 OFFSET 1 " + grouping)) {
            query.setFetchSize(1);
            bindHybrid(query);
            try (ResultSet rows = query.executeQuery()) {
                assertEquals(all.subList(1, 3), readIds(rows));
            }
        }
    }

    private void prepareGroups() throws SQLException {
        createCollection("id INT64 PRIMARY KEY, category VARCHAR(16), v FLOAT_VECTOR(2), w FLOAT_VECTOR(2)");
        createIndex("v", "FLAT", "L2");
        createIndex("w", "FLAT", "L2");
        this.jdbcTemplate.executeUpdate("INSERT INTO " + this.collection + """
                 (id,category,v,w) VALUES
                 (1,'a',[0.1,0],[0.1,0]), (2,'a',[0.2,0],[0.2,0]),
                 (3,'b',[1.0,0],[1.0,0]), (4,'b',[1.1,0],[1.1,0]),
                 (5,'c',[2.0,0],[2.0,0]), (6,'c',[2.1,0],[2.1,0])
                """);
        this.jdbcTemplate.execute("FLUSH " + this.collection);
        loadCollection();
    }

    private Map<String, Integer> groupCounts(ResultSet rows) throws SQLException {
        Map<String, Integer> counts = new LinkedHashMap<>();
        while (rows.next()) {
            counts.merge(rows.getString("category"), 1, Integer::sum);
        }
        return counts;
    }

    private void bindGrouping(PreparedStatement query, int index) throws SQLException {
        query.setString(index, "category");
        query.setInt(index + 1, 2);
        query.setInt(index + 2, 1);
        query.setInt(index + 3, 2);
        query.setBoolean(index + 4, true);
    }

    private void bindHybrid(PreparedStatement query) throws SQLException {
        query.setLong(1, 0);
        query.setObject(2, new float[] { 0, 0 });
        query.setObject(3, new float[] { 0, 0 });
    }
}
