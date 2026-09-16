/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.lambda;

import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.test.contract.material.model.UserBasicDTO;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class LambdaProjectionCase extends LambdaSelectSupport {
    // 能力归属：构造器 API / 查询操作。
    @Test
    @Capability(value = CapabilityId.LAMBDA_SELECT_SINGLE_COLUMN, column = "builder/queries/query")
    public void lambdaSelect_shouldReturnSingleProjectedColumnAsScalarList() throws Exception {
        insert(baseId() + 1, "SelectNameOne", 25, "select-name@nxn.test");

        List<String> names = lambdaTemplate.query(UserInfo.class)//
                .select(UserInfo::getName)//
                .eq(UserInfo::getId, baseId() + 1)//
                .queryForList(String.class);

        assertEquals(1, names.size());
        assertEquals("SelectNameOne", names.get(0));
    }

    // 能力归属：构造器 API / 查询操作。
    @Test
    @Capability(value = CapabilityId.LAMBDA_SELECT_MAP_DTO, column = "builder/queries/query")
    public void lambdaSelect_shouldProjectMultipleColumnsToMapAndDto() throws Exception {
        insert(baseId() + 10, "SelectMapDto", 30, "map-dto@nxn.test");

        List<Map<String, Object>> maps = lambdaTemplate.query(UserInfo.class)//
                .select(UserInfo::getName, UserInfo::getAge)//
                .eq(UserInfo::getId, baseId() + 10)//
                .queryForMapList();
        assertEquals(1, maps.size());
        Map<String, Object> map = maps.get(0);
        assertTrue(containsKey(map, "name"));
        assertTrue(containsKey(map, "age"));
        assertFalse(containsKey(map, "email"));
        assertEquals("SelectMapDto", getVal(map, "name"));
        assertEquals(30, ((Number) getVal(map, "age")).intValue());

        List<UserBasicDTO> dtos = lambdaTemplate.query(UserInfo.class)//
                .select(UserInfo::getName, UserInfo::getAge)//
                .eq(UserInfo::getId, baseId() + 10)//
                .queryForList(UserBasicDTO.class);
        assertEquals(1, dtos.size());
        assertEquals("SelectMapDto", dtos.get(0).getName());
        assertEquals(Integer.valueOf(30), dtos.get(0).getAge());
    }
}
