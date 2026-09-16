/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.xml;

import java.util.Map;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class XmlMapperSelectKeyCase extends XmlMapperKeyGenerationSupport {
    // 能力归属：Mapper 文件 / 主键策略。
    @Test
    @Capability(value = CapabilityId.MAPPER_XML_KEYGEN_SELECT_KEY_BEFORE, column = "mapper-files/statements/key-strategies")
    public void xmlKeygen_shouldRunSelectKeyBeforeWhenFixtureSequenceIsSupported() throws Exception {
        Map<String, Object> params = keygenParams("XmlKeyGenBefore", 40, "xml-key-before@nxn.test");

        assertEquals(1, ((Number) this.session.executeStatement("xmltest.KeyGenerationMapper.insertWithSelectKeyBefore", params)).intValue());

        Object generatedId = params.get("id");
        assertNotNull(generatedId);
        assertEquals("XmlKeyGenBefore", readKeyName(generatedId));
    }

    // 能力归属：Mapper 文件 / 主键策略。
    @Test
    @Capability(value = CapabilityId.MAPPER_XML_KEYGEN_SELECT_KEY_AFTER, column = "mapper-files/statements/key-strategies")
    public void xmlKeygen_shouldRunSelectKeyAfterWhenFixtureSequenceIsSupported() throws Exception {
        Map<String, Object> params = keygenParams("XmlKeyGenAfter", 41, "xml-key-after@nxn.test");

        assertEquals(1, ((Number) this.session.executeStatement("xmltest.KeyGenerationMapper.insertWithSelectKeyAfter", params)).intValue());

        Object generatedId = params.get("id");
        assertNotNull(generatedId);
        assertEquals("XmlKeyGenAfter", readKeyName(generatedId));
    }
}
