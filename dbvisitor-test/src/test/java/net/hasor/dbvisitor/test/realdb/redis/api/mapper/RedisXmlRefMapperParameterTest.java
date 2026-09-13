/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.util.*;
import net.hasor.dbvisitor.test.nxn.capability.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class RedisXmlRefMapperParameterTest extends RedisNativeMapperSupport {
    private RedisExtendedMapper extended() throws Exception {
        return session.createMapper(RedisExtendedMapper.class);
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_REF_PARAMETER)
    public void parameters() throws Exception {
        RedisExtendedMapper m = extended();
        String k = key("param");
        m.put(k, "v");
        Generated bean = new Generated();
        bean.setKey(k);
        assertEquals("v", m.bean(bean));
        assertEquals("v", m.map(params(k, null)));
    }
}
