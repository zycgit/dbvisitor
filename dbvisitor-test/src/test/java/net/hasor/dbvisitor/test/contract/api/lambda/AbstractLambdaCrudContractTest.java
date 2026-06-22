package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class AbstractLambdaCrudContractTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 620000;
    }

    @Test
    @Capability(CapabilityId.LAMBDA_ENTITY_CRUD_INSERT)
    public void lambdaEntityInsert_shouldPersistOneUser() throws SQLException {
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
    public void lambdaEntityBatchInsert_shouldPersistEntityList() throws SQLException {
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
        assertEquals(Integer.valueOf(44), jdbcTemplate.queryForObject("SELECT age FROM user_info WHERE id = ?", new Object[] { id }, Integer.class));
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
        assertEquals(Integer.valueOf(0), jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_info WHERE id = ?", new Object[] { id }, Integer.class));
    }

    @Test
    @Capability(CapabilityId.LAMBDA_FREEDOM_CRUD_INSERT)
    public void lambdaFreedomInsert_shouldPersistOneUser() throws SQLException {
        int id = baseId() + 11;
        int rows = lambdaTemplate.insertFreedom("user_info")//
                .applyMap(userMap(id, "NXN-Lambda-Freedom-Insert", 51, "nxn-lambda-freedom-insert@test.com"))//
                .executeSumResult();

        assertEquals(1, rows);
        assertEquals("NXN-Lambda-Freedom-Insert", jdbcTemplate.queryForString("SELECT name FROM user_info WHERE id = ?", new Object[] { id }));
    }

    @Test
    @Capability(CapabilityId.LAMBDA_FREEDOM_CRUD_QUERY)
    public void lambdaFreedomQuery_shouldReadUserById() throws SQLException {
        int id = baseId() + 12;
        insertByJdbc(id, "NXN-Lambda-Freedom-Query", 52, "nxn-lambda-freedom-query@test.com");

        Map<String, Object> loaded = lambdaTemplate.queryFreedom("user_info")//
                .eq("id", id)//
                .queryForObject();

        assertNotNull(loaded);
        assertEquals("NXN-Lambda-Freedom-Query", value(loaded, "name"));
        assertEquals(52, ((Number) value(loaded, "age")).intValue());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_FREEDOM_CRUD_UPDATE)
    public void lambdaFreedomUpdate_shouldChangeUser() throws SQLException {
        int id = baseId() + 13;
        insertByJdbc(id, "NXN-Lambda-Freedom-Update", 53, "nxn-lambda-freedom-update@test.com");

        Map<String, Object> updates = new HashMap<>();
        updates.put("email", "nxn-lambda-freedom-updated@test.com");

        int rows = lambdaTemplate.updateFreedom("user_info")//
                .eq("id", id)//
                .updateToSample(updates)//
                .doUpdate();

        assertEquals(1, rows);
        assertEquals("nxn-lambda-freedom-updated@test.com", jdbcTemplate.queryForString("SELECT email FROM user_info WHERE id = ?", new Object[] { id }));
    }

    @Test
    @Capability(CapabilityId.LAMBDA_FREEDOM_CRUD_DELETE)
    public void lambdaFreedomDelete_shouldRemoveUser() throws SQLException {
        int id = baseId() + 14;
        insertByJdbc(id, "NXN-Lambda-Freedom-Delete", 55, "nxn-lambda-freedom-delete@test.com");

        int rows = lambdaTemplate.deleteFreedom("user_info")//
                .eq("id", id)//
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

    private Map<String, Object> userMap(int id, String name, int age, String email) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("age", age);
        map.put("email", email);
        map.put("create_time", new Date());
        return map;
    }

    private Object value(Map<String, Object> map, String key) {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        if (map.containsKey(key.toUpperCase())) {
            return map.get(key.toUpperCase());
        }
        return map.get(key.toLowerCase());
    }
}
