package net.hasor.dbvisitor.test.oneapi.suite.basemapper;
import java.sql.SQLException;
import java.util.*;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.page.PageResult;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserRole;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 复合主键测试 —— 验证 BaseMapper 在复合主键（user_id + role_id）场景下的全部行为。
 * <p>覆盖方法分类：
 * <ul>
 *   <li>支持复合主键：insert / replace / replaceByMap / update / updateByMap /
 *       upsert / upsertByMap / delete / deleteByMap / deleteList / deleteListByMap /
 *       loadBy / loadListBy / listBySample / countBySample / countAll / pageBySample</li>
 *   <li>明确拒绝复合主键（抛 UnsupportedOperationException）：
 *       selectById / selectByIds / deleteById / deleteByIds</li>
 * </ul>
 */
public class CompositeKeyTest extends AbstractOneApiTest {

    // ==================== insert ====================

    /** insert(T) 插入单条复合主键记录 */
    @Test
    public void testInsertSingle() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        UserRole role = new UserRole(1, 100, "admin");
        int result = mapper.insert(role);
        assertEquals(1, result);

        // loadBy 回查验证
        UserRole ref = new UserRole();
        ref.setUserId(1);
        ref.setRoleId(100);
        UserRole loaded = mapper.loadBy(ref);
        assertNotNull(loaded);
        assertEquals("admin", loaded.getRoleName());
    }

    /** insert(List) 批量插入多条复合主键记录 */
    @Test
    public void testInsertBatch() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        List<UserRole> list = Arrays.asList(//
                new UserRole(1, 100, "admin"),//
                new UserRole(1, 200, "editor"),//
                new UserRole(2, 100, "admin")//
        );
        int result = mapper.insert(list);
        assertEquals(3, result);
        assertEquals(3, mapper.countAll());
    }

    /** insert(T) 同一复合主键重复插入应抛异常 */
    @Test
    public void testInsertDuplicateCompositeKey() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        mapper.insert(new UserRole(1, 100, "admin"));
        try {
            mapper.insert(new UserRole(1, 100, "duplicate")); // 主键冲突
            fail("Should throw exception for duplicate composite key");
        } catch (Exception e) {
            String msg = e.getMessage().toLowerCase();
            assertTrue("Expected constraint violation but got: " + e.getMessage(), msg.contains("duplicate") || msg.contains("unique") || msg.contains("constraint") || msg.contains("primary"));
        }
    }

    /** insert(T) 相同 userId 不同 roleId 应成功（不冲突） */
    @Test
    public void testInsertSameUserDifferentRole() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        mapper.insert(new UserRole(1, 100, "admin"));
        mapper.insert(new UserRole(1, 200, "editor"));
        assertEquals(2, mapper.countAll());
    }

    // ==================== replace ====================

    /** replace(T) 按复合主键全量更新（null 字段也会被 set） */
    @Test
    public void testReplace() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        mapper.insert(new UserRole(1, 100, "admin"));

        UserRole updated = new UserRole(1, 100, null); // roleName 设为 null
        int result = mapper.replace(updated);
        assertEquals(1, result);

        UserRole loaded = mapper.loadBy(updated);
        assertNotNull(loaded);
        assertNull(loaded.getRoleName()); // null 被覆盖
    }

    /** replaceByMap 按复合主键使用 Map 全量更新 */
    @Test
    public void testReplaceByMap() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        mapper.insert(new UserRole(1, 100, "admin"));

        Map<String, Object> map = new HashMap<>();
        map.put("userId", 1);
        map.put("roleId", 100);
        map.put("roleName", "superadmin");
        int result = mapper.replaceByMap(map);
        assertEquals(1, result);

        UserRole ref = new UserRole();
        ref.setUserId(1);
        ref.setRoleId(100);
        UserRole loaded = mapper.loadBy(ref);
        assertEquals("superadmin", loaded.getRoleName());
    }

    // ==================== update ====================

    /** update(T) 按复合主键局部更新（null 字段不更新） */
    @Test
    public void testUpdate() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        mapper.insert(new UserRole(1, 100, "admin"));

        UserRole sample = new UserRole();
        sample.setUserId(1);
        sample.setRoleId(100);
        sample.setRoleName("superadmin");
        // createTime 为 null，不会被更新
        int result = mapper.update(sample);
        assertEquals(1, result);

        UserRole loaded = mapper.loadBy(sample);
        assertEquals("superadmin", loaded.getRoleName());
    }

    /** updateByMap 按复合主键使用 Map 局部更新（null 值被忽略） */
    @Test
    public void testUpdateByMap() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        mapper.insert(new UserRole(1, 100, "admin"));

        Map<String, Object> map = new HashMap<>();
        map.put("userId", 1);
        map.put("roleId", 100);
        map.put("roleName", "moderator");
        int result = mapper.updateByMap(map);
        assertEquals(1, result);

        UserRole ref = new UserRole();
        ref.setUserId(1);
        ref.setRoleId(100);
        UserRole loaded = mapper.loadBy(ref);
        assertEquals("moderator", loaded.getRoleName());
    }

    /** update(T) 复合主键不匹配时影响0行 */
    @Test
    public void testUpdateNonExistent() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        mapper.insert(new UserRole(1, 100, "admin"));

        UserRole sample = new UserRole();
        sample.setUserId(999);
        sample.setRoleId(999);
        sample.setRoleName("ghost");
        int result = mapper.update(sample);
        assertEquals(0, result);
    }

    /** replace 批量更新多条复合主键记录（逐条 replace） */
    @Test
    public void testBatchReplace() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        mapper.insert(new UserRole(1, 100, "admin"));
        mapper.insert(new UserRole(1, 200, "editor"));
        mapper.insert(new UserRole(2, 100, "admin"));

        // 批量 replace：逐条更新
        List<UserRole> updates = Arrays.asList(
                new UserRole(1, 100, "superadmin"),
                new UserRole(1, 200, "chief-editor"),
                new UserRole(2, 100, "moderator")
        );
        int total = 0;
        for (UserRole u : updates) {
            total += mapper.replace(u);
        }
        assertEquals(3, total);

        // 验证每条都被更新
        UserRole ref1 = new UserRole();
        ref1.setUserId(1);
        ref1.setRoleId(100);
        assertEquals("superadmin", mapper.loadBy(ref1).getRoleName());

        UserRole ref2 = new UserRole();
        ref2.setUserId(1);
        ref2.setRoleId(200);
        assertEquals("chief-editor", mapper.loadBy(ref2).getRoleName());

        UserRole ref3 = new UserRole();
        ref3.setUserId(2);
        ref3.setRoleId(100);
        assertEquals("moderator", mapper.loadBy(ref3).getRoleName());
    }

    /** update 批量局部更新多条复合主键记录（逐条 update） */
    @Test
    public void testBatchUpdate() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        mapper.insert(new UserRole(1, 100, "admin"));
        mapper.insert(new UserRole(1, 200, "editor"));
        mapper.insert(new UserRole(2, 100, "admin"));

        // 批量 update：逐条局部更新 roleName
        String[] newNames = {"super-1", "super-2", "super-3"};
        int[][] keys = {{1, 100}, {1, 200}, {2, 100}};
        int total = 0;
        for (int i = 0; i < keys.length; i++) {
            UserRole sample = new UserRole();
            sample.setUserId(keys[i][0]);
            sample.setRoleId(keys[i][1]);
            sample.setRoleName(newNames[i]);
            total += mapper.update(sample);
        }
        assertEquals(3, total);

        // 验证每条都被更新
        for (int i = 0; i < keys.length; i++) {
            UserRole ref = new UserRole();
            ref.setUserId(keys[i][0]);
            ref.setRoleId(keys[i][1]);
            assertEquals(newNames[i], mapper.loadBy(ref).getRoleName());
        }
    }

    /** upsert 批量操作：混合插入和更新 */
    @Test
    public void testBatchUpsert() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        mapper.insert(new UserRole(1, 100, "admin"));
        mapper.insert(new UserRole(1, 200, "editor"));

        // 批量 upsert：(1,100) 已存在→更新，(1,200) 已存在→更新，(2,100) 不存在→插入
        List<UserRole> batch = Arrays.asList(
                new UserRole(1, 100, "superadmin"),
                new UserRole(1, 200, "chief-editor"),
                new UserRole(2, 100, "new-admin")
        );
        int total = 0;
        for (UserRole u : batch) {
            total += mapper.upsert(u);
        }
        assertEquals(3, total);
        assertEquals(3, mapper.countAll());

        UserRole ref1 = new UserRole();
        ref1.setUserId(1);
        ref1.setRoleId(100);
        assertEquals("superadmin", mapper.loadBy(ref1).getRoleName());

        UserRole ref2 = new UserRole();
        ref2.setUserId(2);
        ref2.setRoleId(100);
        assertEquals("new-admin", mapper.loadBy(ref2).getRoleName());
    }

    /** deleteList 批量删除所有记录（复合主键） */
    @Test
    public void testBatchDeleteAll() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        List<UserRole> data = Arrays.asList(
                new UserRole(1, 100, "a"),
                new UserRole(1, 200, "b"),
                new UserRole(2, 100, "c"),
                new UserRole(2, 200, "d")
        );
        mapper.insert(data);
        assertEquals(4, mapper.countAll());

        // 删除全部 4 条
        int result = mapper.deleteList(data);
        assertEquals(4, result);
        assertEquals(0, mapper.countAll());
    }

    /** deleteList 包含 null 元素时应跳过 null，只删除有效记录 */
    @Test
    public void testDeleteListWithNullElements() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        mapper.insert(new UserRole(1, 100, "admin"));
        mapper.insert(new UserRole(1, 200, "editor"));

        List<UserRole> toDelete = new ArrayList<>();
        toDelete.add(new UserRole(1, 100, null));
        toDelete.add(null); // null 元素应被跳过
        toDelete.add(new UserRole(1, 200, null));

        int result = mapper.deleteList(toDelete);
        assertEquals(2, result);
        assertEquals(0, mapper.countAll());
    }

    /** deleteList 删除不存在的记录时影响 0 行 */
    @Test
    public void testDeleteListNonExistent() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        mapper.insert(new UserRole(1, 100, "admin"));

        List<UserRole> toDelete = new ArrayList<>();
        toDelete.add(new UserRole(999, 999, null));

        int result = mapper.deleteList(toDelete);
        assertEquals(0, result);
        assertEquals(1, mapper.countAll()); // 原记录未受影响
    }

    // ==================== upsert ====================

    /** upsert(T) 不存在时插入 */
    @Test
    public void testUpsertInsert() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        UserRole role = new UserRole(1, 100, "admin");
        int result = mapper.upsert(role);
        assertEquals(1, result);

        UserRole loaded = mapper.loadBy(role);
        assertNotNull(loaded);
        assertEquals("admin", loaded.getRoleName());
    }

    /** upsert(T) 已存在时更新 */
    @Test
    public void testUpsertUpdate() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        mapper.insert(new UserRole(1, 100, "admin"));

        UserRole updated = new UserRole(1, 100, "superadmin");
        int result = mapper.upsert(updated);
        assertEquals(1, result);

        UserRole loaded = mapper.loadBy(updated);
        assertEquals("superadmin", loaded.getRoleName());
    }

    /** upsertByMap 以 Map 形式进行 upsert（不存在→插入，已存在→更新） */
    @Test
    public void testUpsertByMap() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        // 不存在→插入
        Map<String, Object> map = new HashMap<>();
        map.put("userId", 1);
        map.put("roleId", 100);
        map.put("roleName", "admin");
        int r1 = mapper.upsertByMap(map);
        assertEquals(1, r1);

        // 已存在→更新
        Map<String, Object> mapUpdate = new HashMap<>();
        mapUpdate.put("userId", 1);
        mapUpdate.put("roleId", 100);
        mapUpdate.put("roleName", "superadmin");
        int r2 = mapper.upsertByMap(mapUpdate);
        assertEquals(1, r2);

        UserRole ref = new UserRole();
        ref.setUserId(1);
        ref.setRoleId(100);
        UserRole loaded = mapper.loadBy(ref);
        assertEquals("superadmin", loaded.getRoleName());
    }

    // ==================== delete ====================

    /** delete(T) 按实体复合主键删除 */
    @Test
    public void testDeleteByEntity() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        mapper.insert(new UserRole(1, 100, "admin"));
        mapper.insert(new UserRole(1, 200, "editor"));

        UserRole toDelete = new UserRole();
        toDelete.setUserId(1);
        toDelete.setRoleId(100);
        int result = mapper.delete(toDelete);
        assertEquals(1, result);
        assertEquals(1, mapper.countAll()); // 只剩 editor
    }

    /** deleteByMap 按 Map 复合主键删除 */
    @Test
    public void testDeleteByMap() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        mapper.insert(new UserRole(1, 100, "admin"));
        mapper.insert(new UserRole(2, 100, "admin"));

        Map<String, Object> map = new HashMap<>();
        map.put("userId", 1);
        map.put("roleId", 100);
        int result = mapper.deleteByMap(map);
        assertEquals(1, result);
        assertEquals(1, mapper.countAll());
    }

    /** deleteByMap 缺少主键字段时应抛异常 */
    @Test
    public void testDeleteByMapMissingKey() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        mapper.insert(new UserRole(1, 100, "admin"));

        Map<String, Object> map = new HashMap<>();
        map.put("userId", 1);
        // 缺少 roleId
        try {
            mapper.deleteByMap(map);
            fail("Should throw UnsupportedOperationException for missing primary key");
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains("missing primary key"));
        }
    }

    /** deleteList 批量删除（复合主键用 OR 组合条件） */
    @Test
    public void testDeleteList() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        mapper.insert(new UserRole(1, 100, "admin"));
        mapper.insert(new UserRole(1, 200, "editor"));
        mapper.insert(new UserRole(2, 100, "admin"));

        List<UserRole> toDelete = new ArrayList<>();
        toDelete.add(new UserRole(1, 100, null));
        toDelete.add(new UserRole(2, 100, null));

        int result = mapper.deleteList(toDelete);
        assertEquals(2, result);
        assertEquals(1, mapper.countAll()); // 只剩 (1,200)
    }

    /** deleteListByMap 批量删除（Map 形式） */
    @Test
    public void testDeleteListByMap() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        mapper.insert(new UserRole(1, 100, "admin"));
        mapper.insert(new UserRole(1, 200, "editor"));
        mapper.insert(new UserRole(2, 100, "admin"));

        List<Map<String, Object>> toDelete = new ArrayList<>();
        Map<String, Object> m1 = new HashMap<>();
        m1.put("userId", 1);
        m1.put("roleId", 100);
        toDelete.add(m1);
        Map<String, Object> m2 = new HashMap<>();
        m2.put("userId", 1);
        m2.put("roleId", 200);
        toDelete.add(m2);

        int result = mapper.deleteListByMap(toDelete);
        assertEquals(2, result);
        assertEquals(1, mapper.countAll()); // 只剩 (2,100)
    }

    // ==================== deleteById / deleteByIds 拒绝复合主键 ====================

    /** deleteById 复合主键时抛 UnsupportedOperationException */
    @Test
    public void testDeleteByIdThrowsOnCompositeKey() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);
        try {
            mapper.deleteById(1);
            fail("Should throw UnsupportedOperationException for composite key");
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains("does not support composite primary key"));
        }
    }

    /** deleteByIds 复合主键时抛 UnsupportedOperationException */
    @Test
    public void testDeleteByIdsThrowsOnCompositeKey() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);
        try {
            mapper.deleteByIds(Arrays.asList(1, 2, 3));
            fail("Should throw UnsupportedOperationException for composite key");
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains("does not support composite primary key"));
        }
    }

    // ==================== selectById / selectByIds 拒绝复合主键 ====================

    /** selectById 复合主键时抛 UnsupportedOperationException */
    @Test
    public void testSelectByIdThrowsOnCompositeKey() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);
        try {
            mapper.selectById(1);
            fail("Should throw UnsupportedOperationException for composite key");
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains("does not support composite primary key"));
        }
    }

    /** selectByIds 复合主键时抛 UnsupportedOperationException */
    @Test
    public void testSelectByIdsThrowsOnCompositeKey() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);
        try {
            mapper.selectByIds(Arrays.asList(1, 2));
            fail("Should throw UnsupportedOperationException for composite key");
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains("does not support composite primary key"));
        }
    }

    // ==================== loadBy / loadListBy ====================

    /** loadBy(T) 使用实体作为引用对象加载 */
    @Test
    public void testLoadByEntity() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        mapper.insert(new UserRole(1, 100, "admin"));
        mapper.insert(new UserRole(1, 200, "editor"));

        UserRole ref = new UserRole();
        ref.setUserId(1);
        ref.setRoleId(100);
        UserRole loaded = mapper.loadBy(ref);
        assertNotNull(loaded);
        assertEquals("admin", loaded.getRoleName());
    }

    /** loadBy(Map) 使用 Map 作为引用对象加载 */
    @Test
    public void testLoadByMap() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        mapper.insert(new UserRole(1, 100, "admin"));

        Map<String, Object> ref = new HashMap<>();
        ref.put("userId", 1);
        ref.put("roleId", 100);
        UserRole loaded = mapper.loadBy(ref);
        assertNotNull(loaded);
        assertEquals("admin", loaded.getRoleName());
    }

    /** loadBy 不匹配时返回 null */
    @Test
    public void testLoadByNotFound() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        mapper.insert(new UserRole(1, 100, "admin"));

        UserRole ref = new UserRole();
        ref.setUserId(999);
        ref.setRoleId(999);
        UserRole loaded = mapper.loadBy(ref);
        assertNull(loaded);
    }

    /** loadBy(Map) 缺少主键字段时抛异常 */
    @Test
    public void testLoadByMapMissingKey() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        Map<String, Object> ref = new HashMap<>();
        ref.put("userId", 1);
        // 缺少 roleId
        try {
            mapper.loadBy(ref);
            fail("Should throw UnsupportedOperationException for missing primary key");
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains("missing primary key"));
        }
    }

    /** loadListBy 批量加载多条复合主键记录 */
    @Test
    public void testLoadListBy() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        mapper.insert(new UserRole(1, 100, "admin"));
        mapper.insert(new UserRole(1, 200, "editor"));
        mapper.insert(new UserRole(2, 100, "admin"));

        List<UserRole> refs = new ArrayList<>();
        UserRole r1 = new UserRole();
        r1.setUserId(1);
        r1.setRoleId(100);
        refs.add(r1);
        UserRole r2 = new UserRole();
        r2.setUserId(2);
        r2.setRoleId(100);
        refs.add(r2);

        List<UserRole> loaded = mapper.loadListBy(refs);
        assertEquals(2, loaded.size());
    }

    /** loadListBy 使用 Map 引用列表 */
    @Test
    public void testLoadListByMap() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        mapper.insert(new UserRole(1, 100, "admin"));
        mapper.insert(new UserRole(1, 200, "editor"));

        List<Map<String, Object>> refs = new ArrayList<>();
        Map<String, Object> m1 = new HashMap<>();
        m1.put("userId", 1);
        m1.put("roleId", 100);
        refs.add(m1);
        Map<String, Object> m2 = new HashMap<>();
        m2.put("userId", 1);
        m2.put("roleId", 200);
        refs.add(m2);

        List<UserRole> loaded = mapper.loadListBy(refs);
        assertEquals(2, loaded.size());
    }

    // ==================== listBySample / countBySample / countAll ====================

    /** listBySample 按样本条件查询复合主键表（非主键字段匹配） */
    @Test
    public void testListBySample() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        mapper.insert(new UserRole(1, 100, "admin"));
        mapper.insert(new UserRole(2, 100, "admin"));
        mapper.insert(new UserRole(1, 200, "editor"));

        UserRole sample = new UserRole();
        sample.setRoleName("admin");
        List<UserRole> list = mapper.listBySample(sample);
        assertEquals(2, list.size());
    }

    /** listBySample 按复合主键部分字段查询 */
    @Test
    public void testListBySamplePartialKey() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        mapper.insert(new UserRole(1, 100, "admin"));
        mapper.insert(new UserRole(1, 200, "editor"));
        mapper.insert(new UserRole(2, 100, "admin"));

        UserRole sample = new UserRole();
        sample.setUserId(1);
        List<UserRole> list = mapper.listBySample(sample);
        assertEquals(2, list.size());
    }

    /** countBySample 按样本统计复合主键表记录数 */
    @Test
    public void testCountBySample() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        mapper.insert(new UserRole(1, 100, "admin"));
        mapper.insert(new UserRole(2, 100, "admin"));
        mapper.insert(new UserRole(3, 300, "viewer"));

        UserRole sample = new UserRole();
        sample.setRoleName("admin");
        int count = mapper.countBySample(sample);
        assertEquals(2, count);
    }

    /** countAll 统计复合主键表全部记录 */
    @Test
    public void testCountAll() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        assertEquals(0, mapper.countAll());
        mapper.insert(new UserRole(1, 100, "admin"));
        mapper.insert(new UserRole(2, 200, "editor"));
        assertEquals(2, mapper.countAll());
    }

    // ==================== pageBySample ====================

    /** pageBySample 复合主键表分页查询 */
    @Test
    public void testPageBySample() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        // 插入 10 条数据
        for (int i = 1; i <= 10; i++) {
            mapper.insert(new UserRole(i, 100, "admin"));
        }

        UserRole sample = new UserRole();
        sample.setRoleName("admin");

        PageObject page = new PageObject();
        page.setPageSize(3);
        page.setCurrentPage(0);

        PageResult<UserRole> result = mapper.pageBySample(sample, page);
        List<UserRole> data = result.getData();
        assertEquals(3, data.size());
        for (UserRole ur : data) {
            assertEquals("admin", ur.getRoleName());
        }
    }

    // ==================== 复合主键数据隔离 ====================

    /** 验证只有两个主键字段完全相同才视为同一条记录 */
    @Test
    public void testCompositeKeyIsolation() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        // (1,100) (1,200) (2,100) (2,200) — 4条不同的记录
        mapper.insert(new UserRole(1, 100, "a"));
        mapper.insert(new UserRole(1, 200, "b"));
        mapper.insert(new UserRole(2, 100, "c"));
        mapper.insert(new UserRole(2, 200, "d"));
        assertEquals(4, mapper.countAll());

        // 只修改 (1,100) 的 roleName，不影响 (1,200)
        UserRole updateTarget = new UserRole();
        updateTarget.setUserId(1);
        updateTarget.setRoleId(100);
        updateTarget.setRoleName("updated");
        mapper.update(updateTarget);

        UserRole ref100 = new UserRole();
        ref100.setUserId(1);
        ref100.setRoleId(100);
        assertEquals("updated", mapper.loadBy(ref100).getRoleName());

        UserRole ref200 = new UserRole();
        ref200.setUserId(1);
        ref200.setRoleId(200);
        assertEquals("b", mapper.loadBy(ref200).getRoleName()); // 未被影响

        // 只删除 (2,100)，不影响 (2,200)
        UserRole toDelete = new UserRole();
        toDelete.setUserId(2);
        toDelete.setRoleId(100);
        mapper.delete(toDelete);
        assertEquals(3, mapper.countAll());

        UserRole ref2_200 = new UserRole();
        ref2_200.setUserId(2);
        ref2_200.setRoleId(200);
        assertNotNull(mapper.loadBy(ref2_200)); // (2,200) 仍存在
    }

    /** replace 按复合主键更新，只影响精确匹配的记录 */
    @Test
    public void testReplaceIsolation() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        mapper.insert(new UserRole(1, 100, "admin"));
        mapper.insert(new UserRole(1, 200, "editor"));

        // replace (1,100)，roleName 改为 null
        UserRole replaced = new UserRole();
        replaced.setUserId(1);
        replaced.setRoleId(100);
        replaced.setRoleName(null);
        mapper.replace(replaced);

        // (1,100) 的 roleName 被设为 null
        UserRole ref100 = new UserRole();
        ref100.setUserId(1);
        ref100.setRoleId(100);
        assertNull(mapper.loadBy(ref100).getRoleName());

        // (1,200) 不受影响
        UserRole ref200 = new UserRole();
        ref200.setUserId(1);
        ref200.setRoleId(200);
        assertEquals("editor", mapper.loadBy(ref200).getRoleName());
    }

    /** upsert 按复合主键精确判断存在性 */
    @Test
    public void testUpsertIsolation() throws SQLException {
        Session session = newSession();
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        mapper.insert(new UserRole(1, 100, "admin"));

        // upsert (1,200) —— 不存在，应插入
        UserRole newRole = new UserRole(1, 200, "editor");
        mapper.upsert(newRole);
        assertEquals(2, mapper.countAll());

        // upsert (1,100) —— 已存在，应更新
        UserRole updated = new UserRole(1, 100, "superadmin");
        mapper.upsert(updated);
        assertEquals(2, mapper.countAll());

        UserRole ref = new UserRole();
        ref.setUserId(1);
        ref.setRoleId(100);
        assertEquals("superadmin", mapper.loadBy(ref).getRoleName());
    }
}
