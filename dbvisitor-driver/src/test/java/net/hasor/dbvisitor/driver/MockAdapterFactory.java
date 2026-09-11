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
