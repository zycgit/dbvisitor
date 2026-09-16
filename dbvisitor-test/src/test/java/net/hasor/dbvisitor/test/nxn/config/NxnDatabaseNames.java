/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.nxn.config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Allocates numbered test databases without adopting or deleting pre-existing databases. */
public final class NxnDatabaseNames {
    private static final Map<String, Long> SEQUENCES = new HashMap<>();

    private NxnDatabaseNames() {
    }

    public static synchronized String create(Connection admin, String datasource) throws SQLException {
        if (!datasource.matches("[a-z][a-z0-9]*")) {
            throw new IllegalArgumentException("Invalid datasource name: " + datasource);
        }
        String prefix = "dbv_nxn_" + datasource + "_";
        long number = SEQUENCES.getOrDefault(prefix, 0L);
        try (Statement statement = admin.createStatement()) {
            // Observe the server on each allocation: IDE runs or another checkout may share it.
            try (ResultSet existing = statement.executeQuery("SHOW DATABASES")) {
                while (existing.next()) {
                    String name = existing.getString(1);
                    if (name != null && name.startsWith(prefix)) {
                        String suffix = name.substring(prefix.length());
                        if (suffix.matches("[0-9]{1,12}")) {
                            number = Math.max(number, Long.parseLong(suffix));
                        }
                    }
                }
            }
            String database = prefix + String.format(Locale.ROOT, "%06d", number + 1);
            SEQUENCES.put(prefix, number + 1);
            // No IF NOT EXISTS: a competing creator must fail, never share ownership.
            statement.executeUpdate("CREATE DATABASE " + database);
            System.out.println("NxN fixture created database " + database);
            return database;
        }
    }
}
