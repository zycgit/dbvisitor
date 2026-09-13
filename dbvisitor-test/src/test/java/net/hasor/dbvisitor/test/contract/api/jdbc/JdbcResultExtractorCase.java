/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.jdbc;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.jdbc.extractor.BeanMappingResultSetExtractor;
import net.hasor.dbvisitor.jdbc.extractor.ColumnMapResultSetExtractor;
import net.hasor.dbvisitor.jdbc.extractor.FilterResultSetExtractor;
import net.hasor.dbvisitor.jdbc.extractor.MapMappingResultSetExtractor;
import net.hasor.dbvisitor.jdbc.extractor.PairsResultSetExtractor;
import net.hasor.dbvisitor.jdbc.extractor.RowMapperResultSetExtractor;
import net.hasor.dbvisitor.jdbc.mapper.BeanMappingRowMapper;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class JdbcResultExtractorCase extends JdbcResultHandlingSupport {
    @Test
    @Capability(CapabilityId.JDBC_RESULT_EXTRACTOR_CUSTOM)
    public void resultSetExtractor_shouldSupportCustomAggregation() throws SQLException {
        seedUsers();

        ResultSetExtractor<Map<Object, Object>> extractor = rs -> {
            Map<Object, Object> map = new HashMap<>();
            while (rs.next()) {
                Object key = customIntegerKey() ? rs.getInt(customKeyColumn()) : rs.getString(customKeyColumn());
                Object value = customIntegerValue() ? rs.getInt(customValueColumn()) : rs.getString(customValueColumn());
                map.put(key, value);
            }
            return map;
        };

        Map<Object, Object> result = jdbcTemplate.query(selectSql("id, name", "id BETWEEN ? AND ?", true), //
                selectArguments("id, name", "id BETWEEN ? AND ?", baseId() + 1, baseId() + 3), extractor);

        assertEquals(3, result.size());
        assertEquals(expectedCustomMap(), result);
    }

    @Test
    @Capability(CapabilityId.JDBC_RESULT_EXTRACTOR_BUILTIN)
    public void resultSetExtractor_shouldSupportBuiltInListExtractors() throws SQLException {
        seedUsers();

        List<Map<String, Object>> columnMaps = jdbcTemplate.query(selectSql("id, name, age", "age > ?", true), //
                selectArguments("id, name, age", "age > ?", 26), new ColumnMapResultSetExtractor());
        RowMapper<?> rowMapper = new BeanMappingRowMapper<>(resultBeanType());
        List<?> mappedRows = jdbcTemplate.query(selectSql("*", "id = ?", false), //
                selectArguments("*", "id = ?", baseId() + 6), new RowMapperResultSetExtractor<>(rowMapper));
        List<?> filteredRows = jdbcTemplate.query(selectSql("*", "id BETWEEN ? AND ?", true), //
                selectArguments("*", "id BETWEEN ? AND ?", baseId() + 1, baseId() + 10),
                new FilterResultSetExtractor<>(rowMapper, row -> ((Number) beanProperty(row, filterNumberProperty())).doubleValue() > 24));

        assertFalse(columnMaps.isEmpty());
        assertEquals(expectedColumnMapCount(), columnMaps.size());
        assertEquals(1, mappedRows.size());
        for (Map.Entry<String, Object> entry : expectedMappedRow().entrySet()) {
            assertEquals(entry.getValue(), beanProperty(mappedRows.get(0), entry.getKey()));
        }
        assertEquals(6, filteredRows.size());
        assertTrue(filteredRows.stream().allMatch(row -> ((Number) beanProperty(row, filterNumberProperty())).doubleValue() > 24));
    }

    @Test
    @Capability(CapabilityId.JDBC_RESULT_EXTRACTOR_PAIRS)
    public void resultSetExtractor_shouldSupportPairsExtractor() throws SQLException {
        seedUsers();

        ResultSetExtractor<? extends Map<?, ?>> extractor = new PairsResultSetExtractor<>(TypeHandlerRegistry.DEFAULT, pairKeyType(), pairValueType());
        Map<?, ?> result = jdbcTemplate.query(selectSql("id, name", "id BETWEEN ? AND ?", true), //
                selectArguments("id, name", "id BETWEEN ? AND ?", baseId() + 7, baseId() + 9), extractor);

        assertEquals(3, result.size());
        assertEquals(expectedPairs(), result);
    }

    @Test
    @Capability(CapabilityId.JDBC_RESULT_EXTRACTOR_MAPPING)
    public void resultSetExtractor_shouldSupportMappingExtractors() throws SQLException {
        seedUsers();

        List<?> beans = jdbcTemplate.query(selectSql("*", "age = ?", false), //
                selectArguments("*", "age = ?", 25), new BeanMappingResultSetExtractor<>(resultBeanType(), MappingRegistry.DEFAULT));
        List<Map<String, Object>> maps = jdbcTemplate.query(selectSql("*", "id = ?", false), //
                selectArguments("*", "id = ?", baseId() + 10), new MapMappingResultSetExtractor(resultBeanType(), MappingRegistry.DEFAULT));

        assertEquals(1, beans.size());
        for (Map.Entry<String, Object> entry : expectedMappingBean().entrySet()) {
            assertEquals(entry.getValue(), beanProperty(beans.get(0), entry.getKey()));
        }
        assertEquals(1, maps.size());
        for (Map.Entry<String, Object> entry : expectedMappingMap().entrySet()) {
            Object actual = value(maps.get(0), entry.getKey());
            if (entry.getValue() instanceof Number) {
                assertEquals(((Number) entry.getValue()).doubleValue(), ((Number) actual).doubleValue(), 0.0);
            } else {
                assertEquals(entry.getValue(), actual);
            }
        }
    }

    protected String customKeyColumn() {
        return "id";
    }
    protected String customValueColumn() {
        return "name";
    }
    protected boolean customIntegerKey() {
        return true;
    }
    protected boolean customIntegerValue() {
        return false;
    }
    protected Class<?> pairKeyType() {
        return Integer.class;
    }
    protected Class<?> pairValueType() {
        return String.class;
    }
    protected String filterNumberProperty() {
        return "age";
    }
    protected int expectedColumnMapCount() {
        return 4;
    }

    protected Map<Object, Object> expectedCustomMap() {
        return Map.of(baseId() + 1, "NXN-Result-1", baseId() + 2, "NXN-Result-2", baseId() + 3, "NXN-Result-3");
    }

    protected Map<Object, Object> expectedPairs() {
        return Map.of(baseId() + 7, "NXN-Result-7", baseId() + 8, "NXN-Result-8", baseId() + 9, "NXN-Result-9");
    }

    protected Map<String, Object> expectedMappedRow() {
        return Map.of("name", "NXN-Result-6");
    }

    protected Map<String, Object> expectedMappingBean() {
        return Map.of("name", "NXN-Result-5");
    }

    protected Map<String, Object> expectedMappingMap() {
        return Map.of("id", baseId() + 10);
    }
}
