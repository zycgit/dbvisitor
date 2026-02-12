package net.hasor.dbvisitor.test.oneapi.realdb.milvus;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.hasor.cobble.concurrent.ThreadUtils;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.oneapi.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.oneapi.realdb.milvus.dto1.UserInfoMilvus1;
import net.hasor.dbvisitor.test.oneapi.realdb.milvus.dto1.UserInfoMilvus1Mapper;
import net.hasor.dbvisitor.test.oneapi.realdb.milvus.dto2.UserInfoMilvus2;
import net.hasor.dbvisitor.test.oneapi.realdb.milvus.dto2.UserInfoMilvus2Mapper;
import net.hasor.dbvisitor.test.oneapi.realdb.milvus.dto3.UserInfoMilvus3;
import net.hasor.dbvisitor.test.oneapi.realdb.milvus.dto3.UserInfoMilvus3Mapper;
import net.hasor.dbvisitor.test.oneapi.realdb.milvus.dto3.UserInfoMilvus4Mapper;
import net.hasor.dbvisitor.test.oneapi.realdb.milvus.dto5.UserInfoMilvus5;
import net.hasor.dbvisitor.test.oneapi.realdb.milvus.dto5.UserInfoMilvus5Mapper;
import net.hasor.dbvisitor.test.oneapi.realdb.milvus.dto6.UserInfoMilvus6;
import net.hasor.dbvisitor.test.oneapi.realdb.milvus.dto6.UserInfoMilvus6Mapper;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class MilvusMapperTest {

    private void initTable(JdbcTemplate jdbc, String tableName, String createSql) {
        try {
            jdbc.execute("DROP TABLE IF EXISTS " + tableName);
        } catch (Exception e) {
            // ignore
        }
        try {
            jdbc.execute(createSql);
        } catch (Exception e) {
            // ignore
        }
    }

    private void initIndex(JdbcTemplate jdbc, String indexName, String tableName, String createIndexSql) {
        try {
            jdbc.execute(createIndexSql);
            // Milvus index creation is async usually
            ThreadUtils.sleep(2000);
        } catch (Exception e) {
            // ignore
        }
    }

    @Before
    public void before() {
        try (Connection c = OneApiDataSourceManager.getConnection("milvus")) {
            JdbcTemplate jdbc = new JdbcTemplate(c);

            // Table 1 for Annotations
            initTable(jdbc, "tb_mapper_user_milvus", "CREATE TABLE IF NOT EXISTS tb_mapper_user_milvus (uid VARCHAR(64) PRIMARY KEY, name VARCHAR(64), loginName VARCHAR(64), loginPassword VARCHAR(64), v FLOAT_VECTOR(2)) WITH (consistency_level = 'Strong')");
            initIndex(jdbc, "idx_mapper_user_v", "tb_mapper_user_milvus", "CREATE INDEX idx_mapper_user_v ON TABLE tb_mapper_user_milvus (v) USING \"IVF_FLAT\" WITH (nlist = 128, metric_type = 'L2')");
            jdbc.execute("LOAD TABLE tb_mapper_user_milvus");
            ThreadUtils.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
            Assume.assumeNoException(e);
        }
    }

    private List<Float> sampleVector() {
        List<Float> v = new ArrayList<>();
        v.add(1.0f);
        v.add(0.5f);
        return v;
    }

    @Test
    public void using_mapper_api_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session session = config.newSession(OneApiDataSourceManager.getConnection("milvus"))) {
            UserInfoMilvus1Mapper mapper = session.createMapper(UserInfoMilvus1Mapper.class);

            UserInfoMilvus1 user = new UserInfoMilvus1();
            user.setUid("u1");
            user.setName("nomo");
            user.setLoginName("nomo");
            user.setLoginPassword("123456");
            user.setV(sampleVector());

            // Insert
            int result = mapper.insertUser(user);
            assert result == 1;

            // Query
            UserInfoMilvus1 loaded = null;
            for (int i = 0; i < 20; i++) {
                loaded = mapper.selectUser("u1");
                if (loaded != null) {
                    break;
                }
                ThreadUtils.sleep(1000);
            }
            assert loaded != null;
            assert "u1".equals(loaded.getUid());

            // Delete
            int delResult = mapper.deleteUser("u1");
            assert delResult == 1;
        }
    }

    @Test
    public void using_mapper_api_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session session = config.newSession(OneApiDataSourceManager.getConnection("milvus"))) {
            UserInfoMilvus2Mapper mapper = session.createMapper(UserInfoMilvus2Mapper.class);

            UserInfoMilvus2 user = new UserInfoMilvus2();
            user.setUid("u2");
            user.setName("nomo2");
            user.setLoginName("nomo2");
            user.setLoginPassword("123456");
            user.setV(sampleVector());

            // Insert (BaseMapper method)
            int result = mapper.insert(user);
            assert result == 1;

            // Query (BaseMapper method)
            UserInfoMilvus2 loaded = null;
            for (int i = 0; i < 20; i++) {
                loaded = mapper.selectById("u2");
                if (loaded != null) {
                    break;
                }
                ThreadUtils.sleep(1000);
            }
            assert loaded != null;
            assert "nomo2".equals(loaded.getName());

            // Delete (BaseMapper method)
            int delResult = mapper.delete(loaded);
            assert delResult == 1;
        }
    }

    @Test
    public void using_mapper_file_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session session = config.newSession(OneApiDataSourceManager.getConnection("milvus"))) {
            UserInfoMilvus3Mapper mapper = session.createMapper(UserInfoMilvus3Mapper.class);

            UserInfoMilvus3 user = new UserInfoMilvus3();
            user.setUid("u3");
            user.setName("nomo3");
            user.setLoginName("nomo3");
            user.setLoginPassword("123456");
            user.setV(sampleVector());

            // Insert
            int result = mapper.insertUser(user);
            assert result == 1;

            // Query
            List<UserInfoMilvus3> users = null;
            for (int i = 0; i < 20; i++) {
                users = mapper.queryAll();
                if (users != null && !users.isEmpty()) {
                    break;
                }
                ThreadUtils.sleep(1000);
            }
            assert users != null && !users.isEmpty();
            assert users.stream().anyMatch(u -> "u3".equals(u.getUid()));

            // Delete
            int delResult = mapper.deleteUser("u3");
            assert delResult == 1;
        }
    }

    @Test
    public void using_mapper_api_4() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session session = config.newSession(OneApiDataSourceManager.getConnection("milvus"))) {
            UserInfoMilvus4Mapper mapper = session.createMapper(UserInfoMilvus4Mapper.class);

            UserInfoMilvus1 user = new UserInfoMilvus1();
            user.setUid("u4");
            user.setName("nomo4");
            user.setLoginName("nomo4");
            user.setLoginPassword("123456");
            user.setV(sampleVector());

            // Insert
            int result = mapper.insertUser(user);
            assert result == 1;

            // Query
            UserInfoMilvus1 loaded = null;
            for (int i = 0; i < 20; i++) {
                loaded = mapper.findUser("u4");
                if (loaded != null) {
                    break;
                }
                ThreadUtils.sleep(1000);
            }
            assert loaded != null;
            assert "nomo4".equals(loaded.getName());

            // Delete
            int delResult = mapper.deleteUser("u4");
            assert delResult == 1;
        }
    }

    @Test
    public void using_mapper_api_5() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session session = config.newSession(OneApiDataSourceManager.getConnection("milvus"))) {
            UserInfoMilvus5Mapper mapper = session.createMapper(UserInfoMilvus5Mapper.class);

            UserInfoMilvus5 user = new UserInfoMilvus5();
            user.setUserId("u5");
            user.setUserName("nomo5");
            user.setAccount("nomo5");
            user.setPassword("123456");
            user.setVector(sampleVector());

            // Insert
            int result = mapper.insertUser(user);
            assert result == 1;

            // Query
            List<UserInfoMilvus5> loadedList = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                loadedList = mapper.queryAll();
                if (loadedList.stream().anyMatch(u -> "u5".equals(u.getUserId()))) {
                    break;
                }
                ThreadUtils.sleep(1000);
            }
            UserInfoMilvus5 loaded = loadedList.stream().filter(u -> "u5".equals(u.getUserId())).findFirst().orElse(null);

            assert loaded != null;
            assert "nomo5".equals(loaded.getUserName());
            assert "nomo5".equals(loaded.getAccount());

            // Delete
            int delResult = mapper.deleteUser("u5");
            assert delResult == 1;
        }
    }

    @Test
    public void using_mapper_api_6() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session session = config.newSession(OneApiDataSourceManager.getConnection("milvus"))) {
            UserInfoMilvus6Mapper mapper = session.createMapper(UserInfoMilvus6Mapper.class);

            UserInfoMilvus6 user = new UserInfoMilvus6();
            user.setUserId("u6");
            user.setUserName("nomo6");
            user.setAccount("nomo6");
            user.setPassword("123456");
            user.setVector(sampleVector());

            // Insert
            int result = mapper.insert(user);
            assert result == 1;

            // Query
            UserInfoMilvus6 loaded = null;
            for (int i = 0; i < 20; i++) {
                loaded = mapper.selectById("u6");
                if (loaded != null) {
                    break;
                }
                ThreadUtils.sleep(1000);
            }
            assert loaded != null;
            assert "nomo6".equals(loaded.getUserName());
            assert "nomo6".equals(loaded.getAccount());

            // Delete
            int delResult = mapper.delete(loaded);
            assert delResult == 1;
        }
    }
}
