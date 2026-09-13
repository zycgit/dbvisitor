/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.session;

import javax.sql.DataSource;

import org.junit.Test;

import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class SessionFactoryCase extends AbstractNxnContractTest {
    protected DataSource sessionDataSource() {
        return dataSource;
    }

    protected int baseId() {
        return 609000;
    }

    private UserInfo user(Integer id, String name, Integer age) {
        UserInfo user = new UserInfo();
        user.setId(id);
        user.setName(name);
        user.setAge(age);
        user.setEmail(name.toLowerCase() + "@nxn.test");
        return user;
    }

    @Test
    @Capability(CapabilityId.SESSION_CONFIGURATION_FACTORY)
    public void configurationFactories_shouldCreateUsableJdbcLambdaAndSession() throws Exception {
        Configuration configuration = newConfiguration(Options.of());
        int jdbcId = baseId() + 70;
        int mapperId = baseId() + 71;

        JdbcTemplate jdbc = configuration.newJdbc(sessionDataSource());
        assertNotNull(jdbc);
        assertEquals(1, jdbc.executeUpdate("INSERT INTO user_info (id, name, age) VALUES (?, ?, ?)", new Object[] { jdbcId, "ConfigJdbc", 28 }));
        assertEquals(Integer.valueOf(1), jdbc.queryForObject("SELECT COUNT(*) FROM user_info WHERE id = ?", new Object[] { jdbcId }, Integer.class));

        LambdaTemplate lambda = configuration.newLambda(sessionDataSource());
        assertNotNull(lambda);
        assertNotNull(lambda.jdbc());

        Session session = configuration.newSession(sessionDataSource());
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);
        assertEquals(1, mapper.insert(user(mapperId, "ConfigSession", 40)));
        assertEquals("ConfigSession", mapper.selectById(mapperId).getName());
    }
}
