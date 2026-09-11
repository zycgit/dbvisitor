/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.adapter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;

/** Command material for document-store type contracts; values remain JDBC parameters. */
public final class NativeBasicTypeFixture implements AutoCloseable {
    private final Map<String, String> tables = new LinkedHashMap<>();
    private final List<String> created = new ArrayList<>();
    private Connection connection;
    private JdbcTemplate jdbc;
    private boolean mongo;

    public JdbcTemplate open(String environment) throws SQLException {
        return open(environment, "basic_types_test", "basic_types_explicit_test");
    }

    public JdbcTemplate open(String environment, String... logicalTables) throws SQLException {
        this.connection = OneApiDataSourceManager.getConnection(environment);
        this.jdbc = new JdbcTemplate(this.connection);
        this.mongo = "mongo".equals(environment);
        String prefix = "nxn_types_" + UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < logicalTables.length; i++) {
            this.tables.put(logicalTables[i], prefix + "_" + i);
        }
        if (this.mongo) {
            this.jdbc.execute("use test");
        }
        for (String table : this.tables.values()) {
            this.jdbc.execute(this.mongo ? "db.createCollection('" + table + "')" : "PUT /" + table);
            this.created.add(table);
        }
        return this.jdbc;
    }

    public String insertCommand(String table, String columns) {
        String[] parameters = new String[columns.split(",").length];
        java.util.Arrays.fill(parameters, "?");
        return insertCommand(table, columns, parameters);
    }

    public String insertCommand(String table, String columns, String... parameters) {
        String[] names = columns.split(",");
        if (names.length != parameters.length) {
            throw new IllegalArgumentException("Each fixture field requires one parameter expression");
        }
        List<String> fields = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            fields.add("\"" + names[i].trim() + "\": " + parameters[i]);
        }
        String document = "{" + String.join(", ", fields) + "}";
        String physical = physicalTable(table);
        return this.mongo ? "test." + physical + ".insert(" + document + ")" : "POST /" + physical + "/_doc " + document;
    }

    public String selectCommand(String table, String columns) throws SQLException {
        String physical = physicalTable(table);
        if (this.mongo) {
            String projection = "";
            if (!"*".equals(columns)) {
                List<String> fields = new ArrayList<>();
                for (String field : columns.split(",")) {
                    fields.add("\"" + field.trim() + "\": 1");
                }
                projection = ", {" + String.join(", ", fields) + "}";
            }
            return "test." + physical + ".find({id: ?}" + projection + ")";
        }

        // Make fixture writes visible before search; this is not pagination or client-side filtering.
        this.jdbc.execute("POST /" + physical + "/_refresh");
        String source = "";
        if (!"*".equals(columns)) {
            List<String> fields = new ArrayList<>();
            for (String field : columns.split(",")) {
                fields.add("\"" + field.trim() + "\"");
            }
            source = ", \"_source\": [" + String.join(", ", fields) + "]";
        }
        return "POST /" + physical + "/_search {\"query\": {\"term\": {\"id\": ?}}" + source + "}";
    }

    private String physicalTable(String logical) {
        String physical = this.tables.get(logical);
        if (physical == null) {
            throw new IllegalArgumentException("Unknown fixture table: " + logical);
        }
        return physical;
    }

    @Override
    public void close() throws SQLException {
        try {
            for (String table : this.created) {
                this.jdbc.execute(this.mongo ? "test." + table + ".drop()" : "DELETE /" + table);
            }
        } finally {
            if (this.connection != null) {
                this.connection.close();
            }
        }
    }
}
