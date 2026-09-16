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
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class XmlMapperExplicitKeyCase extends XmlMapperKeyGenerationSupport {
    // 能力归属：Mapper 文件 / 主键策略。
    @Test
    @Capability(value = CapabilityId.MAPPER_XML_KEYGEN_EXPLICIT_ID, column = "mapper-files/statements/key-strategies")
    public void xmlKeygen_shouldInsertExplicitIdWithoutGeneration() throws Exception {
        Map<String, Object> params = keygenParams("XmlKeyGenExplicit", 50, "xml-key-explicit@nxn.test");
        params.put("id", baseId() + 99);

        assertEquals(1, ((Number) this.session.executeStatement("xmltest.KeyGenerationMapper.insertWithExplicitId", params)).intValue());

        List<UserInfo> list = this.session.queryStatement("xmltest.KeyGenerationMapper.selectById", mapOf("id", baseId() + 99));
        assertEquals(1, list.size());
        assertEquals("XmlKeyGenExplicit", list.get(0).getName());
    }
}
