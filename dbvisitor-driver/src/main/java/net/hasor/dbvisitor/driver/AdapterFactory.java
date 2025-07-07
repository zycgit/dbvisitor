package net.hasor.dbvisitor.driver;

import java.sql.SQLException;
import java.util.Properties;

public interface AdapterFactory {
    String getAdapterName();

    String[] getPropertyNames();

    TypeSupport createTypeSupport(Properties properties);

    AdapterConnection createConnection(String jdbcUrl, Properties properties) throws SQLException;
}