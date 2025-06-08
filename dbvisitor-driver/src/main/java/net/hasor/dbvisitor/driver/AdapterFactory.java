package net.hasor.dbvisitor.driver;

import java.sql.SQLException;
import java.util.Properties;

public interface AdapterFactory {

    String[] getPropertyNames();

    TypeSupport getTypeSupport();

    AdapterConnection createConnection(String jdbcUrl, Properties properties) throws SQLException;
}