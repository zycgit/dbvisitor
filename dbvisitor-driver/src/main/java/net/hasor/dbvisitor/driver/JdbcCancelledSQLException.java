package net.hasor.dbvisitor.driver;

import java.sql.SQLException;

public class JdbcCancelledSQLException extends SQLException {
    public JdbcCancelledSQLException(String message, String state) {
        super(message, state);
    }
}