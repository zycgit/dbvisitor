/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.jdbc;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class JdbcBeanQueryCase extends JdbcQuerySupport {
    protected Class<?> queryBeanType() {
        return UserInfo.class;
    }

    protected Object[] beanRangeArguments() {
        return new Object[] { baseId() + 1, baseId() + 3 };
    }

    protected Map<String, Object> beanValues(Object bean) {
        UserInfo user = (UserInfo) bean;
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("id", user.getId());
        values.put("name", user.getName());
        values.put("age", user.getAge());
        values.put("email", user.getEmail());
        return values;
    }

    protected Map<String, Object> expectedBeanValues(int offset) {
        return Map.of("id", baseId() + offset, "name", "NXN-JDBC-Query-" + offset, "age", 60 + offset, "email", "nxn-jdbc-query-" + offset + "@test.com");
    }

    protected List<?> requiredBeanValues(Object bean) {
        return Collections.singletonList(((UserInfo) bean).getCreateTime());
    }

    // 能力归属：编程式 API / 查询。
    @Test
    @Capability(value = CapabilityId.JDBC_QUERY_BEAN, column = "jdbc/queries/queries")
    public void jdbcQueryForList_shouldReturnBeans() throws SQLException {
        seedUsers();

        List<?> rows = jdbcTemplate.queryForList(selectRange("id, name, age, email, create_time", "?", "?", true), //
                beanRangeArguments(), queryBeanType());

        assertEquals(3, rows.size());
        assertEquals(expectedBeanValues(2), beanValues(rows.get(1)));
        for (int i = 0; i < rows.size(); i++) {
            Object row = rows.get(i);
            assertEquals(expectedBeanValues(i + 1), beanValues(row));
            for (Object required : requiredBeanValues(row)) {
                assertNotNull(required);
            }
        }
    }
}
