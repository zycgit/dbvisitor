/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.driver;
import java.sql.SQLException;

public interface TransactionSupport {
    boolean supportIsolation(int value);

    void setIsolation(int value) throws SQLException;

    int getIsolation();

    void setAutoCommit(boolean value) throws SQLException;

    boolean isAutoCommit();

    void commit() throws SQLException;

    void rollback() throws SQLException;
}
