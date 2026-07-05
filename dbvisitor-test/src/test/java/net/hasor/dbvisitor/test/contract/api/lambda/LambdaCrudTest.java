package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class LambdaCrudTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 620000;
    }

    @Test
    @Capability(CapabilityId.LAMBDA_ENTITY_CRUD_INSERT)
    public void lambdaEntityInsertPersistOneUser() throws SQLException {
        int id = baseId() + 1;
        int rows = lambdaTemplate.insert(UserInfo.class)//
                .applyEntity(user(id, "NXN-Lambda-Entity-Insert", 41, "nxn-lambda-entity-insert@test.com"))//
                .executeSumResult();

        assertEquals(1, rows);
        assertEquals("NXN-Lambda-Entity-Insert", jdbcTemplate.queryForString("SELECT name FROM user_info WHERE id = ?", new Object[] { id }));

        UserInfo autoIdUser = user(null, "NXN-Lambda-Entity-Auto-Id", 40, "nxn-lambda-entity-auto-id@test.com");
        int autoRows = lambdaTemplate.insert(UserInfo.class)//
                .applyEntity(autoIdUser)//
                .executeSumResult();
        UserInfo loaded = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getName, "NXN-Lambda-Entity-Auto-Id")//
                .queryForObject();

        assertEquals(1, autoRows);
        assertNotNull(loaded);
        assertNotNull(loaded.getId());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_ENTITY_CRUD_BATCH_INSERT)
    public void lambdaEntityBatchInsertPersistEntityList() throws SQLException {
        List<UserInfo> users = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            users.add(user(baseId() + 20 + i, "NXN-Lambda-Entity-Batch-" + i, 20 + i, "nxn-lambda-entity-batch-" + i + "@test.com"));
        }

        int rows = lambdaTemplate.insert(UserInfo.class)//
                .applyEntity(users)//
                .executeSumResult();
        long count = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Lambda-Entity-Batch-%")//
                .queryForCount();

        assertEquals(10, rows);
        assertEquals(10, count);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_ENTITY_CRUD_QUERY)
    public void lambdaEntityQueryUserById() throws SQLException {
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
    public void lambdaEntityUpdateChangeUser() throws SQLException {
        int id = baseId() + 3;
        insertByJdbc(id, "NXN-Lambda-Entity-Update", 43, "nxn-lambda-entity-update@test.com");

        int rows = lambdaTemplate.update(UserInfo.class)//
                .eq(UserInfo::getId, id)//
                .updateTo(UserInfo::getAge, 44)//
                .doUpdate();

        assertEquals(1, rows);
        assertEquals(Integer.valueOf(44), jdbcTemplate.queryForObject("SELECT age FROM user_info WHERE id = ?", new Object[] { id }, Integer.class));
    }

    @Test
    @Capability(CapabilityId.LAMBDA_ENTITY_CRUD_DELETE)
    public void lambdaEntityDeleteRemoveUser() throws SQLException {
        int id = baseId() + 4;
        insertByJdbc(id, "NXN-Lambda-Entity-Delete", 45, "nxn-lambda-entity-delete@test.com");

        int rows = lambdaTemplate.delete(UserInfo.class)//
                .eq(UserInfo::getId, id)//
                .doDelete();

        assertEquals(1, rows);
        assertEquals(Integer.valueOf(0), jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_info WHERE id = ?", new Object[] { id }, Integer.class));
    }

    private void insertByJdbc(int id, String name, int age, String email) throws SQLException {
        jdbcTemplate.executeUpdate(//
                "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { id, name, age, email, new Date() });
    }

    private UserInfo user(Integer id, String name, int age, String email) {
        UserInfo user = new UserInfo();
        user.setId(id);
        user.setName(name);
        user.setAge(age);
        user.setEmail(email);
        user.setCreateTime(new Date());
        return user;
    }
}
