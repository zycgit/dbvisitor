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

import org.junit.Before;

import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.AnnotationAttributesMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

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

    protected int explicitId(int offset) {
        return -baseId() - offset;
    }
}
