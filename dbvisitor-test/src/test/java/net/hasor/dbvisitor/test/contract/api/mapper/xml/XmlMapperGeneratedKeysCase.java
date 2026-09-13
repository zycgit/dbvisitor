/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.xml;

import java.util.HashSet;
import java.util.Set;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotEquals;

@NxnContract
public abstract class XmlMapperGeneratedKeysCase extends XmlMapperKeyGenerationSupport {
    @Test
    @Capability(CapabilityId.MAPPER_XML_KEYGEN_GENERATED_KEYS)
    public void xmlKeygen_shouldPopulateGeneratedKeyProperty() throws Exception {
        if (numericGeneratedKeys()) {
            requiresNxnFeature(FeatureId.GENERATED_KEYS_NUMERIC);
        }
        Map<String, Object> params = keygenParams("XmlKeyGenUser1", 25, "xml-keygen1@nxn.test");

        Object result = this.session.executeStatement("xmltest.KeyGenerationMapper.insertWithGeneratedKeys", params);

        assertEquals(1, ((Number) result).intValue());
        Object generatedId = params.get("id");
        assertGeneratedKey(generatedId);
        assertEquals("XmlKeyGenUser1", readKeyName(generatedId));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_KEYGEN_DISTINCT_GENERATED_KEYS)
    public void xmlKeygen_shouldGenerateDistinctKeysForMultipleInserts() throws Exception {
        if (numericGeneratedKeys()) {
            requiresNxnFeature(FeatureId.GENERATED_KEYS_NUMERIC);
        }
        Object previousId = null;
        Set<Object> keys = new HashSet<>();
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> params = keygenParams("XmlKeyGenMulti" + i, 30 + i, "xml-keygen-multi" + i + "@nxn.test");

            assertEquals(1, ((Number) this.session.executeStatement("xmltest.KeyGenerationMapper.insertWithGeneratedKeys", params)).intValue());

            Object generatedId = params.get("id");
            assertGeneratedKey(generatedId);
            assertTrue(keys.add(generatedId));
            assertKeyProgression(previousId, generatedId);
            assertEquals("XmlKeyGenMulti" + i, readKeyName(generatedId));
            previousId = generatedId;
        }
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_KEYGEN_KEY_COLUMN)
    public void xmlKeygen_shouldPopulateGeneratedKeyWithExplicitKeyColumnWhenSupported() throws Exception {
        requiresNxnFeature(FeatureId.GENERATED_KEY_COLUMN);
        Map<String, Object> params = keygenParams("XmlKeyGenColumn", 35, "xml-key-column@nxn.test");

        assertEquals(1, ((Number) this.session.executeStatement("xmltest.KeyGenerationMapper.insertWithKeyColumn", params)).intValue());

        Object generatedId = params.get("id");
        assertGeneratedKey(generatedId);
        assertEquals("XmlKeyGenColumn", readKeyName(generatedId));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_KEYGEN_RESULT_SET_SOURCE)
    public void xmlKeygen_shouldPopulateGeneratedKeyFromCurrentResultSetWhenSupported() throws Exception {
        requiresNxnFeature(FeatureId.GENERATED_KEY_RESULT_SET);
        Map<String, Object> params = keygenParams("XmlKeyGenResultSet", 36, "xml-key-result-set@nxn.test");

        assertEquals(1, ((Number) this.session.executeStatement("xmltest.KeyGenerationMapper.insertWithGeneratedKeyResultSet", params)).intValue());

        Object generatedId = params.get("id");
        assertGeneratedKey(generatedId);
        assertEquals("XmlKeyGenResultSet", readKeyName(generatedId));

        Map<String, Object> second = keygenParams("XmlKeyGenResultSetSecond", 37, "xml-key-result-set-second@nxn.test");
        assertNull(second.get("id"));
        assertEquals(1, ((Number) this.session.executeStatement("xmltest.KeyGenerationMapper.insertWithGeneratedKeyResultSet", second)).intValue());
        Object secondId = second.get("id");
        assertGeneratedKey(secondId);
        assertNotEquals(generatedId, secondId);
        assertEquals("XmlKeyGenResultSet", readKeyName(generatedId));
        assertEquals("XmlKeyGenResultSetSecond", readKeyName(secondId));
    }
}
