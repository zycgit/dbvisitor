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

@NxnContract
public abstract class AnnotationMapperSelectKeyContractTest extends AnnotationMapperAttributeSupport {
    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_SELECT_KEY)
    public void annotationAttributes_shouldRunSelectKeySqlWhenFixtureSequenceIsSupported() throws Exception {
        requiresNxnFeature(FeatureId.XML_SELECT_KEY_USER_INFO_SEQUENCE);

        UserInfo before = user(null, "AttrSelectKeyBefore", 34, "select-before@nxn.test");
        assertEquals(1, this.mapper.insertWithSelectKeyBefore(before));
        assertNotNull(before.getId());
        assertEquals("AttrSelectKeyBefore", this.mapper.selectByIdPrepared(before.getId()).getName());

        UserInfo after = user(null, "AttrSelectKeyAfter", 35, "select-after@nxn.test");
        assertEquals(1, this.mapper.insertWithSelectKeyAfter(after));
        assertNotNull(after.getId());
        assertEquals("AttrSelectKeyAfter", this.mapper.selectByIdPrepared(after.getId()).getName());

        UserInfo full = user(null, "AttrSelectKeyFull", 36, "select-full@nxn.test");
        assertEquals(1, this.mapper.insertWithSelectKeyFullAttrs(full));
        assertNotNull(full.getId());
        assertEquals("AttrSelectKeyFull", this.mapper.selectByIdPrepared(full.getId()).getName());
    }
}
