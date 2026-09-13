/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.mapping;

import java.sql.SQLException;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@NxnContract
public abstract class AnnotationNullFieldMappingCase extends AnnotationMappingPolicySupport {
    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_NULL_VALUE_ROUND_TRIP)
    public void annotationMapping_shouldRoundTripNullColumns() throws SQLException {
        int nullId = baseId() + 116;
        deleteRaw(nullId);

        UserInfo nullValue = new UserInfo();
        nullValue.setId(nullId);
        nullValue.setName("PolicyNullColumns");
        nullValue.setAge(null);
        nullValue.setEmail(null);
        this.lambdaTemplate.insert(UserInfo.class).applyEntity(nullValue).executeSumResult();

        UserInfo loadedNull = queryRaw(nullId);
        assertEquals("PolicyNullColumns", loadedNull.getName());
        assertNull(loadedNull.getAge());
        assertNull(loadedNull.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_UPDATE_NULL_VALUE)
    public void annotationMapping_shouldUpdateExplicitNullValues() throws SQLException {
        int id = baseId() + 51;
        insertRaw(id, "PolicyUpdateNull", 30, "update-null@nxn.test");

        int updated = this.lambdaTemplate.update(UserInfo.class)//
                .eq(UserInfo::getId, id)//
                .updateTo(UserInfo::getAge, null)//
                .updateTo(UserInfo::getEmail, null)//
                .doUpdate();

        UserInfo loaded = queryRaw(id);
        assertEquals(1, updated);
        assertNull(loaded.getAge());
        assertNull(loaded.getEmail());
    }
}
