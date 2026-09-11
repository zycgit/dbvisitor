/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.driver;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public interface AdapterFactory {
    String getAdapterName();

    String[] getPropertyNames();

    TypeSupport createTypeSupport(Properties properties);

    AdapterConnection createConnection(Connection owner, String jdbcUrl, Properties properties) throws SQLException;
}
