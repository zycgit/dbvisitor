/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.jdbc;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.jdbc.mapper.BeanMappingRowMapper;
import net.hasor.dbvisitor.jdbc.mapper.ColumnMapRowMapper;
import net.hasor.dbvisitor.jdbc.mapper.SingleColumnRowMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.contract.material.model.annotation.IgnoredEmailUser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class JdbcRowMapperCase extends JdbcResultHandlingSupport {
    @Test
    @Capability(CapabilityId.JDBC_RESULT_ROW_MAPPER_CUSTOM)
    public void rowMapper_shouldSupportCustomMappingAndDtoProjection() throws SQLException {
        seedUsers();

        RowMapper<UserNameAge> mapper = (rs, rowNum) -> new UserNameAge(rs.getString("name").toUpperCase() + ":" + rs.getInt("age"));

        List<UserNameAge> results = jdbcTemplate.queryForList(selectSql("*", "id BETWEEN ? AND ?", true), //
                new Object[] { baseId() + 1, baseId() + 3 }, mapper);

        assertEquals(3, results.size());
        assertEquals("NXN-RESULT-1:21", results.get(0).nameAge);
    }

    @Test
    @Capability(CapabilityId.JDBC_RESULT_ROW_MAPPER_BUILTIN)
    public void rowMapper_shouldSupportBuiltInMappers() throws SQLException {
        seedUsers();

        List<Map<String, Object>> maps = jdbcTemplate.queryForList(selectSql("id, name, age", "id = ?", false), //
                new Object[] { baseId() + 2 }, new ColumnMapRowMapper());
        List<String> names = jdbcTemplate.queryForList(selectSql("name", "id BETWEEN ? AND ?", true), //
                new Object[] { baseId() + 1, baseId() + 2 }, new SingleColumnRowMapper<>(String.class));
        List<UserInfo> beans = jdbcTemplate.queryForList(selectSql("*", "id = ?", false), //
                new Object[] { baseId() + 3 }, new BeanMappingRowMapper<>(UserInfo.class));

        assertEquals(1, maps.size());
        assertEquals("NXN-Result-2", value(maps.get(0), "name"));
        assertEquals(2, names.size());
        assertEquals("NXN-Result-1", names.get(0));
        assertEquals(1, beans.size());
        assertEquals(Integer.valueOf(baseId() + 3), beans.get(0).getId());
    }

    @Test
    @Capability(CapabilityId.JDBC_RESULT_IGNORE_FIELD_MAPPING)
    public void beanMapping_shouldHonorIgnoredFields() throws SQLException {
        int id = baseId() + 31;
        insertUser(id, "NXN-Ignore-Result", 30, "ignored-result@nxn.test");

        IgnoredEmailUser user = jdbcTemplate.queryForObject(selectSql("*", "id = ?", false), //
                new Object[] { id }, IgnoredEmailUser.class);

        assertNotNull(user);
        assertEquals(Integer.valueOf(id), user.getId());
        assertEquals("NXN-Ignore-Result", user.getName());
        assertEquals(Integer.valueOf(30), user.getAge());
        assertEquals(null, user.getEmail());
    }
}
