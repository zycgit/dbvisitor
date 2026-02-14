package net.hasor.dbvisitor.driver;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class MockAdapterFactory implements AdapterFactory {
    @Override
    public String getAdapterName() {
        return "mock";
    }

    @Override
    public String[] getPropertyNames() {
        return new String[0];
    }

    @Override
    public TypeSupport createTypeSupport(Properties properties) {
        return new AdapterTypeSupport(properties);
    }

    @Override
    public AdapterConnection createConnection(Connection owner, String jdbcUrl, Properties properties) throws SQLException {
        String user = properties.getProperty("user");
        return new MockAdapterConnection(jdbcUrl, user);
    }
}
