/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.annotation;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class AnnotationMapperResultMappingContractTest extends AnnotationMapperResultMappingSupport {
    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_ENTITY)
    public void queryResult_shouldMapFullAndPartialEntity() throws Exception {
        UserInfo full = this.mapper.selectUserById(baseId() + 1);
        UserInfo partial = this.mapper.selectUserPartial(baseId() + 5);

        assertNotNull(full);
        assertEquals(Integer.valueOf(baseId() + 1), full.getId());
        assertEquals("AnnoResult1", full.getName());
        assertEquals(Integer.valueOf(21), full.getAge());
        assertEquals("anno-result1@nxn.test", full.getEmail());
        assertNotNull(full.getCreateTime());

        assertNotNull(partial);
        assertEquals(Integer.valueOf(baseId() + 5), partial.getId());
        assertEquals("AnnoResult5", partial.getName());
        assertNull(partial.getAge());
        assertNull(partial.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_MAP)
    public void queryResult_shouldMapSingleAndListRowsToMap() throws Exception {
        Map<String, Object> row = this.mapper.selectUserAsMap(baseId() + 2);
        List<Map<String, Object>> rows = this.mapper.selectUsersAsMapList();

        assertEquals(baseId() + 2, number(row, "id").intValue());
        assertEquals("AnnoResult2", value(row, "name"));
        assertTrue(rows.size() >= 10);
        assertNotNull(value(rows.get(0), "id"));
        assertNotNull(value(rows.get(0), "name"));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_SCALAR)
    public void queryResult_shouldMapSingleColumnToScalarTypes() throws Exception {
        assertEquals(Integer.valueOf(23), this.mapper.selectAgeById(baseId() + 3));
        assertEquals("AnnoResult4", this.mapper.selectNameById(baseId() + 4));
        assertTrue(this.mapper.selectCount() >= 10);
        assertNotNull(this.mapper.selectCreateTimeById(baseId() + 7));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_LIST)
    public void queryResult_shouldMapEntityAndScalarLists() throws Exception {
        List<UserInfo> users = this.mapper.selectUsersByAgeRange(21, 25);
        List<String> names = this.mapper.selectAllNames(baseId() + 1, baseId() + 10);
        List<Integer> ids = this.mapper.selectIdRange(baseId() + 1, baseId() + 10);

        assertTrue(users.size() >= 5);
        for (UserInfo user : users) {
            assertTrue(user.getAge() >= 21 && user.getAge() <= 25);
        }
        assertTrue(names.contains("AnnoResult1"));
        assertTrue(ids.contains(baseId() + 1));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_NULL)
    public void queryResult_shouldRepresentNoRowsAndNullColumns() throws Exception {
        UserInfo user = new UserInfo();
        user.setId(baseId() + 101);
        user.setName("AnnoResultNull");
        user.setAge(null);
        user.setEmail(null);
        user.setCreateTime(new Date());

        assertEquals(1, this.mapper.insertUser(user));

        UserInfo loaded = this.mapper.selectUserById(baseId() + 101);
        assertNull(this.mapper.selectUserById(baseId() + 999));
        assertNull(this.mapper.selectNameById(baseId() + 999));
        assertTrue(this.mapper.selectUsersByAgeRange(999, 1000).isEmpty());
        assertEquals("AnnoResultNull", loaded.getName());
        assertNull(loaded.getAge());
        assertNull(loaded.getEmail());
    }
}
