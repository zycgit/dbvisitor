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
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class XmlMapperGeneratedKeysCase extends XmlMapperKeyGenerationSupport {
    @Test
    @Capability(CapabilityId.MAPPER_XML_KEYGEN_GENERATED_KEYS)
    public void xmlKeygen_shouldPopulateGeneratedKeyProperty() throws Exception {
        requiresNxnFeature(FeatureId.GENERATED_KEYS_NUMERIC);
        Map<String, Object> params = keygenParams("XmlKeyGenUser1", 25, "xml-keygen1@nxn.test");

        Object result = this.session.executeStatement("xmltest.KeyGenerationMapper.insertWithGeneratedKeys", params);

        assertEquals(1, ((Number) result).intValue());
        Number generatedId = (Number) params.get("id");
        assertNotNull(generatedId);
        assertTrue(generatedId.longValue() > 0);
        List<UserInfo> list = this.session.queryStatement("xmltest.KeyGenerationMapper.selectById", mapOf("id", generatedId));
        assertEquals(1, list.size());
        assertEquals("XmlKeyGenUser1", list.get(0).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_KEYGEN_DISTINCT_GENERATED_KEYS)
    public void xmlKeygen_shouldGenerateDistinctKeysForMultipleInserts() throws Exception {
        requiresNxnFeature(FeatureId.GENERATED_KEYS_NUMERIC);
        long previousId = 0;
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> params = keygenParams("XmlKeyGenMulti" + i, 30 + i, "xml-keygen-multi" + i + "@nxn.test");

            assertEquals(1, ((Number) this.session.executeStatement("xmltest.KeyGenerationMapper.insertWithGeneratedKeys", params)).intValue());

            Number generatedId = (Number) params.get("id");
            assertNotNull(generatedId);
            assertTrue(generatedId.longValue() > previousId);
            previousId = generatedId.longValue();
        }
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_KEYGEN_KEY_COLUMN)
    public void xmlKeygen_shouldPopulateGeneratedKeyWithExplicitKeyColumnWhenSupported() throws Exception {
        requiresNxnFeature(FeatureId.GENERATED_KEY_COLUMN);
        Map<String, Object> params = keygenParams("XmlKeyGenColumn", 35, "xml-key-column@nxn.test");

        assertEquals(1, ((Number) this.session.executeStatement("xmltest.KeyGenerationMapper.insertWithKeyColumn", params)).intValue());

        Number generatedId = (Number) params.get("id");
        assertNotNull(generatedId);
        assertTrue(generatedId.longValue() > 0);
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_KEYGEN_RESULT_SET_SOURCE)
    public void xmlKeygen_shouldPopulateGeneratedKeyFromCurrentResultSetWhenSupported() throws Exception {
        requiresNxnFeature(FeatureId.GENERATED_KEY_RESULT_SET);
        Map<String, Object> params = keygenParams("XmlKeyGenResultSet", 36, "xml-key-result-set@nxn.test");

        assertEquals(1, ((Number) this.session.executeStatement("xmltest.KeyGenerationMapper.insertWithGeneratedKeyResultSet", params)).intValue());

        Number generatedId = (Number) params.get("id");
        assertNotNull(generatedId);
        assertTrue(generatedId.longValue() > 0);
        List<UserInfo> list = this.session.queryStatement("xmltest.KeyGenerationMapper.selectById", mapOf("id", generatedId));
        assertEquals(1, list.size());
        assertEquals("XmlKeyGenResultSet", list.get(0).getName());
    }
}
