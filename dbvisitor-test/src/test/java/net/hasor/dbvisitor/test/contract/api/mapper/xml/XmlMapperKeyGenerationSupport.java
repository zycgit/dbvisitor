/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.xml;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;

import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

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
