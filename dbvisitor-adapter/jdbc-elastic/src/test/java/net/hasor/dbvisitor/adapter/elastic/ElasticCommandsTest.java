/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.elastic;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.dbvisitor.driver.AdapterResultCursor;
import net.hasor.dbvisitor.driver.JdbcArg;
import net.hasor.dbvisitor.driver.JdbcArgMode;
import net.hasor.dbvisitor.driver.JdbcColumn;
import org.junit.Test;
import static org.junit.Assert.*;

public class ElasticCommandsTest {
    @Test
    public void argumentsKeepOffsetsNormalizeTimeAndDistinguishNullFromMissing() throws Exception {
        ElasticRequest request = new ElasticRequest("", false, false);
        LocalDateTime time = LocalDateTime.of(2026, 3, 15, 12, 30, 45);
        JdbcArg timestamp = new JdbcArg("arg2", JdbcArgMode.In);
        timestamp.setValue(time);
        JdbcArg nullable = new JdbcArg("arg3", JdbcArgMode.In);
        request.setArgMap(Map.of("arg2", timestamp, "arg3", nullable));

        AtomicInteger index = new AtomicInteger(1);
        assertEquals(Timestamp.valueOf(time), ElasticCommands.getArg(index, request));
        assertEquals(2, index.get());
        assertNull(ElasticCommands.getArg(index, request));
        assertEquals(3, index.get());
        SQLException missing = assertThrows(SQLException.class, () -> ElasticCommands.getArg(index, request));
        assertEquals("arg4 not found in request.", missing.getMessage());
    }

    @Test
    public void singleResultsPreserveNullValuesAndFinishTheCursor() throws Exception {
        ElasticRequest request = new ElasticRequest("", false, false);
        JdbcColumn column = ElasticCommands.COL_ID_STRING;
        try (AdapterResultCursor cursor = ElasticCommands.singleResult(request, column, null)) {
            assertEquals(Collections.singletonList(column), cursor.columns());
            assertFalse(cursor.isPending());
            assertTrue(cursor.next());
            assertNull(cursor.column(1));
            assertFalse(cursor.next());
        }
    }

    @Test
    public void scalarAndMappedListsRespectMaxRowsAndFinishEmptyResults() throws Exception {
        ElasticRequest request = new ElasticRequest("", false, false);
        JdbcColumn column = ElasticCommands.COL_ID_STRING;
        List<String> values = Arrays.asList("first", null, "last");
        List<Map<String, Object>> rows = Arrays.asList(Collections.singletonMap(column.name, "first"), Collections.singletonMap(column.name, null), Collections.singletonMap(column.name, "last"));
        for (int maxRows : new int[] { 0, 1, 2, 5 }) {
            request.setMaxRows(maxRows);
            int expected = maxRows == 0 ? values.size() : Math.min(maxRows, values.size());
            try (AdapterResultCursor scalars = ElasticCommands.listResult(request, column, values); AdapterResultCursor mapped = ElasticCommands.listResult(request, Collections.singletonList(column), rows)) {
                for (int index = 0; index < expected; index++) {
                    assertTrue(scalars.next());
                    assertTrue(mapped.next());
                    assertEquals(values.get(index), scalars.column(1));
                    assertEquals(values.get(index), mapped.column(1));
                }
                assertFalse(scalars.next());
                assertFalse(mapped.next());
                assertFalse(scalars.isPending());
                assertFalse(mapped.isPending());
            }
        }
        try (AdapterResultCursor scalars = ElasticCommands.listResult(request, column, Collections.emptyList()); AdapterResultCursor mapped = ElasticCommands.listResult(request, Collections.singletonList(column), Collections.emptyList())) {
            assertFalse(scalars.next());
            assertFalse(mapped.next());
        }
    }
}
