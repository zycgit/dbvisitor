package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.test.config.OneApiDataSourceManager;
import org.junit.Test;

public class MongoJdbcTest {
    @Test
    public void using_jdbc_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Connection c = OneApiDataSourceManager.getConnection("mongo")) {
            JdbcTemplate jdbc = new JdbcTemplate(c);

            // 1. clean
            try {
                jdbc.execute("test.user_info.drop()");
            } catch (Exception e) {
                // ignore
            }

            // 2. insert
            jdbc.execute("test.user_info.insert({name: 'mali', age: 26})");
            jdbc.execute("test.user_info.insert({name: 'dative', age: 32})");
            jdbc.execute("test.user_info.insert({name: 'jon wes', age: 41})");

            // 3. query list
            List<Map<String, Object>> list = jdbc.queryForList("test.user_info.find()");
            assert list.size() == 3;

            // 4. query condition
            Map<String, Object> mali = jdbc.queryForMap("test.user_info.find({name: 'mali'})");
            String json = (String) mali.get("_JSON");
            assert json.contains("\"name\": \"mali\"");
            assert json.contains("\"age\": 26");

            // 5. update
            jdbc.execute("test.user_info.update({name: 'mali'}, {$set: {age: 27}})");
            mali = jdbc.queryForMap("test.user_info.find({name: 'mali'})");
            json = (String) mali.get("_JSON");
            assert json.contains("\"age\": 27");

            // 6. remove
            jdbc.execute("test.user_info.remove({name: 'mali'})");
            list = jdbc.queryForList("test.user_info.find({name: 'mali'})");
            assert list.isEmpty();

            list = jdbc.queryForList("test.user_info.find()");
            assert list.size() == 2;
        }
    }

    @Test
    public void using_jdbc_2() throws Exception {
        try (Connection c = OneApiDataSourceManager.getConnection("mongo")) {
            JdbcTemplate jdbc = new JdbcTemplate(c);

            // 1. clean
            try {
                jdbc.execute("test.user_info.drop()");
            } catch (Exception e) {
                // ignore
            }

            // 2. insert
            jdbc.execute("test.user_info.insert([{name: 'mali', age: 26}, {name: 'dative', age: 32}])");

            // 3. count
            int count = jdbc.queryForInt("test.user_info.count()");
            assert count == 2;
        }
    }
}
