/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.session;

import java.math.BigDecimal;
import java.util.List;
import javax.sql.DataSource;

import org.junit.Test;

import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.contract.material.model.UserOrder;
import net.hasor.dbvisitor.test.contract.material.model.UserRole;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

@NxnContract
public abstract class SessionCoreCase extends AbstractNxnContractTest {
    protected DataSource sessionDataSource() {
        return dataSource;
    }

    protected int baseId() {
        return 609000;
    }

    @Test
    @Capability(CapabilityId.SESSION_LIFECYCLE)
    public void session_shouldBindConfigurationAndCloseIdempotently() throws Exception {
        Configuration configuration = newConfiguration();
        Session session = configuration.newSession(sessionDataSource());

        assertNotNull(session);
        assertSame(configuration, session.getConfiguration());

        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);
        mapper.insert(user(baseId() + 1, "SessionClose", 25));
        assertEquals("SessionClose", mapper.selectById(baseId() + 1).getName());

        session.close();
        session.close();
    }

    @Test
    @Capability(CapabilityId.SESSION_COMPONENT_JDBC)
    public void sessionJdbc_shouldExposeUsableJdbcTemplate() throws Exception {
        Session session = newConfiguration().newSession(sessionDataSource());
        JdbcTemplate jdbc = session.jdbc();

        assertNotNull(jdbc);
        assertEquals(1, jdbc.executeUpdate("INSERT INTO user_info (id, name, age) VALUES (?, ?, ?)", new Object[] { baseId() + 10, "SessionJdbc", 30 }));
        assertEquals(Integer.valueOf(1), jdbc.queryForObject("SELECT COUNT(*) FROM user_info WHERE id = ?", new Object[] { baseId() + 10 }, Integer.class));
    }

    @Test
    @Capability(CapabilityId.SESSION_COMPONENT_LAMBDA)
    public void sessionLambda_shouldExposeUsableLambdaTemplate() throws Exception {
        Session session = newConfiguration().newSession(sessionDataSource());
        LambdaTemplate lambda = session.lambda();

        assertNotNull(lambda);
        assertEquals(1, lambda.insert(UserInfo.class).applyEntity(user(baseId() + 20, "SessionLambda", 28)).executeSumResult());

        List<UserInfo> users = lambda.query(UserInfo.class).eq(UserInfo::getId, baseId() + 20).queryForList();
        assertEquals(1, users.size());
        assertEquals("SessionLambda", users.get(0).getName());
    }

    @Test
    @Capability(CapabilityId.SESSION_BASEMAPPER_CRUD)
    public void sessionBaseMapper_shouldRunCrudThroughCreatedMapper() throws Exception {
        BaseMapper<UserInfo> mapper = newConfiguration().newSession(sessionDataSource()).createBaseMapper(UserInfo.class);
        int id = baseId() + 30;

        assertEquals(1, mapper.insert(user(id, "SessionCrud", 25)));
        UserInfo loaded = mapper.selectById(id);
        assertNotNull(loaded);
        assertEquals("SessionCrud", loaded.getName());

        loaded.setAge(31);
        assertEquals(1, mapper.update(loaded));
        assertEquals(Integer.valueOf(31), mapper.selectById(id).getAge());

        assertEquals(1, mapper.deleteById(id));
        assertNull(mapper.selectById(id));
    }

    @Test
    @Capability(CapabilityId.SESSION_BASEMAPPER_MULTI_ENTITY)
    public void sessionBaseMapper_shouldSupportMultipleEntityTypesInOneSession() throws Exception {
        Session session = newConfiguration().newSession(sessionDataSource());
        BaseMapper<UserInfo> userMapper = session.createBaseMapper(UserInfo.class);
        BaseMapper<UserOrder> orderMapper = session.createBaseMapper(UserOrder.class);
        int id = baseId() + 40;

        assertEquals(1, userMapper.insert(user(id, "SessionMultiEntity", 30)));
        assertEquals(1, orderMapper.insert(order(id, id, "ORD-SESSION-40")));

        assertNotNull(userMapper.selectById(id));
        assertNotNull(orderMapper.selectById(id));
    }

    @Test
    @Capability(CapabilityId.SESSION_BASEMAPPER_COMPOSITE_KEY)
    public void sessionBaseMapper_shouldSupportCompositeKeyEntity() throws Exception {
        requiresNxnFeature(FeatureId.COMPOSITE_PRIMARY_KEY);
        BaseMapper<UserRole> mapper = newConfiguration().newSession(sessionDataSource()).createBaseMapper(UserRole.class);
        UserRole role = new UserRole(baseId() + 50, 1, "SessionRole");

        assertEquals(1, mapper.insert(role));

        UserRole ref = new UserRole();
        ref.setUserId(baseId() + 50);
        ref.setRoleId(1);
        UserRole loaded = mapper.loadBy(ref);
        assertNotNull(loaded);
        assertEquals("SessionRole", loaded.getRoleName());
    }

    @Test
    @Capability(CapabilityId.SESSION_BASEMAPPER_NAMESPACE)
    public void sessionBaseMapper_shouldRegisterEntityInCustomNamespace() throws Exception {
        Configuration configuration = newConfiguration();
        Session session = configuration.newSession(sessionDataSource());

        assertNotNull(session.createBaseMapper(UserRole.class, "nxn.session.role"));
        assertNotNull(configuration.findBySpace("nxn.session.role", UserRole.class));
        assertNull(configuration.findByEntity(UserRole.class));
    }

    @Test
    @Capability(CapabilityId.SESSION_BASEMAPPER_MULTI_INSTANCE)
    public void sessionBaseMapper_shouldCreateDistinctMappersAgainstSameTable() throws Exception {
        Session session = newConfiguration().newSession(sessionDataSource());
        BaseMapper<UserInfo> first = session.createBaseMapper(UserInfo.class);
        BaseMapper<UserInfo> second = session.createBaseMapper(UserInfo.class);
        int id = baseId() + 60;

        assertNotSame(first, second);
        assertEquals(1, first.insert(user(id, "SessionMapperPair", 35)));
        assertEquals("SessionMapperPair", second.selectById(id).getName());
    }

    @Test
    @Capability(CapabilityId.SESSION_CONFIGURATION_ACCESSORS)
    public void configuration_shouldExposeRegistriesOptionsAndClassLoading() throws Exception {
        Options options = Options.of();
        Configuration configuration = newConfiguration(options);

        assertSame(options, configuration.options());
        assertNotNull(configuration.getTypeRegistry());
        assertNotNull(configuration.getMacroRegistry());
        assertNotNull(configuration.getRuleRegistry());
        assertNotNull(configuration.getMapperRegistry());
        assertNotNull(configuration.getMappingRegistry());
        assertNotNull(configuration.getClassLoader());

        configuration.loadEntityToSpace(UserInfo.class);
        assertNotNull(configuration.findByEntity(UserInfo.class));
        assertEquals(UserInfo.class, configuration.loadClass("net.hasor.dbvisitor.test.contract.material.model.UserInfo"));

        try {
            configuration.loadClass("com.nonexistent.SomeClass");
        } catch (ClassNotFoundException e) {
            return;
        }
        throw new AssertionError("Expected missing class lookup to throw ClassNotFoundException.");
    }

    @Test
    @Capability(CapabilityId.SESSION_MULTI_SESSION)
    public void configuration_shouldCreateDistinctSessionsSharingOneConfiguration() throws Exception {
        Configuration configuration = newConfiguration();
        Session firstSession = configuration.newSession(sessionDataSource());
        Session secondSession = configuration.newSession(sessionDataSource());

        assertNotSame(firstSession, secondSession);
        assertSame(firstSession.getConfiguration(), secondSession.getConfiguration());

        BaseMapper<UserInfo> firstMapper = firstSession.createBaseMapper(UserInfo.class);
        BaseMapper<UserInfo> secondMapper = secondSession.createBaseMapper(UserInfo.class);
        int id = baseId() + 80;

        assertEquals(1, firstMapper.insert(user(id, "SessionShared", 30)));
        assertEquals("SessionShared", secondMapper.selectById(id).getName());
    }

    private UserInfo user(Integer id, String name, Integer age) {
        UserInfo user = new UserInfo();
        user.setId(id);
        user.setName(name);
        user.setAge(age);
        user.setEmail(name.toLowerCase() + "@nxn.test");
        return user;
    }

    private UserOrder order(Integer id, Integer userId, String orderNo) {
        UserOrder order = new UserOrder();
        order.setId(id);
        order.setUserId(userId);
        order.setOrderNo(orderNo);
        order.setAmount(new BigDecimal("99.99"));
        return order;
    }
}
