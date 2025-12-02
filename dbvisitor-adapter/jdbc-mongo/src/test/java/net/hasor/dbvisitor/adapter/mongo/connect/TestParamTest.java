package net.hasor.dbvisitor.adapter.mongo.connect;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import net.hasor.dbvisitor.adapter.mongo.MongoCommandInterceptor;
import net.hasor.dbvisitor.adapter.mongo.MongoCustomJedis;
import net.hasor.dbvisitor.adapter.mongo.MongoKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;

public class TestParamTest {

    public Connection redisConnection() throws SQLException {
        Properties prop = new Properties();
        prop.setProperty(MongoKeys.INTERCEPTOR, MongoCommandInterceptor.class.getName());
        prop.setProperty(MongoKeys.CUSTOM_MONGO, MongoCustomJedis.class.getName());
        return new JdbcDriver().connect("jdbc:dbvisitor:mongo://xxxxxx", prop);
    }
}
