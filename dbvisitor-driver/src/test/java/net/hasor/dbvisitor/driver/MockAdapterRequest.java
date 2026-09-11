/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.driver;

public class MockAdapterRequest extends AdapterRequest {
    private final String sql;

    public MockAdapterRequest(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }
}
