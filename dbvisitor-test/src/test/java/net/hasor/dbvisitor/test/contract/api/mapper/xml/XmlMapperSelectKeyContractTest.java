/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.xml;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class XmlMapperSelectKeyContractTest extends XmlMapperKeyGenerationSupport {
    @Test
    @Capability(CapabilityId.MAPPER_XML_KEYGEN_SELECT_KEY_BEFORE)
    public void xmlKeygen_shouldRunSelectKeyBeforeWhenFixtureSequenceIsSupported() throws Exception {
        requiresNxnFeature(FeatureId.XML_SELECT_KEY_USER_INFO_SEQUENCE);
        Map<String, Object> params = keygenParams("XmlKeyGenBefore", 40, "xml-key-before@nxn.test");

        assertEquals(1, ((Number) this.session.executeStatement("xmltest.KeyGenerationMapper.insertWithSelectKeyBefore", params)).intValue());

        Number generatedId = (Number) params.get("id");
        assertNotNull(generatedId);
        List<UserInfo> list = this.session.queryStatement("xmltest.KeyGenerationMapper.selectById", mapOf("id", generatedId));
        assertEquals(1, list.size());
        assertEquals("XmlKeyGenBefore", list.get(0).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_KEYGEN_SELECT_KEY_AFTER)
    public void xmlKeygen_shouldRunSelectKeyAfterWhenFixtureSequenceIsSupported() throws Exception {
        requiresNxnFeature(FeatureId.XML_SELECT_KEY_USER_INFO_SEQUENCE);
        Map<String, Object> params = keygenParams("XmlKeyGenAfter", 41, "xml-key-after@nxn.test");

        assertEquals(1, ((Number) this.session.executeStatement("xmltest.KeyGenerationMapper.insertWithSelectKeyAfter", params)).intValue());

        Number generatedId = (Number) params.get("id");
        assertNotNull(generatedId);
        List<UserInfo> list = this.session.queryStatement("xmltest.KeyGenerationMapper.selectById", mapOf("id", generatedId));
        assertEquals(1, list.size());
        assertEquals("XmlKeyGenAfter", list.get(0).getName());
    }
}
