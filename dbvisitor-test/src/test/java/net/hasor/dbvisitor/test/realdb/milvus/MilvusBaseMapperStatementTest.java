/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.realdb.milvus.material.user.UserInfoMilvus2;
import net.hasor.dbvisitor.test.realdb.milvus.material.user.UserInfoMilvus3;
import org.junit.Test;
import static org.junit.Assert.*;

/** Named-statement execution through BaseMapper with native Milvus commands. */
public class MilvusBaseMapperStatementTest extends MilvusSessionSqlSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_BASEMAPPER_STATEMENT)
    public void baseMapperStatementMethodsShouldUseRegisteredXml() throws Exception {
        BaseMapper<UserInfoMilvus2> base = this.session.createBaseMapper(UserInfoMilvus2.class);
        assertEquals(1, ((Number) base.executeStatement(NAMESPACE + ".insertUser", parameters("base"))).intValue());
        List<UserInfoMilvus3> rows = base.queryStatement(NAMESPACE + ".queryAll", null);
        assertEquals(1, rows.size());
        assertEquals("base", rows.get(0).getUid());
        assertEquals("name", base.selectById("base").getName());
        assertEquals(1, ((Number) base.executeStatement(NAMESPACE + ".deleteUser", Map.of("uid", "base"))).intValue());
        assertTrue(base.queryStatement(NAMESPACE + ".queryAll", null).isEmpty());
    }
}
