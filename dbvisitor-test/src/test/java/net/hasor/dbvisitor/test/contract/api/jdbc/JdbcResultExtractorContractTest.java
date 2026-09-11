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
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class JdbcResultExtractorContractTest extends JdbcResultHandlingSupport {
    @Test
    @Capability(CapabilityId.JDBC_RESULT_EXTRACTOR_CUSTOM)
    public void resultSetExtractor_shouldSupportCustomAggregation() throws SQLException {
        seedUsers();

        ResultSetExtractor<Map<Integer, String>> extractor = rs -> {
            Map<Integer, String> map = new HashMap<>();
            while (rs.next()) {
                map.put(rs.getInt("id"), rs.getString("name"));
            }
            return map;
        };

        Map<Integer, String> result = jdbcTemplate.query(selectSql("id, name", "id BETWEEN ? AND ?", true), //
                new Object[] { baseId() + 1, baseId() + 3 }, extractor);

        assertEquals(3, result.size());
        assertEquals("NXN-Result-1", result.get(baseId() + 1));
        assertEquals("NXN-Result-3", result.get(baseId() + 3));
    }

    @Test
    @Capability(CapabilityId.JDBC_RESULT_EXTRACTOR_BUILTIN)
    public void resultSetExtractor_shouldSupportBuiltInListExtractors() throws SQLException {
        seedUsers();

        List<Map<String, Object>> columnMaps = jdbcTemplate.query(selectSql("id, name, age", "age > ?", true), //
                new Object[] { 26 }, new ColumnMapResultSetExtractor());
        RowMapper<UserInfo> rowMapper = new BeanMappingRowMapper<>(UserInfo.class);
        List<UserInfo> mappedRows = jdbcTemplate.query(selectSql("*", "id = ?", false), //
                new Object[] { baseId() + 6 }, new RowMapperResultSetExtractor<>(rowMapper));
        List<UserInfo> filteredRows = jdbcTemplate.query(selectSql("*", "id BETWEEN ? AND ?", true), //
                new Object[] { baseId() + 1, baseId() + 10 }, new FilterResultSetExtractor<>(rowMapper, user -> user.getAge() > 24));

        assertFalse(columnMaps.isEmpty());
        assertEquals(1, mappedRows.size());
        assertEquals("NXN-Result-6", mappedRows.get(0).getName());
        assertEquals(6, filteredRows.size());
        assertTrue(filteredRows.stream().allMatch(user -> user.getAge() > 24));
    }

    @Test
    @Capability(CapabilityId.JDBC_RESULT_EXTRACTOR_PAIRS)
    public void resultSetExtractor_shouldSupportPairsExtractor() throws SQLException {
        seedUsers();

        ResultSetExtractor<Map<Integer, String>> extractor = new PairsResultSetExtractor<>(TypeHandlerRegistry.DEFAULT, Integer.class, String.class);
        Map<Integer, String> result = jdbcTemplate.query(selectSql("id, name", "id BETWEEN ? AND ?", true), //
                new Object[] { baseId() + 7, baseId() + 9 }, extractor);

        assertEquals(3, result.size());
        assertEquals("NXN-Result-7", result.get(baseId() + 7));
        assertEquals("NXN-Result-9", result.get(baseId() + 9));
    }

    @Test
    @Capability(CapabilityId.JDBC_RESULT_EXTRACTOR_MAPPING)
    public void resultSetExtractor_shouldSupportMappingExtractors() throws SQLException {
        seedUsers();

        List<UserInfo> beans = jdbcTemplate.query(selectSql("*", "age = ?", false), //
                new Object[] { 25 }, new BeanMappingResultSetExtractor<>(UserInfo.class, MappingRegistry.DEFAULT));
        List<Map<String, Object>> maps = jdbcTemplate.query(selectSql("*", "id = ?", false), //
                new Object[] { baseId() + 10 }, new MapMappingResultSetExtractor(UserInfo.class, MappingRegistry.DEFAULT));

        assertEquals(1, beans.size());
        assertEquals("NXN-Result-5", beans.get(0).getName());
        assertEquals(1, maps.size());
        assertEquals(baseId() + 10, ((Number) value(maps.get(0), "id")).intValue());
    }
}
