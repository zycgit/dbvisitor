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
import java.util.List;

public interface AdapterCursor extends AutoCloseable {
    List<JdbcColumn> columns();

    boolean next() throws SQLException;

    Object column(int column) throws IOException, SQLException;

    int batchSize();

    void close() throws IOException;

    List<String> warnings();

    void clearWarnings();

    boolean isPending();

    boolean isClose();
}
