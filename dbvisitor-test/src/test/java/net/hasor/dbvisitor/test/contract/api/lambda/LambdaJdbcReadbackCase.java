/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcCrudCommand;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/** Lambda mutations followed by the JdbcTemplate scalar APIs, independently of basic CRUD. */
@NxnContract
public abstract class LambdaJdbcReadbackCase extends LambdaCrudSupport {
    @Test
    @Capability(CapabilityId.LAMBDA_JDBC_INSERT_READBACK)
    public void lambdaInsert_shouldAllowJdbcStringReadback() throws SQLException {
        int id = baseId() + 1;
        lambdaTemplate.insert(UserInfo.class)
                .applyEntity(user(id, "NXN-Lambda-Entity-Insert", 41, "nxn-lambda-entity-insert@test.com"))
                .executeSumResult();

        assertEquals("NXN-Lambda-Entity-Insert",
                jdbcTemplate.queryForString(command(JdbcCrudCommand.SELECT_NAME), new Object[] { id }));
    }

    @Test
    @Capability(CapabilityId.LAMBDA_JDBC_UPDATE_READBACK)
    public void lambdaUpdate_shouldAllowJdbcIntegerReadback() throws SQLException {
        int id = baseId() + 3;
        insertByJdbc(id, "NXN-Lambda-Entity-Update", 43, "nxn-lambda-entity-update@test.com");
        lambdaTemplate.update(UserInfo.class).eq(UserInfo::getId, id).updateTo(UserInfo::getAge, 44).doUpdate();

        assertEquals(Integer.valueOf(44),
                jdbcTemplate.queryForObject(command(JdbcCrudCommand.SELECT_AGE), new Object[] { id }, Integer.class));
    }

    @Test
    @Capability(CapabilityId.LAMBDA_JDBC_DELETE_READBACK)
    public void lambdaDelete_shouldAllowJdbcCountReadback() throws SQLException {
        int id = baseId() + 4;
        insertByJdbc(id, "NXN-Lambda-Entity-Delete", 45, "nxn-lambda-entity-delete@test.com");
        lambdaTemplate.delete(UserInfo.class).eq(UserInfo::getId, id).doDelete();

        assertEquals(Integer.valueOf(0),
                jdbcTemplate.queryForObject(command(JdbcCrudCommand.COUNT_BY_ID), new Object[] { id }, Integer.class));
    }
}
