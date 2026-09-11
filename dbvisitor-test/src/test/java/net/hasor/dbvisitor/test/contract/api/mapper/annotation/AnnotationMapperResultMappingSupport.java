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
import java.util.Map;

import org.junit.Before;

import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.ResultMappingMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

public abstract class AnnotationMapperResultMappingSupport extends AbstractNxnContractTest {
    protected static final String PATTERN = "AnnoResult%";

    protected ResultMappingMapper mapper;

    @Before
    public void createAnnotationMapper() throws Exception {
        Configuration configuration = newConfiguration();
        Session session = configuration.newSession(dataSource);
        this.mapper = session.createMapper(ResultMappingMapper.class);
    }

    @Override
    protected void initData() throws SQLException {
        for (int i = 1; i <= 10; i++) {
            UserInfo user = new UserInfo();
            user.setId(baseId() + i);
            user.setName("AnnoResult" + i);
            user.setAge(20 + i);
            user.setEmail("anno-result" + i + "@nxn.test");
            user.setCreateTime(new Date());
            jdbcTemplate.executeUpdate(//
                    "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                    new Object[] { user.getId(), user.getName(), user.getAge(), user.getEmail(), user.getCreateTime() });
        }
    }

    protected int baseId() {
        return 951000;
    }

    protected Object value(Map<String, Object> row, String key) {
        if (row.containsKey(key)) {
            return row.get(key);
        }
        String lower = key.toLowerCase();
        if (row.containsKey(lower)) {
            return row.get(lower);
        }
        return row.get(key.toUpperCase());
    }

    protected Number number(Map<String, Object> row, String key) {
        return (Number) value(row, key);
    }
}
