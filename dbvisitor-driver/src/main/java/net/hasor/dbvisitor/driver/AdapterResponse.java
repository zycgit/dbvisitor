package net.hasor.dbvisitor.driver;

import net.hasor.cobble.concurrent.future.BasicFuture;

import java.util.List;

public class AdapterResponse extends BasicFuture<AdapterResponse> {

    public List<JdbcColumn> getColumnList() {
        return null;
    }

    public boolean isResult() {
        return false;
    }

    public AdapterCursor toCursor() {
        return null;
    }

    public long getUpdateCount() {
        return 0;
    }
}
