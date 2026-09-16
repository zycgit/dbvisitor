/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.result;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.test.contract.api.lambda.LambdaResultHandlingSupport;
import net.hasor.dbvisitor.test.contract.material.handler.ResultHandlerProbe;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class LambdaRowMapperCase extends LambdaResultHandlingSupport {
    // 能力归属：结果接收 / rowmapper / builder。
    @Test
    @Capability(value = CapabilityId.LAMBDA_RESULT_ROW_MAPPER_CUSTOM, column = "results/rowmapper/mapping", variants = { "builder" })
    public void lambdaResult_shouldUseCustomRowMapperForSingleObject() throws SQLException {
        insertByJdbc(baseId() + 31, "LRMapper", 26, "lr-mapper@test.com");

        RowMapper<UserInfo> mapper = (rs, rowNum) -> {
            ResultHandlerProbe.record(rs);
            assertEquals(0, rowNum);
            UserInfo user = new UserInfo();
            user.setId(rs.getInt("id"));
            user.setName(rs.getString("name").toUpperCase(Locale.ROOT));
            user.setAge(rs.getInt("age") * 2);
            user.setEmail(rs.getString("email"));
            return user;
        };

        UserInfo result = ResultHandlerProbe.verify(1, () -> lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 31)//
                .queryForObject(mapper));

        assertNotNull(result);
        assertEquals(Integer.valueOf(baseId() + 31), result.getId());
        assertEquals("LRMAPPER", result.getName());
        assertEquals(Integer.valueOf(52), result.getAge());
        assertEquals("lr-mapper@test.com", result.getEmail());
        assertNull(ResultHandlerProbe.verify(0, () -> lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 999)//
                .queryForObject(mapper)));
        ResultHandlerProbe.verifyFailure(() -> lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 31)//
                .queryForObject(mapper));
    }

    // 能力归属：结果接收 / rowmapper / builder。
    @Test
    @Capability(value = CapabilityId.LAMBDA_RESULT_ROW_MAPPER_LIST, column = "results/rowmapper/mapping", variants = { "builder" })
    public void lambdaResult_shouldUseRowMapperForListsAndPartialObjects() throws SQLException {
        insertUsers("LRList", new int[] { 20, 25, 30 }, baseId() + 40);

        AtomicInteger rowCount = new AtomicInteger();
        RowMapper<String> nameMapper = (rs, rowNum) -> {
            ResultHandlerProbe.record(rs);
            assertEquals(rowCount.getAndIncrement(), rowNum);
            return rs.getString("name");
        };
        List<String> names = ResultHandlerProbe.verify(3, () -> orderRows(queryRows("LRList"), "id")//
                .queryForList(nameMapper));
        assertEquals(3, names.size());
        assertEquals("LRList1", names.get(0));
        assertEquals("LRList3", names.get(2));
        assertEquals(List.of("LRList1", "LRList2", "LRList3"), names);
        assertTrue(ResultHandlerProbe.verify(0, () -> queryRows("LRMissing").queryForList(nameMapper)).isEmpty());
        assertEquals(3, rowCount.get());

        RowMapper<UserInfo> partialMapper = (rs, rowNum) -> {
            ResultHandlerProbe.record(rs);
            UserInfo user = new UserInfo();
            user.setId(rs.getInt("id"));
            user.setName(rs.getString("name"));
            return user;
        };
        UserInfo partial = ResultHandlerProbe.verify(1, () -> lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 41)//
                .select("id", "name")//
                .queryForObject(partialMapper));
        assertEquals(Integer.valueOf(baseId() + 41), partial.getId());
        assertEquals("LRList2", partial.getName());
        assertNull(partial.getAge());
    }
}
