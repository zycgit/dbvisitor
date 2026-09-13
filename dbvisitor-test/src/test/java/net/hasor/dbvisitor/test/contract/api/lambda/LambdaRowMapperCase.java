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

import org.junit.Test;

import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@NxnContract
public abstract class LambdaRowMapperCase extends LambdaResultHandlingSupport {
    @Test
    @Capability(CapabilityId.LAMBDA_RESULT_ROW_MAPPER_CUSTOM)
    public void lambdaResult_shouldUseCustomRowMapperForSingleObject() throws SQLException {
        insertByJdbc(baseId() + 31, "LRMapper", 26, "lr-mapper@test.com");

        RowMapper<UserInfo> mapper = (rs, rowNum) -> {
            UserInfo user = new UserInfo();
            user.setId(rs.getInt("id"));
            user.setName(rs.getString("name").toUpperCase());
            user.setAge(rs.getInt("age") * 2);
            user.setEmail(rs.getString("email"));
            return user;
        };

        UserInfo result = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 31)//
                .queryForObject(mapper);

        assertNotNull(result);
        assertEquals(Integer.valueOf(baseId() + 31), result.getId());
        assertEquals("LRMAPPER", result.getName());
        assertEquals(Integer.valueOf(52), result.getAge());
        assertEquals("lr-mapper@test.com", result.getEmail());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_RESULT_ROW_MAPPER_LIST)
    public void lambdaResult_shouldUseRowMapperForListsAndPartialObjects() throws SQLException {
        insertUsers("LRList", new int[] { 20, 25, 30 }, baseId() + 40);

        RowMapper<String> nameMapper = (rs, rowNum) -> rs.getString("name");
        List<String> names = orderRows(queryRows("LRList"), "id")//
                .queryForList(nameMapper);
        assertEquals(3, names.size());
        assertEquals("LRList1", names.get(0));
        assertEquals("LRList3", names.get(2));

        RowMapper<UserInfo> partialMapper = (rs, rowNum) -> {
            UserInfo user = new UserInfo();
            user.setId(rs.getInt("id"));
            user.setName(rs.getString("name"));
            return user;
        };
        UserInfo partial = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 41)//
                .applySelect("id, name")//
                .queryForObject(partialMapper);
        assertEquals(Integer.valueOf(baseId() + 41), partial.getId());
        assertEquals("LRList2", partial.getName());
        assertNull(partial.getAge());
    }
}
