package net.hasor.dbvisitor.test.suite.transaction;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import net.hasor.dbvisitor.transaction.Isolation;
import net.hasor.dbvisitor.transaction.Propagation;
import net.hasor.dbvisitor.transaction.TransactionManager;
import net.hasor.dbvisitor.transaction.TransactionStatus;
import net.hasor.dbvisitor.transaction.support.TransactionHelper;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * 混合 API 事务测试 + 隔离级别测试
 * <p>覆盖：
 * <ul>
 *   <li>JdbcTemplate + LambdaTemplate 混合使用</li>
 *   <li>Session (XML Mapper) + LambdaTemplate 混合使用</li>
 *   <li>BaseMapper + LambdaTemplate 混合使用</li>
 *   <li>多种 API 在同一事务中的联动</li>
 *   <li>不同传播行为下混合 API</li>
 *   <li>隔离级别设置验证</li>
 * </ul>
 */
public class TransactionMixedApiTest extends AbstractOneApiTest {

    private TransactionManager getTxManager() {
        return TransactionHelper.txManager(dataSource);
    }

    private long countById(int id) throws SQLException {
        return lambdaTemplate.query(UserInfo.class).eq(UserInfo::getId, id).queryForCount();
    }

    // ==================== JdbcTemplate + LambdaTemplate ====================

    /**
     * JdbcTemplate 写入 + LambdaTemplate 读取，同一事务中数据可见，提交后持久化
     */
    @Test
    public void testMixed_JdbcAndLambda_Commit() throws SQLException {
        TransactionManager tm = getTxManager();
        TransactionStatus status = tm.begin(Propagation.REQUIRED);

        // JdbcTemplate 写入
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, CURRENT_TIMESTAMP)", new Object[] { 63001, "MixJdbc1", 25 });

        // LambdaTemplate 写入
        UserInfo user = new UserInfo();
        user.setId(63002);
        user.setName("MixLambda1");
        user.setCreateTime(new Date());
        lambdaTemplate.insert(UserInfo.class).applyEntity(user).executeSumResult();

        // 事务内相互可见
        assertEquals(1, countById(63001));
        assertEquals(1, countById(63002));

        tm.commit(status);

