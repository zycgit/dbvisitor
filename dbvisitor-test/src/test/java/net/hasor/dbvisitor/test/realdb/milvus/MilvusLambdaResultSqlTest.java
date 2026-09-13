/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.jdbc.RowCallbackHandler;
import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.jdbc.extractor.FilterResultSetExtractor;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;
import static org.junit.Assert.*;

/** Result conversion uses native scalar filters and vector order, without relational projections. */
public class MilvusLambdaResultSqlTest extends MilvusSqlContractSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_LAMBDA_RESULT_VALUES)
    public void mapsScalarsAndCustomRowsShouldPreserveSelectedValues() throws SQLException {
        LambdaTemplate lambda = prepareRows();
        List<Map<String, Object>> rows = lambda.queryFreedom(this.collection)
                .orderByL2("v", new float[] { 0, 0 }).queryForMapList();
        assertEquals(3, rows.size());
        assertEquals("row1", rows.get(0).get("name"));
        assertEquals("row3", rows.get(2).get("name"));
        assertEquals(3, lambda.queryFreedom(this.collection).queryForCount());
        assertEquals(Long.valueOf(1), lambda.queryFreedom(this.collection).eq("id", 1)
                .applySelect("id").queryForObject(Long.class));
        assertEquals("1", lambda.queryFreedom(this.collection).eq("id", 1)
                .applySelect("id").queryForObject(String.class));
        RowMapper<String> mapper = (rs, rowNum) -> rowNum + ":" + rs.getString("name");
        assertEquals(List.of("0:row1", "1:row2", "2:row3"), lambda.queryFreedom(this.collection)
                .orderByL2("v", new float[] { 0, 0 }).queryForList(mapper));
        assertNull(lambda.queryFreedom(this.collection).eq("id", 99).queryForObject(mapper));
        assertTrue(lambda.queryFreedom(this.collection).eq("id", 99).queryForMapList().isEmpty());
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_LAMBDA_RESULT_CALLBACK)
    public void callbackShouldObserveRowsInVectorOrderAndSkipEmptyResults() throws SQLException {
        LambdaTemplate lambda = prepareRows();
        List<String> observed = new ArrayList<>();
        RowCallbackHandler callback = (rs, rowNum) -> observed.add(rowNum + ":" + rs.getString("name"));
        lambda.queryFreedom(this.collection).orderByL2("v", new float[] { 0, 0 }).query(callback);
        assertEquals(List.of("0:row1", "1:row2", "2:row3"), observed);
        lambda.queryFreedom(this.collection).eq("id", 99).query(callback);
        assertEquals(3, observed.size());
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_LAMBDA_RESULT_EXTRACTORS)
    public void extractorsAndPairsShouldTransformOnlyReturnedRows() throws SQLException {
        LambdaTemplate lambda = prepareRows();
        ResultSetExtractor<Map<Long, String>> extractor = rs -> {
            Map<Long, String> values = new LinkedHashMap<>();
            while (rs.next()) {
                values.put(rs.getLong("id"), rs.getString("name"));
            }
            return values;
        };
        Map<Long, String> expected = Map.of(1L, "row1", 2L, "row2", 3L, "row3");
        assertEquals(expected, lambda.queryFreedom(this.collection).query(extractor));
        assertEquals(expected, lambda.queryFreedom(this.collection).queryForPairs("id", "name", Long.class, String.class));
        ResultSetExtractor<List<Long>> filtered = new FilterResultSetExtractor<>((rs, rowNum) -> rs.getLong("id"), id -> id != 2L);
        assertEquals(List.of(1L, 3L), lambda.queryFreedom(this.collection)
                .orderByL2("v", new float[] { 0, 0 }).query(filtered));
        assertTrue(lambda.queryFreedom(this.collection).eq("id", 99).query(extractor).isEmpty());
        assertTrue(lambda.queryFreedom(this.collection).eq("id", 99).query(filtered).isEmpty());
    }

    private LambdaTemplate prepareRows() throws SQLException {
        createCollection("id INT64 PRIMARY KEY, name VARCHAR(32), v FLOAT_VECTOR(2)");
        createIndex("v", "FLAT", "L2");
        loadCollection();
        try (PreparedStatement insert = this.connection.prepareStatement("INSERT INTO " + this.collection + " (id, name, v) VALUES (?, ?, ?)")) {
            for (int i = 1; i <= 3; i++) {
                insert.setLong(1, i);
                insert.setString(2, "row" + i);
                insert.setObject(3, new float[] { i, 0 });
                assertEquals(1, insert.executeUpdate());
            }
        }
        return new LambdaTemplate(this.connection);
    }
}
