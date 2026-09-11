/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.adapter.AdapterContractTest;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.ParameterBindingMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Checks special values and exact collection-query identities through the shared Mapper. */
public class MilvusMapperParameterSqlContractTest extends AdapterContractTest {
    private final MilvusUserInfoFixture fixture = new MilvusUserInfoFixture();
    private Session session;
    private ParameterBindingMapper mapper;

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Before
    public void openMapper() throws Exception {
        this.session = new Configuration().newSession(this.fixture.open());
        this.mapper = this.session.createMapper(ParameterBindingMapper.class);
    }

    @After
    public void closeMapper() throws Exception {
        try {
            this.fixture.close();
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_MAPPER_PARAMETER_BINDING)
    public void sharedMapperShouldBindBeanNamedAndPositionalValues() throws Exception {
        UserInfo first = user(1, "quote'\"\\中文", 20);
        assertEquals(1, this.mapper.insertBean(first));
        UserInfo loaded = this.mapper.selectById(1);
        assertEquals(first.getName(), loaded.getName());
        assertEquals(first.getCreateTime(), loaded.getCreateTime());
        assertNull(loaded.getEmail());
        assertEquals(1, this.mapper.updateByPosition(29, 1));
        assertEquals(Integer.valueOf(29), this.mapper.selectById(1).getAge());
        assertEquals(1, this.mapper.insertWithManyParams(2, "", null, "", first.getCreateTime(), "ignored one", "ignored two"));
        UserInfo second = this.mapper.selectById(2);
        assertEquals("", second.getName());
        assertEquals("", second.getEmail());
        assertNull(second.getAge());
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_MAPPER_PARAMETER_COLLECTIONS)
    public void sharedMapperShouldBindBetweenAndExpandArrayAndListRules() throws Exception {
        for (int i = 1; i <= 3; i++) {
            assertEquals(1, this.mapper.insertBean(user(i, "row-" + i, i * 10)));
        }
        assertEquals(Set.of(1, 2), ids(this.mapper.selectByAgeRange(10, 20)));
        assertEquals(Set.of(1, 3), ids(this.mapper.selectByAgesArray(new Integer[] { 10, 30 })));
        assertEquals(Set.of(2, 3), ids(this.mapper.selectByAgesList(List.of(20, 30))));
        assertTrue(this.mapper.selectByAgesList(List.of(99)).isEmpty());
    }

    private UserInfo user(int id, String name, int age) {
        UserInfo user = new UserInfo();
        user.setId(id);
        user.setName(name);
        user.setAge(age);
        user.setCreateTime(new Date(1_700_000_000_123L));
        return user;
    }

    private Set<Integer> ids(List<UserInfo> users) {
        return users.stream().map(UserInfo::getId).collect(Collectors.toSet());
    }
}
