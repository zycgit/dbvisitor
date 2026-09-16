/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.mapping;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class AnnotationPartialFieldMappingCase extends AnnotationMappingPolicySupport {
    // 能力归属：对象映射 / 写入策略 / 部分字段。
    @Test
    @Capability(value = CapabilityId.MAPPING_ANNOTATION_PARTIAL_INSERT, column = "mapping-keys/write-policies/fields")
    public void annotationMapping_shouldAllowPartialEntityInsert() throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(baseId() + 52);
        user.setName("PolicyPartialInsert");
        user.setAge(28);

        this.lambdaTemplate.insert(UserInfo.class).applyEntity(user).executeSumResult();
        UserInfo loaded = queryRaw(baseId() + 52);

        assertNotNull(loaded);
        assertEquals("PolicyPartialInsert", loaded.getName());
        assertEquals(Integer.valueOf(28), loaded.getAge());
        assertNull(loaded.getEmail());
    }

    // 能力归属：对象映射 / 写入策略 / 部分字段。
    @Test
    @Capability(value = CapabilityId.MAPPING_ANNOTATION_PARTIAL_UPDATE, column = "mapping-keys/write-policies/fields")
    public void annotationMapping_shouldUpdateOnlyExplicitFields() throws SQLException {
        int id = baseId() + 53;
        insertRaw(id, "PolicyPartialUpdate", 25, "partial-update@nxn.test");

        int updated = this.lambdaTemplate.update(UserInfo.class)//
                .eq(UserInfo::getId, id)//
                .updateTo(UserInfo::getName, "PolicyOnlyNameUpdated")//
                .doUpdate();

        UserInfo loaded = queryRaw(id);
        assertEquals(1, updated);
        assertEquals("PolicyOnlyNameUpdated", loaded.getName());
        assertEquals(Integer.valueOf(25), loaded.getAge());
        assertEquals("partial-update@nxn.test", loaded.getEmail());
    }
}
