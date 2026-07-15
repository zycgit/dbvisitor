package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.adapter.AdapterContractTest;
import net.hasor.dbvisitor.test.realdb.milvus.material.user.UserInfoMilvus1;
import net.hasor.dbvisitor.test.realdb.milvus.material.user.UserInfoMilvus1Mapper;
import net.hasor.dbvisitor.test.realdb.milvus.material.user.UserInfoMilvus2;
import net.hasor.dbvisitor.test.realdb.milvus.material.user.UserInfoMilvus2Mapper;
import net.hasor.dbvisitor.test.realdb.milvus.material.user.UserInfoMilvus3;
import net.hasor.dbvisitor.test.realdb.milvus.material.user.UserInfoMilvus3Mapper;
import net.hasor.dbvisitor.test.realdb.milvus.material.user.UserInfoMilvus4Mapper;
import net.hasor.dbvisitor.test.realdb.milvus.material.user.UserInfoMilvus5;
import net.hasor.dbvisitor.test.realdb.milvus.material.user.UserInfoMilvus5Mapper;
import net.hasor.dbvisitor.test.realdb.milvus.material.user.UserInfoMilvus6;
import net.hasor.dbvisitor.test.realdb.milvus.material.user.UserInfoMilvus6Mapper;
import org.junit.Before;

import static org.junit.Assert.*;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;

