/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.unit.support;

import java.lang.reflect.Proxy;
import java.sql.Connection;

/** Configuration tests must fail if they accidentally invoke JDBC. */
public final class NoDatabaseConnection {
    private NoDatabaseConnection() {
    }

    public static Connection create() {
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[] { Connection.class }, (proxy, method, args) -> {
            if ("close".equals(method.getName())) {
                return null;
            }
            throw new AssertionError("Configuration tests must not call Connection." + method.getName());
        });
    }
}
