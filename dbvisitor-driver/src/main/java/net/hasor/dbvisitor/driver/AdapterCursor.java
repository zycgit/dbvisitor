package net.hasor.dbvisitor.driver;

import java.io.Closeable;
import java.sql.SQLException;
import java.util.List;

public interface AdapterCursor extends Closeable {

    List<JdbcColumn> columns();

    boolean next() throws SQLException;

    Object column(int column);

    int batchSize();

    void close();

    List<String> warnings();

    void clearWarnings();
}