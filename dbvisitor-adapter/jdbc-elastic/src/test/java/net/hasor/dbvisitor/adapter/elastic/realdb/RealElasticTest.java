package net.hasor.dbvisitor.adapter.elastic.realdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
import org.junit.Ignore;
import org.junit.Test;

public class RealElasticTest {
    private static final String MONGO_URL = "jdbc:dbvisitor:elastic://127.0.0.1:17017/admin";

    @Test
    @Ignore("Requires real ElasticDB")
    public void test_01() throws Exception {
        Properties props = new Properties();
        props.setProperty("username", "root");
        props.setProperty("password", "123456");

        try (Connection c = DriverManager.getConnection(MONGO_URL, props)) {
            // 1. clean
            try (Statement s = c.createStatement()) {
                try {
                    s.execute("test.user_info.drop()");
                } catch (Exception e) {
                    // ignore
                }
            }

            // 2. insert
            try (Statement s = c.createStatement()) {
                s.execute("test.user_info.insert({name: 'mali', age: 26})");
            }
            try (Statement s = c.createStatement()) {
                s.execute("test.user_info.insert({name: 'dative', age: 32})");
            }
            try (Statement s = c.createStatement()) {
                s.execute("test.user_info.insert({name: 'jon wes', age: 41})");
            }

            // 3. query
            try (Statement s = c.createStatement()) {
                try (ResultSet rs = s.executeQuery("test.user_info.find({name: 'mali'})")) {
                    if (rs.next()) {
                        String json = rs.getString("_JSON");
                        if (!json.contains("\"name\": \"mali\"") || !json.contains("\"age\": 26")) {
                            throw new RuntimeException("data not match: " + json);
                        }
                    } else {
                        throw new RuntimeException("no data found");
                    }
                }
            }
        }
    }
}
