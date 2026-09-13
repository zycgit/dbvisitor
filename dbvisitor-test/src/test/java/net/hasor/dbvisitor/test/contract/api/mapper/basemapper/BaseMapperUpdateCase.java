/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.basemapper;

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
public abstract class BaseMapperUpdateCase extends BaseMapperCrudSupport {
    @Test
    @Capability(CapabilityId.BASEMAPPER_REPLACE)
    public void baseMapperReplace_shouldWriteNullFields() {
        int id = baseId() + 261;
        this.mapper.insert(user(id, "BaseReplaceBefore", 28, "replace@basemapper.com"));

        UserInfo replacement = new UserInfo();
        replacement.setId(id);
        replacement.setName("BaseReplaced");
        replacement.setAge(null);
        int result = this.mapper.replace(replacement);
        UserInfo loaded = this.mapper.selectById(id);

        assertEquals(1, result);
        assertEquals("BaseReplaced", loaded.getName());
        assertNull(loaded.getAge());
        assertNull(loaded.getEmail());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_UPSERT_INSERT)
    public void baseMapperUpsert_shouldInsertMissingEntity() {
        int id = baseId() + 271;

        int result = this.mapper.upsert(user(id, "BaseUpsertInsert", 32, "upsert-insert@basemapper.com"));
        UserInfo loaded = this.mapper.selectById(id);

        assertTrue(result >= 1);
        assertNotNull(loaded);
        assertEquals("BaseUpsertInsert", loaded.getName());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_UPSERT_UPDATE)
    public void baseMapperUpsert_shouldUpdateExistingEntity() {
        int id = baseId() + 281;
        this.mapper.insert(user(id, "BaseUpsertBefore", 25, "upsert-update@basemapper.com"));

        int result = this.mapper.upsert(user(id, "BaseUpsertAfter", 26, "upsert-update@basemapper.com"));
        UserInfo loaded = this.mapper.selectById(id);

        assertTrue(result >= 1);
        assertEquals("BaseUpsertAfter", loaded.getName());
        assertEquals(Integer.valueOf(26), loaded.getAge());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_UPDATE)
    public void baseMapperUpdate_shouldUpdateOnlyNonNullFields() {
        this.mapper.insert(user(baseId() + 81, "BaseUpdate", 81, "before@basemapper.com"));

        UserInfo update = new UserInfo();
        update.setId(baseId() + 81);
        update.setAge(82);

        int result = this.mapper.update(update);
        UserInfo loaded = this.mapper.selectById(baseId() + 81);

        assertEquals(1, result);
        assertEquals("BaseUpdate", loaded.getName());
        assertEquals(Integer.valueOf(82), loaded.getAge());
        assertEquals("before@basemapper.com", loaded.getEmail());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_UPDATE_BOUNDARY)
    public void baseMapperUpdate_shouldReturnZeroForMissingOrNullPrimaryKey() {
        UserInfo missing = user(baseId() + 99901, "BaseMissingUpdate", 30, null);
        assertMutationRows(0, this.mapper.update(missing));

        UserInfo nullPrimaryKey = new UserInfo();
        nullPrimaryKey.setName("BaseNullPrimaryKey");
        nullPrimaryKey.setAge(30);
        assertMutationRows(0, this.mapper.update(nullPrimaryKey));
    }

}
