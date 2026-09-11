/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.transaction;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;

import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.contract.material.service.CallerTransactionService;
import net.hasor.dbvisitor.test.contract.material.service.UserTransactionService;
import net.hasor.dbvisitor.transaction.Isolation;
import net.hasor.dbvisitor.transaction.Propagation;
import net.hasor.dbvisitor.transaction.TransactionManager;
import net.hasor.dbvisitor.transaction.TransactionStatus;
import net.hasor.dbvisitor.transaction.TransactionTemplate;
import net.hasor.dbvisitor.transaction.TransactionTemplateManager;
import net.hasor.dbvisitor.transaction.support.TransactionHelper;

import static org.junit.Assert.assertEquals;

public abstract class TransactionSupport extends AbstractNxnContractTest {
    protected int baseId() {
        return 910000;
    }

    @Before
    public void requireTransactionFeature() {
        requiresNxnFeature(FeatureId.TRANSACTION);
    }

    @After
    public void resetOracleTransactionState() {
        if (isOracle()) {
            OneApiDataSourceManager.reset();
            dataSource = null;
        }
    }

    protected TransactionManager txManager() {
        return TransactionHelper.txManager(dataSource);
    }

    protected TransactionTemplate txTemplate() {
        return new TransactionTemplateManager(txManager());
    }

    protected void insertUser(int id, String name) throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(id);
        user.setName(name);
        user.setAge(30);
        user.setEmail(name + "@nxn.test");
        user.setCreateTime(new Date());
        lambdaTemplate.insert(UserInfo.class).applyEntity(user).executeSumResult();
    }

    protected long countById(int id) throws SQLException {
        return lambdaTemplate.query(UserInfo.class).eq(UserInfo::getId, id).queryForCount();
    }

    protected UserInfo user(int id, String name) {
        UserInfo user = new UserInfo();
        user.setId(id);
        user.setName(name);
        user.setAge(30);
        user.setEmail(name + "@nxn.test");
        user.setCreateTime(new Date());
        return user;
    }

    protected Session sessionWithStatementMapper() throws Exception {
        Configuration configuration = newConfiguration();
        configuration.loadMapper("/mapper/StatementTestMapper.xml");
        return configuration.newSession(dataSource);
    }

    protected Map<String, Object> userParams(int id, String name, int age) {
        return mapOf("id", id, "name", name, "age", age, "email", name + "@nxn.test");
    }

    protected Map<String, Object> mapOf(Object... pairs) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((String) pairs[i], pairs[i + 1]);
        }
        return map;
    }

    protected void assertIsolationCommit(int id, Isolation isolation, String name) throws SQLException {
        TransactionManager tm = txManager();
        TransactionStatus status = tm.begin(Propagation.REQUIRED, isolation);
        assertEquals(isolation, status.getIsolationLevel());
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, @{macro, currentTimestamp})", new Object[] { id, name, 25 });
        tm.commit(status);
        assertEquals(1, countById(id));
    }

    protected UserTransactionService userProxy() {
        UserTransactionService rawUser = new UserTransactionService(lambdaTemplate);
        return TransactionHelper.support(rawUser, dataSource);
    }

    protected CallerTransactionService callerProxy() {
        CallerTransactionService rawCaller = new CallerTransactionService(userProxy(), lambdaTemplate);
        return TransactionHelper.support(rawCaller, dataSource);
    }
}
