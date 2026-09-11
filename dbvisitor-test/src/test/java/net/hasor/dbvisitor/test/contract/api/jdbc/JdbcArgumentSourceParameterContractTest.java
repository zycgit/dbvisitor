/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.jdbc;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.dynamic.args.ArraySqlArgSource;
import net.hasor.dbvisitor.dynamic.args.BeanSqlArgSource;
import net.hasor.dbvisitor.dynamic.args.MapSqlArgSource;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class JdbcArgumentSourceParameterContractTest extends JdbcParameterSupport {
    @Test
    @Capability(CapabilityId.JDBC_PARAM_ARG_SOURCE)
    public void sqlArgSources_shouldBindArrayBeanAndMapSources() throws SQLException {
        jdbcTemplate.executeUpdate(command(JdbcParameterCommand.INSERT_ARRAY_SOURCE), //
                new ArraySqlArgSource(new Object[] { baseId() + 12, "NXN-Param-Source-Array", 26, "nxn-param-source-array@test.com", new Date() }));

        UserInfo bean = new UserInfo();
        bean.setId(baseId() + 13);
        bean.setName("NXN-Param-Source-Bean");
        bean.setAge(29);
        bean.setEmail("nxn-param-source-bean@test.com");
        bean.setCreateTime(new Date());
        jdbcTemplate.executeUpdate(command(JdbcParameterCommand.INSERT_COLON), new BeanSqlArgSource(bean));

        Map<String, Object> map = new HashMap<>();
        map.put("id", baseId() + 14);
        map.put("name", "NXN-Param-Source-Map");
        map.put("age", 34);
        map.put("email", "nxn-param-source-map@test.com");
        map.put("createTime", new Date());
        jdbcTemplate.executeUpdate(command(JdbcParameterCommand.INSERT_BRACE), new MapSqlArgSource(map));

        Long count = jdbcTemplate.queryForObject(//
                command(JdbcParameterCommand.COUNT_SOURCE_NAMES), //
                new MapSqlArgSource(new HashMap<String, Object>() {{
                    put("names", Arrays.asList("NXN-Param-Source-Array", "NXN-Param-Source-Bean", "NXN-Param-Source-Map"));
                    put("minAge", 25);
                }}), Long.class);

        assertEquals(Long.valueOf(3), count);
    }
}
