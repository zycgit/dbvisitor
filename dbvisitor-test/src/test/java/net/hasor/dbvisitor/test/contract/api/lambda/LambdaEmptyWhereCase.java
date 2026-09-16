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
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class LambdaEmptyWhereCase extends AbstractNxnContractTest {
    protected int baseId() {
        return 690000;
    }

    // 能力归属：构造器 API / 写入操作。
    @Test
    @Capability(value = CapabilityId.LAMBDA_EDGE_EMPTY_WHERE_DELETE_REJECT, column = "builder/inserts-updates-and-deletes/writes")
    public void lambdaDelete_shouldRejectEmptyWhereByDefault() throws SQLException {
        insertUser(baseId() + 41, "NXN-Lambda-Edge-Delete-Reject", 25);

        try {
            lambdaTemplate.delete(UserInfo.class).doDelete();
            fail("Empty-where delete should require allowEmptyWhere().");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("allowEmptyWhere"));
        }

        assertNotNull(lambdaTemplate.query(UserInfo.class).eq(UserInfo::getId, baseId() + 41).queryForObject());
    }

    // 能力归属：构造器 API / 写入操作。
    @Test
    @Capability(value = CapabilityId.LAMBDA_EDGE_EMPTY_WHERE_DELETE_ALLOW, column = "builder/inserts-updates-and-deletes/writes")
    public void lambdaDelete_shouldAllowEmptyWhereWhenExplicitlyEnabled() throws SQLException {
        requiresNxnFeature(FeatureId.EMPTY_WHERE_MUTATION);

        insertUser(baseId() + 51, "NXN-Lambda-Edge-Delete-Allow-1", 25);
        insertUser(baseId() + 52, "NXN-Lambda-Edge-Delete-Allow-2", 26);

        int deleted = lambdaTemplate.delete(UserInfo.class)//
                .allowEmptyWhere()//
                .doDelete();

        assertMutationRows(2, deleted);
        assertEquals(0, lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, Arrays.asList(baseId() + 51, baseId() + 52))//
                .queryForCount());
    }

    // 能力归属：构造器 API / 写入操作。
    @Test
    @Capability(value = CapabilityId.LAMBDA_EDGE_EMPTY_WHERE_UPDATE_REJECT, column = "builder/inserts-updates-and-deletes/writes")
    public void lambdaUpdate_shouldRejectEmptyWhereByDefault() throws SQLException {
        insertUser(baseId() + 61, "NXN-Lambda-Edge-Update-Reject", 25);

        try {
            lambdaTemplate.update(UserInfo.class)//
                    .updateTo(UserInfo::getAge, 99)//
                    .doUpdate();
            fail("Empty-where update should require allowEmptyWhere().");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("allowEmptyWhere"));
        }

        assertEquals(Integer.valueOf(25), lambdaTemplate.query(UserInfo.class).eq(UserInfo::getId, baseId() + 61).queryForObject().getAge());
    }

    // 能力归属：构造器 API / 写入操作。
    @Test
    @Capability(value = CapabilityId.LAMBDA_EDGE_EMPTY_WHERE_UPDATE_ALLOW, column = "builder/inserts-updates-and-deletes/writes")
    public void lambdaUpdate_shouldAllowEmptyWhereWhenExplicitlyEnabled() throws SQLException {
        requiresNxnFeature(FeatureId.EMPTY_WHERE_MUTATION);

        insertUser(baseId() + 71, "NXN-Lambda-Edge-Update-Allow-1", 20);
        insertUser(baseId() + 72, "NXN-Lambda-Edge-Update-Allow-2", 30);

        int updated = lambdaTemplate.update(UserInfo.class)//
                .allowEmptyWhere()//
                .updateTo(UserInfo::getAge, 99)//
                .doUpdate();

        assertMutationRows(2, updated);
        assertEquals(Integer.valueOf(99), lambdaTemplate.query(UserInfo.class).eq(UserInfo::getId, baseId() + 71).queryForObject().getAge());
        assertEquals(Integer.valueOf(99), lambdaTemplate.query(UserInfo.class).eq(UserInfo::getId, baseId() + 72).queryForObject().getAge());
    }

    protected void insertUser(int id, String name, Integer age) throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { id, name, age, name.toLowerCase() + "@nxn.test", new Date() });
    }
}
