/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.mapping;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

public abstract class AnnotationMappingPolicySupport extends AbstractNxnContractTest {
    protected int baseId() {
        return 920000;
    }

    protected void insertRaw(int id, String name, Integer age, String email) throws SQLException {
        deleteRaw(id);
        UserInfo user = new UserInfo();
        user.setId(id);
        user.setName(name);
        user.setAge(age);
        user.setEmail(email);
        user.setCreateTime(new Date());
        this.lambdaTemplate.insert(UserInfo.class).applyEntity(user).executeSumResult();
    }

    protected UserInfo queryRaw(int id) throws SQLException {
        return this.lambdaTemplate.query(UserInfo.class).eq(UserInfo::getId, id).queryForObject();
    }

    protected void deleteRaw(int id) throws SQLException {
        jdbcTemplate.executeUpdate("DELETE FROM user_info WHERE id = ?", new Object[] { id });
    }

    protected void updateRawEmail(int id, String email) throws SQLException {
        jdbcTemplate.executeUpdate("UPDATE user_info SET email = ? WHERE id = ?", new Object[] { email, id });
    }

    protected Object value(Map<String, Object> row, String key) {
        if (row.containsKey(key)) {
            return row.get(key);
        }
        if (row.containsKey(key.toUpperCase())) {
            return row.get(key.toUpperCase());
        }
        return row.get(key.toLowerCase());
    }
}
