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
import static org.junit.Assert.assertSame;

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
        JdbcTemplate jdbc = configuration.newJdbc(sessionDataSource());
        assertNotNull(jdbc);
        assertEquals(1, jdbc.executeUpdate(jdbcInsertCommand(), jdbcInsertParameters()));
        assertEquals(Integer.valueOf(1), jdbc.queryForObject(jdbcCountCommand(), jdbcCountParameters(), Integer.class));

        LambdaTemplate lambda = configuration.newLambda(sessionDataSource());
        assertNotNull(lambda);
        assertNotNull(lambda.jdbc());

        Session session = configuration.newSession(sessionDataSource());
        assertSame(configuration, session.getConfiguration());
        assertEquals("ConfigJdbc", readFactoryName(session, baseId() + 70));
        assertEquals(1, insertFactoryEntity(session, baseId() + 71, "ConfigSession", 40));
        assertEquals("ConfigSession", readFactoryName(session, baseId() + 71));
        assertEquals("ConfigSession", jdbc.queryForString(jdbcNameCommand(), jdbcNameParameters()));
    }

    protected String jdbcInsertCommand() { return "INSERT INTO user_info (id, name, age) VALUES (?, ?, ?)"; }
    protected Object[] jdbcInsertParameters() { return new Object[] { baseId() + 70, "ConfigJdbc", 28 }; }
    protected String jdbcCountCommand() { return "SELECT COUNT(*) FROM user_info WHERE id = ?"; }
    protected Object[] jdbcCountParameters() { return new Object[] { baseId() + 70 }; }
    protected String jdbcNameCommand() { return "SELECT name FROM user_info WHERE id = ?"; }
    protected Object[] jdbcNameParameters() { return new Object[] { baseId() + 71 }; }

    protected int insertFactoryEntity(Session session, int id, String name, int age) throws Exception {
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);
        return mapper.insert(user(id, name, age));
    }

    protected String readFactoryName(Session session, int id) throws Exception {
        return session.createBaseMapper(UserInfo.class).selectById(id).getName();
    }
}
