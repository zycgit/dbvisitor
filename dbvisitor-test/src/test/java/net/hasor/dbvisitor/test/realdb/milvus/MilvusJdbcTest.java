package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.config.OneApiDataSourceManager;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusJdbcTest {
    private Connection   connection;
    private JdbcTemplate jdbcTemplate;

    @Before
    public void before() {
        try {
            this.connection = OneApiDataSourceManager.getConnection("milvus");
            this.jdbcTemplate = new JdbcTemplate(this.connection);
            try {
                this.jdbcTemplate.execute("DROP TABLE IF EXISTS tb_crud_user");
                this.jdbcTemplate.execute("DROP TABLE IF EXISTS tb_vector_type");
            } catch (Exception e) {
                // ignore
            }
        } catch (Throwable e) {
            Assume.assumeNoException("Milvus connection failed, skipping test", e);
        }
    }

    @After
    public void after() {
        if (this.jdbcTemplate != null) {
            try {
                this.jdbcTemplate.execute("DROP TABLE IF EXISTS tb_crud_user");
                this.jdbcTemplate.execute("DROP TABLE IF EXISTS tb_vector_type");
            } catch (Exception e) {
                // ignore
            }
        }
        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    @Test
    public void testCrud() throws SQLException {
        // 1. Create Table
        this.jdbcTemplate.execute("CREATE TABLE tb_crud_user (id INT64 PRIMARY KEY, name VARCHAR(20), age INT64, v FLOAT_VECTOR(2))");

        // 2. Insert
        this.jdbcTemplate.execute("INSERT INTO tb_crud_user (id, name, age, v) VALUES (1, 'User1', 10, [0.1, 0.1])");
        this.jdbcTemplate.execute("INSERT INTO tb_crud_user (id, name, age, v) VALUES (2, 'User2', 20, [0.2, 0.2])");
        this.jdbcTemplate.execute("INSERT INTO tb_crud_user (id, name, age, v) VALUES (3, 'User3', 30, [0.3, 0.3])");

        // 2.1 Create Index & Load (Required for Query)
        this.jdbcTemplate.execute("CREATE INDEX idx_v_crud ON TABLE tb_crud_user (v) USING \"IVF_FLAT\" WITH (nlist = 1024, metric_type = 'L2')");
        this.jdbcTemplate.execute("LOAD TABLE tb_crud_user");

        // 3. Query All
        List<Map<String, Object>> list = jdbcTemplate.queryForList("SELECT * FROM tb_crud_user LIMIT 10");
        assertEquals(3, list.size());

        // 4. Query Single
        Map<String, Object> user1 = jdbcTemplate.queryForMap("SELECT * FROM tb_crud_user WHERE id = 1");
        assertEquals(1L, Long.parseLong(user1.get("id").toString()));
        assertEquals("User1", user1.get("name"));

        // 5. Update
        this.jdbcTemplate.execute("UPDATE tb_crud_user SET name = 'User2_Updated' WHERE id = 2");

        // 6. Verify Update
        Map<String, Object> user2 = jdbcTemplate.queryForMap("SELECT * FROM tb_crud_user WHERE id = 2");
        assertEquals("User2_Updated", user2.get("name"));

        // 7. Delete
        this.jdbcTemplate.execute("DELETE FROM tb_crud_user WHERE id = 1");

        // 8. Verify Delete
        list = jdbcTemplate.queryForList("SELECT * FROM tb_crud_user LIMIT 10");
        assertEquals(2, list.size());
    }

    @Test
    public void testVectorType() throws SQLException {
        this.jdbcTemplate.execute("CREATE TABLE tb_vector_type (id INT64 PRIMARY KEY, v FLOAT_VECTOR(2))");
        this.jdbcTemplate.execute("INSERT INTO tb_vector_type (id, v) VALUES (1, [0.1, 0.2])");
        this.jdbcTemplate.execute("CREATE INDEX idx_vec_type ON TABLE tb_vector_type (v) USING \"IVF_FLAT\" WITH (nlist = 1024, metric_type = 'L2')");
        this.jdbcTemplate.execute("LOAD TABLE tb_vector_type");

        Map<String, Object> row = jdbcTemplate.queryForMap("SELECT * FROM tb_vector_type LIMIT 1");
        Object v = row.get("v");

        assertNotNull("Vector value should not be null", v);
        assertTrue("Vector value should be instance of List, but was " + v.getClass().getName(), v instanceof java.util.List);

        List<?> list = (List<?>) v;
        assertEquals(2, list.size());
        assertEquals(0.1f, ((Number) list.get(0)).floatValue(), 0.0001f);
        assertEquals(0.2f, ((Number) list.get(1)).floatValue(), 0.0001f);
    }
}
