/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.result;

import java.sql.SQLException;
import java.util.*;
import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.jdbc.mapper.*;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcResultHandlingSupport;
import net.hasor.dbvisitor.test.contract.material.handler.ResultHandlerProbe;
import net.hasor.dbvisitor.test.contract.material.model.annotation.IgnoredEmailUser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import net.hasor.dbvisitor.types.handler.string.StringTypeHandler;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class JdbcRowMapperCase extends JdbcResultHandlingSupport {
    // 能力归属：结果接收 / rowmapper / jdbc。
    @Test
    @Capability(value = CapabilityId.JDBC_RESULT_ROW_MAPPER_CUSTOM, column = "results/rowmapper/mapping", variants = { "jdbc" })
    public void rowMapper_shouldSupportCustomMappingAndDtoProjection() throws SQLException {
        seedUsers();

        List<Integer> rowNumbers = new ArrayList<>();
        RowMapper<UserNameAge> mapper = (rs, rowNum) -> {
            ResultHandlerProbe.record(rs);
            rowNumbers.add(rowNum);
            return new UserNameAge(rs.getString(customNameColumn()).toUpperCase(Locale.ROOT) + ":" + rs.getInt(customNumberColumn()));
        };

        List<UserNameAge> results = ResultHandlerProbe.verify(3, () -> jdbcTemplate.queryForList(selectSql("*", "id BETWEEN ? AND ?", true), //
                selectArguments("*", "id BETWEEN ? AND ?", baseId() + 1, baseId() + 3), mapper));

        assertEquals(3, results.size());
        assertEquals(expectedCustomRows(), results.stream().map(row -> row.nameAge).toList());
        assertEquals(List.of(0, 1, 2), rowNumbers);
        assertTrue(ResultHandlerProbe.verify(0, () -> jdbcTemplate.queryForList(emptyResultSql(), emptyResultArguments(), mapper)).isEmpty());
        assertEquals(List.of(0, 1, 2), rowNumbers);
        ResultHandlerProbe.verifyFailure(() -> jdbcTemplate.queryForList(selectSql("*", "id BETWEEN ? AND ?", true), //
                selectArguments("*", "id BETWEEN ? AND ?", baseId() + 1, baseId() + 3), mapper));
    }

    // 能力归属：结果接收 / rowmapper / jdbc。
    @Test
    @Capability(value = CapabilityId.JDBC_RESULT_ROW_MAPPER_BUILTIN, column = "results/rowmapper/mapping", variants = { "jdbc" })
    public void rowMapper_shouldSupportBuiltInMappers() throws SQLException {
        seedUsers();

        List<Map<String, Object>> maps = jdbcTemplate.queryForList(selectSql("id, name, age", "id = ?", false), //
                selectArguments("id, name, age", "id = ?", baseId() + 2), new ColumnMapRowMapper());
        List<String> names = jdbcTemplate.queryForList(selectSql("name", "id BETWEEN ? AND ?", true), //
                selectArguments("name", "id BETWEEN ? AND ?", baseId() + 1, baseId() + 2), new SingleColumnRowMapper<>(String.class));
        List<?> beans = jdbcTemplate.queryForList(selectSql("*", "id = ?", false), //
                selectArguments("*", "id = ?", baseId() + 3), new BeanMappingRowMapper<>(resultBeanType()));
        List<Map<String, Object>> mappedMaps = jdbcTemplate.queryForList(selectSql("*", "id = ?", false), //
                selectArguments("*", "id = ?", baseId() + 3), new MapMappingRowMapper(resultBeanType(), MappingRegistry.DEFAULT));
        List<String> typedNames = jdbcTemplate.queryForList(selectSql("name", "id BETWEEN ? AND ?", true), //
                selectArguments("name", "id BETWEEN ? AND ?", baseId() + 1, baseId() + 2), new TypeHandlerColumnRowMapper<>(new StringTypeHandler()));

        assertEquals(1, maps.size());
        for (Map.Entry<String, Object> entry : expectedColumnMap().entrySet()) {
            Object actual = value(maps.get(0), entry.getKey());
            if (entry.getValue() instanceof Number) {
                assertEquals(((Number) entry.getValue()).doubleValue(), ((Number) actual).doubleValue(), 0.0);
            } else {
                assertEquals(entry.getValue(), actual);
            }
        }
        assertEquals(2, names.size());
        assertEquals(expectedScalarNames(), names);
        assertEquals(1, beans.size());
        for (Map.Entry<String, Object> entry : expectedBean().entrySet()) {
            assertEquals(entry.getValue(), beanProperty(beans.get(0), entry.getKey()));
        }
        assertEquals(1, mappedMaps.size());
        for (Map.Entry<String, Object> entry : expectedBean().entrySet()) {
            assertEquals(entry.getValue(), value(mappedMaps.get(0), entry.getKey()));
        }
        assertEquals(expectedScalarNames(), typedNames);
    }

    // 能力归属：结果接收 / rowmapper / jdbc。
    @Test
    @Capability(value = CapabilityId.JDBC_RESULT_IGNORE_FIELD_MAPPING, column = "results/rowmapper/mapping", variants = { "jdbc" })
    public void beanMapping_shouldHonorIgnoredFields() throws SQLException {
        int id = baseId() + 31;
        insertUser(id, "NXN-Ignore-Result", 30, "ignored-result@nxn.test");

        Object user = jdbcTemplate.queryForObject(selectSql("*", "id = ?", false), //
                selectArguments("*", "id = ?", id), ignoredBeanType());

        assertNotNull(user);
        for (Map.Entry<String, Object> entry : expectedIgnoredBean(id).entrySet()) {
            assertEquals(entry.getValue(), beanProperty(user, entry.getKey()));
        }
    }

    protected String customNameColumn() {
        return "name";
    }

    protected String customNumberColumn() {
        return "age";
    }

    protected List<String> expectedCustomRows() {
        return List.of("NXN-RESULT-1:21", "NXN-RESULT-2:22", "NXN-RESULT-3:23");
    }

    protected Map<String, Object> expectedColumnMap() {
        return Map.of("id", baseId() + 2, "name", "NXN-Result-2", "age", 22);
    }

    protected List<String> expectedScalarNames() {
        return List.of("NXN-Result-1", "NXN-Result-2");
    }

    protected Map<String, Object> expectedBean() {
        return Map.of("id", baseId() + 3, "name", "NXN-Result-3", "age", 23, "email", "nxn-result-3@test.com");
    }

    protected Class<?> ignoredBeanType() {
        return IgnoredEmailUser.class;
    }

    protected Map<String, Object> expectedIgnoredBean(int id) {
        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put("id", id);
        expected.put("name", "NXN-Ignore-Result");
        expected.put("age", 30);
        expected.put("email", null);
        return expected;
    }

    protected record UserNameAge(String nameAge) {
    }
}
