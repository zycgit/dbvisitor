package net.hasor.dbvisitor.adapter.mongo;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import com.mongodb.client.MongoCursor;
import net.hasor.dbvisitor.driver.AdapterCursor;
import net.hasor.dbvisitor.driver.JdbcColumn;
import org.bson.Document;

class MongoAdapterCursor implements AdapterCursor {
    private final MongoCursor<Document> cursor;
    private final List<JdbcColumn>      columns;
    private       Document              current;
    private       boolean               closed;

    MongoAdapterCursor(MongoCursor<Document> cursor, String catalog, String schema, String table) {
        this.cursor = cursor;
        this.columns = Collections.singletonList(new JdbcColumn("document", "struct", table, catalog, schema));
    }

    @Override
    public List<JdbcColumn> columns() {
        return this.columns;
    }

    @Override
    public boolean next() {
        if (this.cursor.hasNext()) {
            this.current = this.cursor.next();
            return true;
        }
        return false;
    }

    @Override
    public Object column(int column) throws SQLException {
        if (column == 1) {
            return this.current;
        }
        throw new SQLException("Column index out of range: " + column);
    }

    @Override
    public int batchSize() {
        return -1;
    }

    @Override
    public void close() {
        this.closed = true;
        this.cursor.close();
    }

    @Override
    public List<String> warnings() {
        return Collections.emptyList();
    }

    @Override
    public void clearWarnings() {
    }

    @Override
    public boolean isPending() {
        return false;
    }

    @Override
    public boolean isClose() {
        return this.closed;
    }
}
