/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.nxn.env;

public enum DataSourceId {
    PG("pg"),
    MYSQL("mysql"),
    H2("h2"),
    MSSQL("mssql"),
    ORACLE("oracle"),
    DB2("db2"),
    CLICKHOUSE("clickhouse"),
    REDIS("redis"),
    MONGO("mongo"),
    ELASTIC6("es6"),
    ELASTIC7("es7"),
    MILVUS("milvus");

    private final String env;

    DataSourceId(String env) {
        this.env = env;
    }

    public String env() {
        return this.env;
    }
}