        // 提交后持久化
        assertEquals(1, countById(63001));
        assertEquals(1, countById(63002));
    }

    /**
     * JdbcTemplate + LambdaTemplate 写入后回滚 → 两条数据全部丢失
     */
    @Test
    public void testMixed_JdbcAndLambda_Rollback() throws SQLException {
        TransactionManager tm = getTxManager();
        TransactionStatus status = tm.begin(Propagation.REQUIRED);

        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, CURRENT_TIMESTAMP)", new Object[] { 63003, "MixJdbcRb", 25 });

        UserInfo user = new UserInfo();
        user.setId(63004);
        user.setName("MixLambdaRb");
        user.setCreateTime(new Date());
        lambdaTemplate.insert(UserInfo.class).applyEntity(user).executeSumResult();

        tm.rollBack(status);

        assertEquals(0, countById(63003));
        assertEquals(0, countById(63004));
    }

    // ==================== Session (XML Mapper) + LambdaTemplate ====================

    /**
     * Session.executeStatement (XML Mapper 写入) + LambdaTemplate 读取，同一事务中联动
     */
    @Test
    public void testMixed_SessionAndLambda_Commit() throws Exception {
        Configuration config = new Configuration();
        config.loadMapper("/mapper/StatementTestMapper.xml");
        Session session = config.newSession(dataSource);

        TransactionManager tm = getTxManager();
        TransactionStatus status = tm.begin(Propagation.REQUIRED);

        // Session 通过 XML Mapper 写入
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", 63011);
        params.put("name", "MixSession1");
        params.put("age", 30);
        params.put("email", "session@test.com");
        session.executeStatement("StatementTestMapper.insertUserWithId", params);

        // LambdaTemplate 写入（同一事务）
        UserInfo user = new UserInfo();
        user.setId(63012);
        user.setName("MixLambda2");
        user.setCreateTime(new Date());
        lambdaTemplate.insert(UserInfo.class).applyEntity(user).executeSumResult();

        // 事务内相互可见
        Map<String, Object> q = new HashMap<String, Object>();
        q.put("id", 63011);
        List<UserInfo> sessionResult = session.queryStatement("StatementTestMapper.queryUserById", q);
        assertEquals(1, sessionResult.size());
        assertEquals(1, countById(63012));

        tm.commit(status);

        assertEquals(1, countById(63011));
        assertEquals(1, countById(63012));
    }

    /**
     * Session + LambdaTemplate 回滚 → 两种 API 写入的数据全部回滚
     */
    @Test
    public void testMixed_SessionAndLambda_Rollback() throws Exception {
        Configuration config = new Configuration();
        config.loadMapper("/mapper/StatementTestMapper.xml");
        Session session = config.newSession(dataSource);

        TransactionManager tm = getTxManager();
        TransactionStatus status = tm.begin(Propagation.REQUIRED);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", 63013);
        params.put("name", "MixSesRb");
        params.put("age", 30);
        params.put("email", "sesrb@test.com");
        session.executeStatement("StatementTestMapper.insertUserWithId", params);

        UserInfo user = new UserInfo();
        user.setId(63014);
        user.setName("MixLamRb2");
        user.setCreateTime(new Date());
        lambdaTemplate.insert(UserInfo.class).applyEntity(user).executeSumResult();

        tm.rollBack(status);

        assertEquals(0, countById(63013));
        assertEquals(0, countById(63014));
    }

    // ==================== BaseMapper + LambdaTemplate ====================

    /**
     * BaseMapper 写入 + LambdaTemplate 读取，同一事务中
     */
    @Test
    public void testMixed_BaseMapperAndLambda_Commit() throws Exception {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        TransactionManager tm = getTxManager();
        TransactionStatus status = tm.begin(Propagation.REQUIRED);

        // BaseMapper 写入
        UserInfo u1 = new UserInfo();
        u1.setId(63021);
        u1.setName("MixBM1");
        u1.setCreateTime(new Date());
        mapper.insert(u1);

        // LambdaTemplate 写入
        UserInfo u2 = new UserInfo();
        u2.setId(63022);
        u2.setName("MixLam3");
        u2.setCreateTime(new Date());
        lambdaTemplate.insert(UserInfo.class).applyEntity(u2).executeSumResult();

        // 事务内 BaseMapper 可读 LambdaTemplate 写入的数据
        UserInfo found = mapper.selectById(63022);
        assertNotNull(found);
        assertEquals("MixLam3", found.getName());

        // LambdaTemplate 可读 BaseMapper 写入的数据
        assertEquals(1, countById(63021));

        tm.commit(status);

        assertEquals(1, countById(63021));
        assertEquals(1, countById(63022));
    }

    // ==================== 跨传播行为混合 API ====================

    /**
     * 外层 REQUIRED 用 JdbcTemplate，内层 REQUIRES_NEW 用 LambdaTemplate → 互不影响
     */
    @Test
    public void testMixed_RequiresNew_DifferentApis() throws SQLException {
        TransactionManager tm = getTxManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED);
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, CURRENT_TIMESTAMP)", new Object[] { 63031, "MixOuterJdbc", 25 });

        TransactionStatus inner = tm.begin(Propagation.REQUIRES_NEW);
        UserInfo user = new UserInfo();
        user.setId(63032);
        user.setName("MixInnerLam");
        user.setCreateTime(new Date());
        lambdaTemplate.insert(UserInfo.class).applyEntity(user).executeSumResult();
        tm.commit(inner);

        tm.rollBack(outer);

        assertEquals(0, countById(63031)); // 外层 JdbcTemplate 回滚
        assertEquals(1, countById(63032)); // 内层 LambdaTemplate 独立提交
    }

    // ==================== 隔离级别验证 ====================

    /**
     * READ_COMMITTED 隔离级别下正常提交
     */
    @Test
    public void testIsolation_ReadCommitted() throws SQLException {
        TransactionManager tm = getTxManager();
        TransactionStatus status = tm.begin(Propagation.REQUIRED, Isolation.READ_COMMITTED);

        // 验证事务属性
        assertEquals(Isolation.READ_COMMITTED, status.getIsolationLevel());

        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, CURRENT_TIMESTAMP)", new Object[] { 63041, "IsoRC", 25 });

        tm.commit(status);

        assertEquals(1, countById(63041));
    }

    /**
     * REPEATABLE_READ 隔离级别下正常提交
     */
    @Test
    public void testIsolation_RepeatableRead() throws SQLException {
        TransactionManager tm = getTxManager();
        TransactionStatus status = tm.begin(Propagation.REQUIRED, Isolation.REPEATABLE_READ);

        assertEquals(Isolation.REPEATABLE_READ, status.getIsolationLevel());

        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, CURRENT_TIMESTAMP)", new Object[] { 63042, "IsoRR", 25 });

        tm.commit(status);

        assertEquals(1, countById(63042));
    }

    /**
     * SERIALIZABLE 隔离级别下正常提交
     */
    @Test
    public void testIsolation_Serializable() throws SQLException {
        TransactionManager tm = getTxManager();
        TransactionStatus status = tm.begin(Propagation.REQUIRED, Isolation.SERIALIZABLE);

        assertEquals(Isolation.SERIALIZABLE, status.getIsolationLevel());

        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, CURRENT_TIMESTAMP)", new Object[] { 63043, "IsoSer", 25 });

        tm.commit(status);

        assertEquals(1, countById(63043));
    }

    /**
     * REQUIRES_NEW 使用不同隔离级别：事务结束后隔离级别应自动恢复
     */
    @Test
    public void testIsolation_RestoreAfterRequiresNew() throws SQLException {
        TransactionManager tm = getTxManager();

        TransactionStatus outer = tm.begin(Propagation.REQUIRED, Isolation.READ_COMMITTED);
        assertEquals(Isolation.READ_COMMITTED, outer.getIsolationLevel());

        // 内层使用 SERIALIZABLE
        TransactionStatus inner = tm.begin(Propagation.REQUIRES_NEW, Isolation.SERIALIZABLE);
        assertEquals(Isolation.SERIALIZABLE, inner.getIsolationLevel());

        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, CURRENT_TIMESTAMP)", new Object[] { 63044, "IsoRestore", 25 });

        tm.commit(inner); // 隔离级别应恢复

        // 外层事务继续
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, CURRENT_TIMESTAMP)", new Object[] { 63045, "IsoOuter", 25 });

        tm.commit(outer);

        assertEquals(1, countById(63044));
        assertEquals(1, countById(63045));
    }
}
