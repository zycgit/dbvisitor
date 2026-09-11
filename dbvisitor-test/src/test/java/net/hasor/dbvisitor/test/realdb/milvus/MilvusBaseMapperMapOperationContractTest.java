/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.mapper.basemapper.BaseMapperMapOperationContractTest;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusBaseMapperMapOperationContractTest extends BaseMapperMapOperationContractTest {
    private final MilvusUserInfoFixture fixture = new MilvusUserInfoFixture();
    private Session session;

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = new JdbcTemplate(this.fixture.open());
        this.lambdaTemplate = new LambdaTemplate(this.jdbcTemplate);
    }

    @Override
    protected Session newSession() throws SQLException {
        // The inherited @Before creates its mapper before the subclass setup method.
        this.session = newConfiguration().newSession(this.fixture.open());
        return this.session;
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_MAP_PARTIAL_REPLACE)
    public void missingPropertiesShouldDistinguishPartialUpdateFromReplacement() {
        BaseMapper<UserInfo> mapper = this.session.createBaseMapper(UserInfo.class);
        UserInfo initial = new UserInfo();
        initial.setId(1);
        initial.setName("before");
        initial.setAge(25);
        initial.setEmail("keep@example.test");
        initial.setCreateTime(new Date());
        assertEquals(1, mapper.insert(initial));

        assertEquals(1, mapper.updateByMap(Map.of("id", 1, "name", "partial")));
        UserInfo partial = mapper.selectById(1);
        assertEquals(Integer.valueOf(25), partial.getAge());
        assertEquals("keep@example.test", partial.getEmail());
        assertEquals(initial.getCreateTime(), partial.getCreateTime());

        assertEquals(1, mapper.replaceByMap(Map.of("id", 1, "name", "replacement")));
        UserInfo replacement = mapper.selectById(1);
        assertEquals("replacement", replacement.getName());
        assertNull(replacement.getAge());
        assertNull(replacement.getEmail());
        assertNull(replacement.getCreateTime());

        assertEquals(1, mapper.update(initial));
        assertTrue(mapper.upsertByMap(Map.of("id", 1, "name", "upsert replacement")) >= 1);
        UserInfo upserted = mapper.selectById(1);
        assertEquals("upsert replacement", upserted.getName());
        assertNull(upserted.getAge());
        assertNull(upserted.getEmail());
        assertNull(upserted.getCreateTime());
    }

    @After
    public void cleanupFixture() throws SQLException, IOException {
        try {
            // Drop the isolated schema before Session closes the shared fixture connection.
            this.fixture.close();
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
    }
}
