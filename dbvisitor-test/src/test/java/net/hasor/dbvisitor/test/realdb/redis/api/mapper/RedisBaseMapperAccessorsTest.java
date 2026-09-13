/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.util.*;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.*;
import net.hasor.dbvisitor.test.nxn.capability.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class RedisBaseMapperAccessorsTest extends RedisNativeMapperSupport {


    @Before
    public void loadStatements() throws Exception {
        loadXml();
        session.getConfiguration().loadMapper("/mapper/redis/CoverageMapper.xml");
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_NATIVE_ACCESSORS)
    public void accessors_shouldExposeEntitySessionAndWorkingJdbc() throws Exception {
        BaseMapper<Entry> base = session.createBaseMapper(Entry.class);
        assertEquals(Entry.class, base.entityType());
        assertSame(session, base.session());
        assertNotNull(base.jdbc());
        String key = key("accessor");
        assertEquals(1, base.jdbc().executeUpdate("SET ? ?", new Object[] { key, "value" }));
        assertEquals(Arrays.asList("value"), base.queryStatement("redis.Native.get", params(key, null)));
        assertEquals("value", base.session().jdbc().queryForString("GET ?", key));
    }
}
