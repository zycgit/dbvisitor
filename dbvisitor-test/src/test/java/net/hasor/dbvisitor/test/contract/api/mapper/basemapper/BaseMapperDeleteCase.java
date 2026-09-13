/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.basemapper;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@NxnContract
public abstract class BaseMapperDeleteCase extends BaseMapperCrudSupport {
    @Test
    @Capability(CapabilityId.BASEMAPPER_DELETE)
    public void baseMapperDelete_shouldDeleteById() {
        this.mapper.insert(user(baseId() + 101, "BaseDelete", 101, null));

        int result = this.mapper.deleteById(baseId() + 101);

        assertEquals(1, result);
        assertNull(this.mapper.selectById(baseId() + 101));
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_DELETE_BY_IDS)
    public void baseMapperDeleteByIds_shouldDeleteOnlyRequestedPrimaryKeys() {
        for (int i = 1; i <= 5; i++) {
            this.mapper.insert(user(baseId() + 290 + i, "BaseDeleteIds" + i, 20 + i, null));
        }

        int result = this.mapper.deleteByIds(Arrays.asList(baseId() + 291, baseId() + 292, baseId() + 293));

        assertMutationRows(3, result);
        assertNull(this.mapper.selectById(baseId() + 291));
        assertNull(this.mapper.selectById(baseId() + 292));
        assertNull(this.mapper.selectById(baseId() + 293));
        assertNotNull(this.mapper.selectById(baseId() + 294));
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_DELETE_ENTITY)
    public void baseMapperDeleteEntity_shouldDeleteUsingEntityPrimaryKey() {
        int id = baseId() + 301;
        this.mapper.insert(user(id, "BaseDeleteEntity", 28, null));

        UserInfo delete = new UserInfo();
        delete.setId(id);
        int result = this.mapper.delete(delete);

        assertEquals(1, result);
        assertNull(this.mapper.selectById(id));
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_DELETE_LIST)
    public void baseMapperDeleteList_shouldDeleteEntityList() {
        // @formatter:off
        List<UserInfo> users = Arrays.asList(
            user(baseId() + 311, "BaseDeleteList1", 31, null),
            user(baseId() + 312, "BaseDeleteList2", 32, null),
            user(baseId() + 313, "BaseDeleteList3", 33, null),
            user(baseId() + 314, "BaseDeleteList4", 34, null)
        );
        // @formatter:on
        this.mapper.insert(users);

        int result = this.mapper.deleteList(users.subList(0, 3));

        assertMutationRows(3, result);
        assertNull(this.mapper.selectById(baseId() + 311));
        assertNull(this.mapper.selectById(baseId() + 312));
        assertNull(this.mapper.selectById(baseId() + 313));
        assertNotNull(this.mapper.selectById(baseId() + 314));
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_DELETE_BOUNDARY)
    public void baseMapperDelete_shouldReturnZeroForMissingPrimaryKey() {
        assertMutationRows(0, this.mapper.deleteById(baseId() + 99902));
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_DELETE_EMPTY_AND_NULL_KEYS)
    public void baseMapperDelete_shouldHandleEmptyAndNullPrimaryKeys() {
        assertMutationRows(0, this.mapper.deleteByIds(Arrays.asList()));
        try {
            this.mapper.deleteById(null);
            fail("Null primary key should be rejected.");
        } catch (Exception e) {
            String message = String.valueOf(e.getMessage()).toLowerCase();
            assertTrue(message.contains("null"));
        }
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_DELETE_REINSERT)
    public void baseMapperDelete_shouldAllowReinsertingDeletedPrimaryKey() {
        int reinsertId = baseId() + 321;
        this.mapper.insert(user(reinsertId, "BaseReinsertBefore", 30, null));
        assertEquals(1, this.mapper.deleteById(reinsertId));
        assertEquals(1, this.mapper.insert(user(reinsertId, "BaseReinsertAfter", 35, null)));
        assertEquals("BaseReinsertAfter", this.mapper.selectById(reinsertId).getName());
    }

}