public class MilvusMapperContractTest extends AdapterContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }


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
        } catch (Exception e) {
            // Index likely exists
        }
    }

    @Before
    public void before() throws SQLException {
        try (Connection c = newAdapterConnection()) {
            JdbcTemplate jdbc = new JdbcTemplate(c);

            // Table 1 for Annotations
            initTable(jdbc, "tb_mapper_user_milvus", "CREATE TABLE IF NOT EXISTS tb_mapper_user_milvus (uid VARCHAR(64) PRIMARY KEY, name VARCHAR(64), loginName VARCHAR(64), loginPassword VARCHAR(64), v FLOAT_VECTOR(2)) WITH (consistency_level = 'Strong')");
            initIndex(jdbc, "idx_mapper_user_v", "tb_mapper_user_milvus", "CREATE INDEX idx_mapper_user_v ON TABLE tb_mapper_user_milvus (v) USING \"IVF_FLAT\" WITH (nlist = 128, metric_type = 'L2')");
            jdbc.execute("LOAD TABLE tb_mapper_user_milvus");
        }
    }

    private List<Float> sampleVector() {
        List<Float> v = new ArrayList<>();
        v.add(1.0f);
        v.add(0.5f);
        return v;
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_MAPPER_ANNOTATION_CRUD)
    public void using_mapper_api_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session session = config.newSession(newAdapterConnection())) {
            UserInfoMilvus1Mapper mapper = session.createMapper(UserInfoMilvus1Mapper.class);

            UserInfoMilvus1 user = new UserInfoMilvus1();
            user.setUid("u1");
            user.setName("nomo");
            user.setLoginName("nomo");
            user.setLoginPassword("123456");
            user.setV(sampleVector());

            // Insert
            int result = mapper.insertUser(user);
            assertEquals(1, result);

            // Query
            UserInfoMilvus1 loaded = mapper.selectUser("u1");
            assertNotNull(loaded);
            assertEquals("u1", loaded.getUid());

            // Delete
            int delResult = mapper.deleteUser("u1");
            assertEquals(1, delResult);
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_BASEMAPPER_CRUD)
    public void using_mapper_api_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session session = config.newSession(newAdapterConnection())) {
            UserInfoMilvus2Mapper mapper = session.createMapper(UserInfoMilvus2Mapper.class);

            UserInfoMilvus2 user = new UserInfoMilvus2();
            user.setUid("u2");
            user.setName("nomo2");
            user.setLoginName("nomo2");
            user.setLoginPassword("123456");
            user.setV(sampleVector());

            // Insert (BaseMapper method)
            int result = mapper.insert(user);
            assertEquals(1, result);

            // Query (BaseMapper method)
            UserInfoMilvus2 loaded = mapper.selectById("u2");
            assertNotNull(loaded);
            assertEquals("nomo2", loaded.getName());

            // Delete (BaseMapper method)
            int delResult = mapper.delete(loaded);
            assertEquals(1, delResult);
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_MAPPER_XML_CRUD)
    public void using_mapper_file_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session session = config.newSession(newAdapterConnection())) {
            UserInfoMilvus3Mapper mapper = session.createMapper(UserInfoMilvus3Mapper.class);

            UserInfoMilvus3 user = new UserInfoMilvus3();
            user.setUid("u3");
            user.setName("nomo3");
            user.setLoginName("nomo3");
            user.setLoginPassword("123456");
            user.setV(sampleVector());

            // Insert
            int result = mapper.insertUser(user);
            assertEquals(1, result);

            // Query
            List<UserInfoMilvus3> users = mapper.queryAll();
            assertNotNull(users);
            assertFalse(users.isEmpty());
            assertTrue(users.stream().anyMatch(u -> "u3".equals(u.getUid())));

            // Delete
            int delResult = mapper.deleteUser("u3");
            assertEquals(1, delResult);
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_MAPPER_XML_CRUD)
    public void using_mapper_api_4() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session session = config.newSession(newAdapterConnection())) {
            UserInfoMilvus4Mapper mapper = session.createMapper(UserInfoMilvus4Mapper.class);

            UserInfoMilvus1 user = new UserInfoMilvus1();
            user.setUid("u4");
            user.setName("nomo4");
            user.setLoginName("nomo4");
            user.setLoginPassword("123456");
            user.setV(sampleVector());

            // Insert
            int result = mapper.insertUser(user);
            assertEquals(1, result);

            // Query
            UserInfoMilvus1 loaded = mapper.findUser("u4");
            assertNotNull(loaded);
            assertEquals("nomo4", loaded.getName());

            // Delete
            int delResult = mapper.deleteUser("u4");
            assertEquals(1, delResult);
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_MAPPER_XML_RESULT_MAP)
    public void using_mapper_api_5() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session session = config.newSession(newAdapterConnection())) {
            UserInfoMilvus5Mapper mapper = session.createMapper(UserInfoMilvus5Mapper.class);

            UserInfoMilvus5 user = new UserInfoMilvus5();
            user.setUserId("u5");
            user.setUserName("nomo5");
            user.setAccount("nomo5");
            user.setPassword("123456");
            user.setVector(sampleVector());

            // Insert
            int result = mapper.insertUser(user);
            assertEquals(1, result);

            // Query
            List<UserInfoMilvus5> loadedList = mapper.queryAll();
            UserInfoMilvus5 loaded = loadedList.stream().filter(u -> "u5".equals(u.getUserId())).findFirst().orElse(null);

            assertNotNull(loaded);
            assertEquals("nomo5", loaded.getUserName());
            assertEquals("nomo5", loaded.getAccount());

            // Delete
            int delResult = mapper.deleteUser("u5");
            assertEquals(1, delResult);
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_MAPPER_ANNOTATION_MAP)
    public void using_mapper_api_6() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session session = config.newSession(newAdapterConnection())) {
            UserInfoMilvus6Mapper mapper = session.createMapper(UserInfoMilvus6Mapper.class);

            UserInfoMilvus6 user = new UserInfoMilvus6();
            user.setUserId("u6");
            user.setUserName("nomo6");
            user.setAccount("nomo6");
            user.setPassword("123456");
            user.setVector(sampleVector());

            // Insert
            int result = mapper.insert(user);
            assertEquals(1, result);

            // Query
            UserInfoMilvus6 loaded = mapper.selectById("u6");
            assertNotNull(loaded);
            assertEquals("nomo6", loaded.getUserName());
            assertEquals("nomo6", loaded.getAccount());

            // Delete
            int delResult = mapper.delete(loaded);
            assertEquals(1, delResult);
        }
    }
}
