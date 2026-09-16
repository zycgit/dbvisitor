/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.annotation;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.AnnotationAttributesMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import org.junit.Before;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class AnnotationMapperAttributeSupport extends AbstractNxnContractTest {
    protected static final String PATTERN = "AttrNxn%";

    protected AnnotationAttributesMapper mapper;

    @Before
    public void createAnnotationMapper() throws Exception {
        Configuration configuration = newConfiguration();
        Session session = configuration.newSession(dataSource);
        this.mapper = session.createMapper(mapperType());
    }

    protected Class<? extends AnnotationAttributesMapper> mapperType() {
        return AnnotationAttributesMapper.class;
    }

    @Override
    protected void initData() throws SQLException {
        for (int i = 1; i <= 10; i++) {
            jdbcTemplate.executeUpdate(//
                    "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, @{macro, currentTimestamp})", //
                    new Object[] { baseId() + i, "AttrNxn" + i, 20 + i, "attr-nxn" + i + "@nxn.test" });
        }
    }

    protected int baseId() {
        return 959000;
    }

    protected void assertAtLeastSeedRows(List<UserInfo> users) {
        assertNotNull(users);
        assertTrue(users.size() >= 10);
    }

    protected UserInfo user(Integer id, String name, Integer age, String email) {
        UserInfo user = new UserInfo();
        user.setId(id);
        user.setName(name);
        user.setAge(age);
        user.setEmail(email);
        user.setCreateTime(new Date());
        return user;
    }

    protected enum KeyWrite {
        GENERATED,
        COLUMN,
        RESULT_SET,
        EXPLICIT,
        BEFORE,
        AFTER,
        OPTIONS
    }

    protected boolean numericGeneratedKeys() {
        return true;
    }

    protected Object keyRecord(Object id, String name, int age, String email) {
        return user((Integer) id, name, age, email);
    }

    protected Object keyValue(Object record) {
        return ((UserInfo) record).getId();
    }

    protected Object explicitKey() {
        return explicitId(101);
    }

    protected int writeKeyRecord(KeyWrite operation, Object record) throws Exception {
        UserInfo user = (UserInfo) record;
        return switch (operation) {
            case GENERATED -> this.mapper.insertWithGeneratedKeyNoKeyColumn(user);
            case COLUMN -> this.mapper.insertWithKeyProperty(user);
            case RESULT_SET -> this.mapper.insertWithGeneratedKeyResultSet(user);
            case EXPLICIT -> this.mapper.insertWithoutGeneratedKey(user);
            case BEFORE -> this.mapper.insertWithSelectKeyBefore(user);
            case AFTER -> this.mapper.insertWithSelectKeyAfter(user);
            case OPTIONS -> this.mapper.insertWithSelectKeyFullAttrs(user);
        };
    }

    protected String readKeyName(Object id) throws Exception {
        return this.mapper.selectByIdPrepared((Integer) id).getName();
    }

    protected void assertGeneratedKey(Object id) {
        assertNotNull(id);
        assertTrue(id instanceof Number);
        assertTrue(((Number) id).longValue() > 0);
    }

    protected int explicitId(int offset) {
        return -baseId() - offset;
    }
}
