/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.annotation;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class AnnotationMapperGeneratedKeysContractTest extends AnnotationMapperAttributeSupport {
    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_GENERATED_KEYS)
    public void annotationAttributes_shouldPopulateGeneratedKeysAndSupportExplicitIds() throws Exception {
        requiresNxnFeature(FeatureId.GENERATED_KEYS_NUMERIC);
        UserInfo generated = user(null, "AttrGeneratedKey", 31, "generated@nxn.test");
        assertNull(generated.getId());

        assertEquals(1, this.mapper.insertWithGeneratedKeyNoKeyColumn(generated));

        assertNotNull(generated.getId());
        assertTrue(generated.getId() > 0);
        assertEquals("AttrGeneratedKey", this.mapper.selectByIdPrepared(generated.getId()).getName());

        int explicitId = explicitId(101);
        UserInfo explicit = user(explicitId, "AttrManualKey", 32, "manual@nxn.test");
        assertEquals(1, this.mapper.insertWithoutGeneratedKey(explicit));
        assertEquals(Integer.valueOf(explicitId), explicit.getId());
        assertEquals("AttrManualKey", this.mapper.selectByIdPrepared(explicitId).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_KEY_COLUMN)
    public void annotationAttributes_shouldPopulateGeneratedKeysWithKeyColumnWhenSupported() throws Exception {
        requiresNxnFeature(FeatureId.GENERATED_KEY_COLUMN);
        UserInfo user = user(null, "AttrKeyColumn", 33, "key-column@nxn.test");

        assertEquals(1, this.mapper.insertWithKeyProperty(user));

        assertNotNull(user.getId());
        assertTrue(user.getId() > 0);
        assertEquals("AttrKeyColumn", this.mapper.selectByIdPrepared(user.getId()).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_RESULT_SET_KEY_SOURCE)
    public void annotationAttributes_shouldPopulateGeneratedKeysFromCurrentResultSetWhenSupported() throws Exception {
        requiresNxnFeature(FeatureId.GENERATED_KEY_RESULT_SET);
        UserInfo user = user(null, "AttrResultSetKeySource", 34, "result-set-key-source@nxn.test");

        assertEquals(1, this.mapper.insertWithGeneratedKeyResultSet(user));

        assertNotNull(user.getId());
        assertTrue(user.getId() > 0);
        assertEquals("AttrResultSetKeySource", this.mapper.selectByIdPrepared(user.getId()).getName());
    }
}
