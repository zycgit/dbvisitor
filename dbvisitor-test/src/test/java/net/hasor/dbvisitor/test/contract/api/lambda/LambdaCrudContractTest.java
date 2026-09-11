/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcCrudCommand;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class LambdaCrudContractTest extends LambdaCrudSupport {
    @Test
    @Capability(CapabilityId.LAMBDA_ENTITY_CRUD_INSERT)
    public void lambdaEntityInsert_shouldPersistOneUser() throws SQLException {
        int id = baseId() + 1;
        int rows = lambdaTemplate.insert(UserInfo.class)//
                .applyEntity(user(id, "NXN-Lambda-Entity-Insert", 41, "nxn-lambda-entity-insert@test.com"))//
                .executeSumResult();

        assertEquals(1, rows);
        assertEquals("NXN-Lambda-Entity-Insert", storedValue(JdbcCrudCommand.SELECT_NAME, "name", id));

    }

    @Test
    @Capability(CapabilityId.LAMBDA_ENTITY_CRUD_QUERY)
    public void lambdaEntityQuery_shouldReadUserById() throws SQLException {
        int id = baseId() + 2;
        insertByJdbc(id, "NXN-Lambda-Entity-Query", 42, "nxn-lambda-entity-query@test.com");

        UserInfo loaded = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, id)//
                .queryForObject();

        assertNotNull(loaded);
        assertEquals(Integer.valueOf(id), loaded.getId());
        assertEquals("NXN-Lambda-Entity-Query", loaded.getName());
        assertEquals(Integer.valueOf(42), loaded.getAge());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_ENTITY_CRUD_UPDATE)
    public void lambdaEntityUpdate_shouldChangeUser() throws SQLException {
        int id = baseId() + 3;
        insertByJdbc(id, "NXN-Lambda-Entity-Update", 43, "nxn-lambda-entity-update@test.com");

        int rows = lambdaTemplate.update(UserInfo.class)//
                .eq(UserInfo::getId, id)//
                .updateTo(UserInfo::getAge, 44)//
                .doUpdate();

        assertEquals(1, rows);
        assertEquals("44", storedValue(JdbcCrudCommand.SELECT_AGE, "age", id));
    }

    @Test
    @Capability(CapabilityId.LAMBDA_ENTITY_CRUD_DELETE)
    public void lambdaEntityDelete_shouldRemoveUser() throws SQLException {
        int id = baseId() + 4;
        insertByJdbc(id, "NXN-Lambda-Entity-Delete", 45, "nxn-lambda-entity-delete@test.com");

        int rows = lambdaTemplate.delete(UserInfo.class)//
                .eq(UserInfo::getId, id)//
                .doDelete();

        assertEquals(1, rows);
        assertUserAbsent(id);
    }
}
