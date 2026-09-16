/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import org.junit.Before;
import static org.junit.Assert.*;

public abstract class XmlMapperKeyGenerationSupport extends AbstractNxnContractTest {
    protected Session session;

    @Before
    public void createXmlMapperSession() throws Exception {
        Configuration config = newConfiguration();
        config.loadMapper(mapperResource());
        this.session = config.newSession(dataSource);
    }

    protected String mapperResource() {
        return "/mapper/XmlKeyGenerationMapper.xml";
    }

    protected boolean numericGeneratedKeys() {
        return true;
    }

    protected void assertGeneratedKey(Object id) {
        assertNotNull(id);
        assertTrue(id instanceof Number);
        assertTrue(((Number) id).longValue() > 0);
    }

    protected void assertKeyProgression(Object previous, Object current) {
        if (previous != null) {
            assertTrue(((Number) current).longValue() > ((Number) previous).longValue());
        }
    }

    protected String readKeyName(Object id) throws Exception {
        List<UserInfo> rows = this.session.queryStatement("xmltest.KeyGenerationMapper.selectById", mapOf("id", id));
        assertEquals(1, rows.size());
        return rows.get(0).getName();
    }

    protected int baseId() {
        return 958000;
    }

    protected Map<String, Object> keygenParams(String name, int age, String email) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("age", age);
        params.put("email", email);
        return params;
    }

    protected Map<String, Object> mapOf(String key, Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put(key, value);
        return params;
    }
}
