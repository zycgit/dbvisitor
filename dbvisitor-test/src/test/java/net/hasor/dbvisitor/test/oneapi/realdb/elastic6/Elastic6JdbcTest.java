package net.hasor.dbvisitor.test.oneapi.realdb.elastic6;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.oneapi.config.OneApiDataSourceManager;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Elastic6JdbcTest {
    @Test
    public void using_jdbc_1() throws Exception {
        try (Connection c = OneApiDataSourceManager.getConnection("es7")) {
            JdbcTemplate jdbc = new JdbcTemplate(c);

            // 1. clean
            try {
                jdbc.execute("DELETE /test_user_info");
            } catch (Exception e) {
                // ignore
            }

            // 2. insert
            jdbc.execute("POST /test_user_info/_doc/1 { \"name\": \"mali\", \"age\": 26 }");
            jdbc.execute("POST /test_user_info/_doc/2 { \"name\": \"dative\", \"age\": 32 }");
            jdbc.execute("POST /test_user_info/_doc/3 { \"name\": \"jon wes\", \"age\": 41 }");
            jdbc.execute("POST /test_user_info/_refresh");

            // 3. query list
            List<Map<String, Object>> list = jdbc.queryForList("POST /test_user_info/_search");
            assertEquals(3, list.size());

            // 4. query condition
            Map<String, Object> mali = jdbc.queryForMap("POST /test_user_info/_search { \"query\": { \"match\": { \"name\": \"mali\" } } }");
            assertEquals("mali", mali.get("name"));
            assertEquals(26, Integer.parseInt(mali.get("age").toString()));

            // 5. update
            jdbc.execute("POST /test_user_info/_update/1 { \"doc\": { \"age\": 27 } }");
            jdbc.execute("POST /test_user_info/_refresh");
            mali = jdbc.queryForMap("POST /test_user_info/_search { \"query\": { \"match\": { \"name\": \"mali\" } } }");
            assertEquals(27, Integer.parseInt(mali.get("age").toString()));

            // 6. remove
            jdbc.execute("DELETE /test_user_info/_doc/1");
            jdbc.execute("POST /test_user_info/_refresh");
            list = jdbc.queryForList("POST /test_user_info/_search { \"query\": { \"match\": { \"name\": \"mali\" } } }");
            assertTrue(list.isEmpty());

            list = jdbc.queryForList("POST /test_user_info/_search");
            assertEquals(2, list.size());
        }
    }

    @Test
    public void using_jdbc_2() throws Exception {
        try (Connection c = OneApiDataSourceManager.getConnection("es7")) {
            JdbcTemplate jdbc = new JdbcTemplate(c);

            // 1. clean
            try {
                jdbc.execute("DELETE /test_user_info_2");
            } catch (Exception e) {
                // ignore
            }

            // 2. insert
            jdbc.execute("POST /test_user_info_2/_doc/1 { \"name\": \"mali\", \"age\": 26 }");
            jdbc.execute("POST /test_user_info_2/_doc/2 { \"name\": \"dative\", \"age\": 32 }");
            jdbc.execute("POST /test_user_info_2/_refresh");

            // 3. count
            int countInt = jdbc.queryForInt("POST /test_user_info_2/_count");
            assertEquals(2, countInt);
        }
    }
}
