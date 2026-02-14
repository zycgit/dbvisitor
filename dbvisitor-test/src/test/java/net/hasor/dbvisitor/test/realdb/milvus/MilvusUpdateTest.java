package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.realdb.milvus.dto.UserInfoMilvus;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class MilvusUpdateTest {

    @Before
    public void before() {
        try (Connection c = OneApiDataSourceManager.getConnection("milvus")) {
            JdbcTemplate jdbc = new JdbcTemplate(c);
            jdbc.execute("DROP TABLE IF EXISTS tb_user_info_milvus");
            jdbc.execute("CREATE TABLE tb_user_info_milvus (uid VARCHAR(64) PRIMARY KEY, name VARCHAR(64), loginName VARCHAR(64), loginPassword VARCHAR(64), v FLOAT_VECTOR(2))");
            jdbc.execute("CREATE INDEX idx_user_v ON TABLE tb_user_info_milvus (v) USING \"IVF_FLAT\" WITH (nlist = 128, metric_type = 'L2')");
            jdbc.execute("LOAD TABLE tb_user_info_milvus");
        } catch (Throwable e) {
            Assume.assumeNoException("Milvus setup failed - skipping tests", e);
        }
    }

    private List<Float> sampleVector(float val) {
        return Arrays.asList(val, val);
    }

    @Test
    public void testUpdateColumns() throws SQLException {
        try (Connection c = OneApiDataSourceManager.getConnection("milvus")) {
            LambdaTemplate lambda = new LambdaTemplate(c);

            // 1. Prepare Data
            UserInfoMilvus user = new UserInfoMilvus();
            user.setUid("u_001");
            user.setName("name_init");
            user.setLoginName("login_init");
            user.setLoginPassword("pass_init");
            user.setV(sampleVector(0.1f));

            lambda.insert(UserInfoMilvus.class).applyEntity(user).executeSumResult();

            // 2. Update Single Column
            int r1 = lambda.update(UserInfoMilvus.class).eq(UserInfoMilvus::getUid, "u_001").updateTo(UserInfoMilvus::getName, "name_updated").doUpdate();
            assert r1 == 1;

            UserInfoMilvus u1 = lambda.query(UserInfoMilvus.class).eq(UserInfoMilvus::getUid, "u_001").queryForObject();
            assert "name_updated".equals(u1.getName());
            assert "login_init".equals(u1.getLoginName());

            // 3. Update Multiple Columns
            int r2 = lambda.update(UserInfoMilvus.class).eq(UserInfoMilvus::getUid, "u_001").updateTo(UserInfoMilvus::getName, "name_updated_2").updateTo(UserInfoMilvus::getLoginName, "login_updated").doUpdate();
            assert r2 == 1;

            UserInfoMilvus u2 = lambda.query(UserInfoMilvus.class).eq(UserInfoMilvus::getUid, "u_001").queryForObject();
            assert "name_updated_2".equals(u2.getName());
            assert "login_updated".equals(u2.getLoginName());
        }
    }

    @Test
    public void testUpdatePassword() throws SQLException {
        try (Connection c = OneApiDataSourceManager.getConnection("milvus")) {
            LambdaTemplate lambda = new LambdaTemplate(c);

            // 1. Prepare Data
            UserInfoMilvus user = new UserInfoMilvus();
            user.setUid("u_002");
            user.setName("name_002");
            user.setLoginName("login_002");
            user.setLoginPassword("pass_002");
            user.setV(sampleVector(0.2f));

            lambda.insert(UserInfoMilvus.class).applyEntity(user).executeSumResult();

            // 2. Update Password
            int r = lambda.update(UserInfoMilvus.class).eq(UserInfoMilvus::getUid, "u_002").updateTo(UserInfoMilvus::getLoginPassword, "pass_002_new").doUpdate();
            assert r == 1;

            UserInfoMilvus u = lambda.query(UserInfoMilvus.class).eq(UserInfoMilvus::getUid, "u_002").queryForObject();
            assert "pass_002_new".equals(u.getLoginPassword());
        }
    }

    @Test
    public void testUpdateByMap() throws SQLException {
        try (Connection c = OneApiDataSourceManager.getConnection("milvus")) {
            LambdaTemplate lambda = new LambdaTemplate(c);

            UserInfoMilvus user = new UserInfoMilvus();
            user.setLoginName("login_003");
            user.setLoginPassword("pass_003");
            user.setUid("u_003");
            user.setName("name_003");
            user.setV(sampleVector(0.3f));
            lambda.insert(UserInfoMilvus.class).applyEntity(user).executeSumResult();

            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put("name", "name_003_updated");
            updateMap.put("loginName", "login_003_new");

            int r = lambda.update(UserInfoMilvus.class).eq(UserInfoMilvus::getUid, "u_003").updateToSampleMap(updateMap).doUpdate();
            assert r == 1;

            UserInfoMilvus u = lambda.query(UserInfoMilvus.class).eq(UserInfoMilvus::getUid, "u_003").queryForObject();
            assert "name_003_updated".equals(u.getName());
            assert "login_003_new".equals(u.getLoginName());
        }
    }

    @Test
    public void testUpdateVector() throws SQLException {
        try (Connection c = OneApiDataSourceManager.getConnection("milvus")) {
            LambdaTemplate lambda = new LambdaTemplate(c);

            UserInfoMilvus user = new UserInfoMilvus();
            user.setLoginName("login_004");
            user.setLoginPassword("pass_004");
            user.setUid("u_004");
            user.setName("name_004");
            user.setV(sampleVector(0.4f));
            lambda.insert(UserInfoMilvus.class).applyEntity(user).executeSumResult();

            List<Float> newVec = sampleVector(0.9f);
            int r = lambda.update(UserInfoMilvus.class).eq(UserInfoMilvus::getUid, "u_004").updateTo(UserInfoMilvus::getV, newVec).doUpdate();
            assert r == 1;

            UserInfoMilvus u = lambda.query(UserInfoMilvus.class).eq(UserInfoMilvus::getUid, "u_004").queryForObject();
            assert newVec.equals(u.getV()) || (Math.abs(newVec.get(0) - u.getV().get(0)) < 0.0001);
        }
    }
}
