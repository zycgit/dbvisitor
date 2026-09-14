/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.driver;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Selects columns from keys actually returned by the adapter, without buffering rows. */
class GeneratedKeyCursor implements AdapterCursor {
    private final AdapterCursor    source;
    private final List<JdbcColumn> columns = new ArrayList<>();
    private final int[]            indexes;

    public GeneratedKeyCursor(AdapterCursor source, String[] names) throws SQLException {
        this.source = source;
        this.indexes = new int[names.length];
        List<JdbcColumn> available = source.columns();
        for (int i = 0; i < names.length; i++) {
            int found = -1;
            // Prefer exact names; otherwise follow JDBC's case-insensitive column lookup.
            for (int j = 0; j < available.size(); j++) {
                if (available.get(j).name.equals(names[i])) {
                    found = j;
                    break;
                }
            }
            if (found < 0) {
                for (int j = 0; j < available.size(); j++) {
                    if (available.get(j).name.equalsIgnoreCase(names[i])) {
                        found = j;
                        break;
                    }
                }
            }
            if (found < 0) {
                throw new SQLException("Generated key column not returned by adapter: " + names[i], "S0022");
            }
            this.indexes[i] = found + 1;
            this.columns.add(available.get(found));
        }
    }

    @Override
    public List<JdbcColumn> columns() {
        return this.columns;
    }

    @Override
    public boolean next() throws SQLException {
        return this.source.next();
    }

    @Override
    public Object column(int column) throws IOException, SQLException {
        return this.source.column(this.indexes[column - 1]);
    }

    @Override
    public int batchSize() {
        return this.source.batchSize();
    }

    @Override
    public void close() throws IOException {
        this.source.close();
    }

    @Override
    public List<String> warnings() {
        return this.source.warnings();
    }

    @Override
    public void clearWarnings() {
        this.source.clearWarnings();
    }

    @Override
    public boolean isPending() {
        return this.source.isPending();
    }

    @Override
    public boolean isClose() {
        return this.source.isClose();
    }
}
