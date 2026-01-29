package net.hasor.dbvisitor.adapter.milvus;

import java.lang.reflect.InvocationHandler;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import net.hasor.dbvisitor.driver.JdbcDriver;

public class AbstractJdbcTest {

    public Connection milvusConnection() throws SQLException {
        Properties prop = new Properties();
        prop.setProperty(MilvusKeys.CUSTOM_MILVUS, MilvusCustomClient.class.getName());
        return new JdbcDriver().connect("jdbc:dbvisitor:milvus://xxxxxx:19530", prop);
    }

    public InvocationHandler createInvocationHandler(String methodName, TestInvocationHandler handler) {
        return (proxy, method, args) -> {
            if (method.getName().equals(methodName)) {
                return handler.invoke(method.getName(), args);
            }
            return null;
        };
    }

    public InvocationHandler createInvocationHandler(String[] methodName, TestInvocationHandler handler) {
        return (proxy, method, args) -> {
            for (String c : methodName) {
                if (method.getName().equals(c)) {
                    return handler.invoke(method.getName(), args);
                }
            }
            return null;
        };
    }
}
