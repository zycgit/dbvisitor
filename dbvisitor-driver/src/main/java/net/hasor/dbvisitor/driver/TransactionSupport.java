package net.hasor.dbvisitor.driver;

import java.sql.SQLException;

public interface TransactionSupport {

    boolean supportIsolation(int value);

    void setIsolation(int value);

    int getIsolation();

    void setAutoCommit(boolean value);

    boolean isAutoCommit();

    void commit() throws SQLException;

    void rollback() throws SQLException;
}
