/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@NxnContract
public abstract class LambdaNullResultMappingCase extends LambdaResultHandlingSupport {
    @Test
    @Capability(CapabilityId.LAMBDA_RESULT_NULL_AND_CALCULATED)
    public void lambdaResult_shouldMapNullsAndMapToBean() throws SQLException {
        insertByJdbc(baseId() + 181, "LRNull", null, null);
        insertByJdbc(baseId() + 182, "LRCalc", 30, "lr-calc@test.com");

        RowMapper<UserInfo> nullSafeMapper = (rs, rowNum) -> {
            UserInfo user = new UserInfo();
            user.setId(rs.getInt("id"));
            user.setName(rs.getString("name"));
            int age = rs.getInt("age");
            user.setAge(rs.wasNull() ? null : age);
            user.setEmail(rs.getString("email"));
            return user;
        };
        UserInfo nullUser = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 181)//
                .queryForObject(nullSafeMapper);
        assertEquals("LRNull", nullUser.getName());
        assertNull(nullUser.getAge());
        assertNull(nullUser.getEmail());

        List<Map<String, Object>> maps = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 182)//
                .queryForMapList();
        assertEquals(1, maps.size());
        UserInfo bean = new UserInfo();
        bean.setId(((Number) getVal(maps.get(0), "id")).intValue());
        bean.setName((String) getVal(maps.get(0), "name"));
        bean.setAge(((Number) getVal(maps.get(0), "age")).intValue());
        assertEquals(Integer.valueOf(baseId() + 182), bean.getId());
        assertEquals("LRCalc", bean.getName());
        assertEquals(Integer.valueOf(30), bean.getAge());
    }
}
