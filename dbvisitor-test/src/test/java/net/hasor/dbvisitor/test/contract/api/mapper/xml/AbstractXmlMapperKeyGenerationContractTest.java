package net.hasor.dbvisitor.test.contract.api.mapper.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class AbstractXmlMapperKeyGenerationContractTest extends AbstractNxnContractTest {
    private Session session;

    @Before
    public void createXmlMapperSession() throws Exception {
        Configuration config = newConfiguration();
        config.loadMapper("/mapper/XmlKeyGenerationMapper.xml");
        this.session = config.newSession(dataSource);
    }

    protected int baseId() {
        return 958000;
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_KEYGEN_GENERATED_KEYS)
    public void xmlKeygen_shouldPopulateGeneratedKeyProperty() throws Exception {
        requiresNxnFeature(FeatureId.GENERATED_KEYS_NUMERIC);
        Map<String, Object> params = keygenParams("XmlKeyGenUser1", 25, "xml-keygen1@nxn.test");

        Object result = this.session.executeStatement("xmltest.KeyGenerationMapper.insertWithGeneratedKeys", params);

        assertEquals(1, ((Number) result).intValue());
        Number generatedId = (Number) params.get("id");
        assertNotNull(generatedId);
        assertTrue(generatedId.intValue() > 0);
        List<UserInfo> list = this.session.queryStatement("xmltest.KeyGenerationMapper.selectById", mapOf("id", generatedId));
        assertEquals(1, list.size());
        assertEquals("XmlKeyGenUser1", list.get(0).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_KEYGEN_GENERATED_KEYS)
    public void xmlKeygen_shouldGenerateDistinctKeysForMultipleInserts() throws Exception {
        requiresNxnFeature(FeatureId.GENERATED_KEYS_NUMERIC);
        int previousId = 0;
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> params = keygenParams("XmlKeyGenMulti" + i, 30 + i, "xml-keygen-multi" + i + "@nxn.test");

            assertEquals(1, ((Number) this.session.executeStatement("xmltest.KeyGenerationMapper.insertWithGeneratedKeys", params)).intValue());

            Number generatedId = (Number) params.get("id");
            assertNotNull(generatedId);
            assertTrue(generatedId.intValue() > previousId);
            previousId = generatedId.intValue();
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
        assertTrue(generatedId.intValue() > 0);
    }

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

    @Test
    @Capability(CapabilityId.MAPPER_XML_KEYGEN_EXPLICIT_ID)
    public void xmlKeygen_shouldInsertExplicitIdWithoutGeneration() throws Exception {
        Map<String, Object> params = keygenParams("XmlKeyGenExplicit", 50, "xml-key-explicit@nxn.test");
        params.put("id", baseId() + 99);

        assertEquals(1, ((Number) this.session.executeStatement("xmltest.KeyGenerationMapper.insertWithExplicitId", params)).intValue());

        List<UserInfo> list = this.session.queryStatement("xmltest.KeyGenerationMapper.selectById", mapOf("id", baseId() + 99));
        assertEquals(1, list.size());
        assertEquals("XmlKeyGenExplicit", list.get(0).getName());
    }

    private Map<String, Object> keygenParams(String name, int age, String email) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("age", age);
        params.put("email", email);
        return params;
    }

    private Map<String, Object> mapOf(String key, Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put(key, value);
        return params;
    }
}
