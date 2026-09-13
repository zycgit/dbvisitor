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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;

import static org.junit.Assert.*;

/** Lambda page iteration over stable, explicitly vector-ordered Milvus results. */
public class MilvusLambdaIteratorSqlTest extends MilvusSqlContractSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_LAMBDA_ITERATOR_PAGES)
    public void pagedIteratorShouldPreserveOrderAndTransformAcrossPageBoundaries() throws SQLException {
        prepareRows(12);
        LambdaTemplate lambda = new LambdaTemplate(this.connection);
        Iterator<Long> iterator = lambda.queryFreedom(this.collection).ge("id", 3)
                .orderByL2("v", new float[] { 0, 0 })
                .iteratorByBatch(3, row -> ((Number) row.get("id")).longValue());
        List<Long> ids = new ArrayList<>();
        while (iterator.hasNext()) {
            assertTrue(iterator.hasNext());
            ids.add(iterator.next());
        }
        assertEquals(List.of(3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L), ids);
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_LAMBDA_ITERATOR_LIMIT_EMPTY)
    public void iteratorShouldStopAtNonPageAlignedLimitAndHandleEmptyQuery() throws SQLException {
        prepareRows(12);
        LambdaTemplate lambda = new LambdaTemplate(this.connection);
        Iterator<Long> limited = lambda.queryFreedom(this.collection).orderByL2("v", new float[] { 0, 0 })
                .iteratorForLimit(5, 3, row -> ((Number) row.get("id")).longValue());
        List<Long> ids = new ArrayList<>();
        while (limited.hasNext()) {
            ids.add(limited.next());
        }
        assertEquals(List.of(0L, 1L, 2L, 3L, 4L), ids);
        assertThrows(NoSuchElementException.class, limited::next);
        Iterator<?> empty = lambda.queryFreedom(this.collection).eq("id", 99).iteratorForLimit(-1, 3);
        assertFalse(empty.hasNext());
        assertThrows(NoSuchElementException.class, empty::next);
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_LAMBDA_ITERATOR_ALL)
    public void negativeLimitShouldTraverseAllRows() throws SQLException {
        prepareRows(12);
        Iterator<Map<String, Object>> iterator = new LambdaTemplate(this.connection).queryFreedom(this.collection)
                .orderByL2("v", new float[] { 0, 0 }).iteratorForLimit(-1, 3);
        int count = 0;
        while (iterator.hasNext()) {
            assertEquals(count, ((Number) iterator.next().get("id")).intValue());
            count++;
        }
        assertEquals(12, count);
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_LAMBDA_ITERATOR_EARLY_BREAK)
    public void consumerShouldStopBeforeExhaustion() throws SQLException {
        prepareRows(12);
        Iterator<Map<String, Object>> iterator = new LambdaTemplate(this.connection).queryFreedom(this.collection)
                .orderByL2("v", new float[] { 0, 0 }).iteratorForLimit(-1, 3);
        int count = 0;
        while (iterator.hasNext() && count < 10) {
            assertEquals(count, ((Number) iterator.next().get("id")).intValue());
            count++;
        }
        assertEquals(10, count);
        assertTrue(iterator.hasNext());
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_LAMBDA_ITERATOR_LARGE_BATCH)
    public void largeBatchShouldTraverseAllFiveHundredRows() throws SQLException {
        prepareRows(500);
        Iterator<Map<String, Object>> iterator = new LambdaTemplate(this.connection).queryFreedom(this.collection)
                .orderByL2("v", new float[] { 0, 0 }).iteratorByBatch(100);
        int count = 0;
        while (iterator.hasNext()) {
            assertEquals(count, ((Number) iterator.next().get("id")).intValue());
            count++;
        }
        assertEquals(500, count);
    }

    private void prepareRows(int count) throws SQLException {
        createCollection("id INT64 PRIMARY KEY, v FLOAT_VECTOR(2)");
        createIndex("v", "FLAT", "L2");
        loadCollection();
        try (PreparedStatement insert = this.connection.prepareStatement("INSERT INTO " + this.collection + " (id, v) VALUES (?, ?)")) {
            for (int i = 0; i < count; i++) {
                insert.setLong(1, i);
                insert.setObject(2, new float[] { i, 0 });
                assertEquals(1, insert.executeUpdate());
            }
        }
    }
}
