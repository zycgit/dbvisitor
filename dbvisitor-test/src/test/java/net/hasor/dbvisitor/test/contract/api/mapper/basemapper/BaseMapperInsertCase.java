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

@NxnContract
public abstract class BaseMapperInsertCase extends BaseMapperCrudSupport {
    @Test
    @Capability(CapabilityId.BASEMAPPER_INSERT)
    public void baseMapperInsert_shouldInsertSingleEntity() {
        UserInfo user = user(baseId() + 1, "BaseInsert", 30, "insert@basemapper.com");

        int result = this.mapper.insert(user);
        UserInfo loaded = this.mapper.selectById(baseId() + 1);

        assertEquals(1, result);
        assertNotNull(loaded);
        assertEquals("BaseInsert", loaded.getName());
        assertEquals(Integer.valueOf(30), loaded.getAge());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_BATCH_INSERT)
    public void baseMapperBatchInsert_shouldInsertEntityList() {
        // @formatter:off
        List<UserInfo> users = Arrays.asList(
            user(baseId() + 11, "BaseBatch1", 21, "batch1@basemapper.com"),
            user(baseId() + 12, "BaseBatch2", 22, "batch2@basemapper.com"),
            user(baseId() + 13, "BaseBatch3", 23, "batch3@basemapper.com")
        );
        // @formatter:on

        int result = this.mapper.insert(users);
        List<UserInfo> loaded = this.mapper.selectByIds(Arrays.asList(baseId() + 11, baseId() + 12, baseId() + 13));

        assertEquals(3, result);
        assertEquals(3, loaded.size());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_INSERT_BOUNDARY)
    public void baseMapperInsert_shouldHandleNullsEmptyListsSpecialValuesAndLargeBatches() {
        int nullFieldsId = baseId() + 201;
        int allNullId = baseId() + 202;
        int specialId = baseId() + 204;
        int unicodeId = baseId() + 205;

        assertEquals(1, this.mapper.insert(user(nullFieldsId, "BaseNullFields", null, null)));
        UserInfo nullFields = this.mapper.selectById(nullFieldsId);
        assertNull(nullFields.getAge());
        assertNull(nullFields.getEmail());

        UserInfo allNull = new UserInfo();
        allNull.setId(allNullId);
        assertEquals(1, this.mapper.insert(allNull));
        UserInfo loadedAllNull = this.mapper.selectById(allNullId);
        assertNotNull(loadedAllNull);
        assertNull(loadedAllNull.getName());
        assertNull(loadedAllNull.getAge());
        assertNull(loadedAllNull.getEmail());

        assertEquals(0, this.mapper.insert(Arrays.<UserInfo>asList()));

        assertEquals(1, this.mapper.insert(user(specialId, "O'Brien & Co.", 35, "special@basemapper.com")));
        assertEquals("O'Brien & Co.", this.mapper.selectById(specialId).getName());

        assertEquals(1, this.mapper.insert(user(unicodeId, "Test User Unicode", 28, "unicode@basemapper.com")));
        assertEquals("Test User Unicode", this.mapper.selectById(unicodeId).getName());

        List<UserInfo> batch = new java.util.ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            batch.add(user(baseId() + 220 + i, "BaseLargeBatch" + i, 20 + (i % 10), "large" + i + "@basemapper.com"));
        }
        assertEquals(25, this.mapper.insert(batch));
        assertNotNull(this.mapper.selectById(baseId() + 221));
        assertNotNull(this.mapper.selectById(baseId() + 245));
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_EDGE_INSERT)
    public void baseMapperInsert_shouldHandleNegativeKeyAndEscapedValues() {
        int negativeId = -baseId();
        assertEquals(1, this.mapper.insert(user(negativeId, "BaseNegativeKey", 25, null)));
        assertEquals("BaseNegativeKey", this.mapper.selectById(negativeId).getName());

        int quotedId = baseId() + 361;
        String quotedName = "Test'Quote\"Double\\Slash";
        assertEquals(1, this.mapper.insert(user(quotedId, quotedName, 28, null)));
        assertEquals(quotedName, this.mapper.selectById(quotedId).getName());
    }
}
