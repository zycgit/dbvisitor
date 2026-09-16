/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class LambdaEmptyTextCase extends AbstractNxnContractTest {
    protected int baseId() {
        return 710000;
    }

    // 能力归属：构造器 API / 条件构造器。
    @Test
    @Capability(value = CapabilityId.LAMBDA_EMPTY_STRING_VS_NULL, column = "builder/condition-builders/predicates")
    public void lambdaQuery_shouldDistinguishEmptyStringAndNullValues() throws SQLException {
        insert(baseId() + 11, "", 25, "empty-string@nxn.test");
        insert(baseId() + 12, null, 26, "null-name@nxn.test");

        List<UserInfo> emptyStringRows = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, Arrays.asList(baseId() + 11, baseId() + 12))//
                .eq(UserInfo::getName, "")//
                .queryForList();
        List<UserInfo> nullRows = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, Arrays.asList(baseId() + 11, baseId() + 12))//
                .isNull(UserInfo::getName)//
                .queryForList();

        if (profile().supportsFeature(FeatureId.DISTINCT_EMPTY_STRING)) {
            assertEquals(1, emptyStringRows.size());
            assertEquals(Integer.valueOf(baseId() + 11), emptyStringRows.get(0).getId());
            assertEquals(1, nullRows.size());
            assertEquals(Integer.valueOf(baseId() + 12), nullRows.get(0).getId());
        } else {
            // Oracle stores an empty string as NULL; equality is not an IS NULL predicate.
            assertEquals(0, emptyStringRows.size());
            assertEquals(2, nullRows.size());
        }
    }

    protected void insert(int id, String name, Integer age, String email) throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { id, name, age, email, new Date() });
    }
}
