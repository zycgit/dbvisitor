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
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class AnnotationEmptyFieldMappingContractTest extends AnnotationMappingPolicySupport {
    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_EMPTY_STRING_ROUND_TRIP)
    public void annotationMapping_shouldPreserveEmptyStrings() throws SQLException {
        requiresNxnFeature(FeatureId.DISTINCT_EMPTY_STRING);
        int emptyId = baseId() + 117;
        deleteRaw(emptyId);
        UserInfo emptyValue = new UserInfo();
        emptyValue.setId(emptyId);
        emptyValue.setName("");
        emptyValue.setAge(25);
        emptyValue.setEmail("empty-columns@nxn.test");
        this.lambdaTemplate.insert(UserInfo.class).applyEntity(emptyValue).executeSumResult();

        UserInfo loadedEmpty = queryRaw(emptyId);
        assertNotNull(loadedEmpty);
        assertNotNull(loadedEmpty.getName());
        assertEquals("", loadedEmpty.getName());
        assertEquals(Integer.valueOf(25), loadedEmpty.getAge());
        assertEquals("empty-columns@nxn.test", loadedEmpty.getEmail());
    }
}
