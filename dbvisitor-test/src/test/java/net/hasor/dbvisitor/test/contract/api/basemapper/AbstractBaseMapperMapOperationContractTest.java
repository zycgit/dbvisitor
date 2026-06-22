package net.hasor.dbvisitor.test.contract.api.basemapper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public abstract class AbstractBaseMapperMapOperationContractTest extends AbstractNxnContractTest {
    private BaseMapper<UserInfo> mapper;

    @Before
    public void createBaseMapper() throws SQLException {
        this.mapper = newSession().createBaseMapper(UserInfo.class);
    }

    protected int baseId() {
        return 917000;
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_UPDATE_BY_MAP)
    public void updateByMap_shouldIgnoreNullFieldsAndUpdateNonNullValues() {
        this.mapper.insert(user(baseId() + 1, "MapUpdate", 25, "before-map-update@test.com"));

        Map<String, Object> update = mapOf("id", baseId() + 1, "name", "MapUpdated", "age", 30, "email", null);

        int result = this.mapper.updateByMap(update);
        UserInfo loaded = this.mapper.selectById(baseId() + 1);

        assertEquals(1, result);
        assertEquals("MapUpdated", loaded.getName());
        assertEquals(Integer.valueOf(30), loaded.getAge());
        assertEquals("before-map-update@test.com", loaded.getEmail());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_REPLACE_BY_MAP)
    public void replaceByMap_shouldWriteNullFields() {
        this.mapper.insert(user(baseId() + 11, "MapReplace", 28, "before-map-replace@test.com"));

        Map<String, Object> replace = mapOf("id", baseId() + 11, "name", "MapReplaced", "age", null, "email", null);

        int result = this.mapper.replaceByMap(replace);
        UserInfo loaded = this.mapper.selectById(baseId() + 11);

        assertEquals(1, result);
        assertEquals("MapReplaced", loaded.getName());
        assertNull(loaded.getAge());
        assertNull(loaded.getEmail());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_UPSERT_BY_MAP_INSERT)
    public void upsertByMap_shouldInsertWhenPrimaryKeyDoesNotExist() {
        Map<String, Object> upsert = mapOf("id", baseId() + 21, "name", "MapUpsertInsert", "age", 32, "email", "upsert-insert@test.com");

        int result = this.mapper.upsertByMap(upsert);
        UserInfo loaded = this.mapper.selectById(baseId() + 21);

        assertTrue(result >= 1);
        assertNotNull(loaded);
        assertEquals("MapUpsertInsert", loaded.getName());
        assertEquals(Integer.valueOf(32), loaded.getAge());
        assertEquals("upsert-insert@test.com", loaded.getEmail());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_UPSERT_BY_MAP_UPDATE)
    public void upsertByMap_shouldUpdateWhenPrimaryKeyExists() {
        this.mapper.insert(user(baseId() + 31, "MapUpsertBefore", 35, "before-upsert@test.com"));

        Map<String, Object> upsert = mapOf("id", baseId() + 31, "name", "MapUpsertUpdated", "age", 36, "email", "after-upsert@test.com");

        int result = this.mapper.upsertByMap(upsert);
        UserInfo loaded = this.mapper.selectById(baseId() + 31);

        assertTrue(result >= 1);
        assertEquals("MapUpsertUpdated", loaded.getName());
        assertEquals(Integer.valueOf(36), loaded.getAge());
        assertEquals("after-upsert@test.com", loaded.getEmail());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_DELETE_BY_MAP)
    public void deleteByMap_shouldDeleteByPrimaryKey() {
        this.mapper.insert(user(baseId() + 41, "MapDelete", 41, null));

        int result = this.mapper.deleteByMap(mapOf("id", baseId() + 41));

        assertEquals(1, result);
        assertNull(this.mapper.selectById(baseId() + 41));
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_DELETE_LIST_BY_MAP)
    public void deleteListByMap_shouldDeleteEachPrimaryKeyMap() {
        for (int i = 1; i <= 5; i++) {
            this.mapper.insert(user(baseId() + 50 + i, "MapBatchDelete" + i, 50 + i, null));
        }
        List<Map<String, Object>> deleteList = new ArrayList<>();
        deleteList.add(mapOf("id", baseId() + 51));
        deleteList.add(mapOf("id", baseId() + 52));
        deleteList.add(mapOf("id", baseId() + 53));

        int result = this.mapper.deleteListByMap(deleteList);

        assertMutationRows(3, result);
        assertNull(this.mapper.selectById(baseId() + 51));
        assertNull(this.mapper.selectById(baseId() + 52));
        assertNull(this.mapper.selectById(baseId() + 53));
        assertNotNull(this.mapper.selectById(baseId() + 54));
        assertNotNull(this.mapper.selectById(baseId() + 55));
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_MAP_MISSING_PRIMARY_KEY)
    public void mapWithoutPrimaryKey_shouldExposeUpdateAndDeleteSemantics() {
        this.mapper.insert(user(baseId() + 61, "MapNoPkTarget", 61, "no-pk-target@test.com"));

        int updateResult = this.mapper.updateByMap(mapOf("name", "NoPrimaryKey", "age", 62));
        UserInfo loaded = this.mapper.selectById(baseId() + 61);

        assertMutationRows(0, updateResult);
        assertNotNull(loaded);
        assertEquals("MapNoPkTarget", loaded.getName());
        assertEquals(Integer.valueOf(61), loaded.getAge());

        try {
            this.mapper.deleteByMap(mapOf("name", "MapNoPkTarget"));
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains("missing primary key"));
            return;
        }
        throw new AssertionError("deleteByMap without primary key should be rejected");
    }

    private UserInfo user(Integer id, String name, Integer age, String email) {
        UserInfo user = new UserInfo();
        user.setId(id);
        user.setName(name);
        user.setAge(age);
        user.setEmail(email);
        user.setCreateTime(new Date());
        return user;
    }

    private Map<String, Object> mapOf(Object... keyValues) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }
}
