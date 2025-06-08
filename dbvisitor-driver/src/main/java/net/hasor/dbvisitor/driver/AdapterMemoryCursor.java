package net.hasor.dbvisitor.driver;

import net.hasor.cobble.ArrayUtils;

import java.util.Collections;
import java.util.List;

class AdapterMemoryCursor implements AdapterCursor {
    private       AdapterDataContainer container;
    private final List<JdbcColumn>     columns;
    private final Object[][]           data;
    private       int                  row = -1;

    AdapterMemoryCursor(List<JdbcColumn> info, Object[][] data, AdapterDataContainer container) {
        this.columns = info;
        this.data = data;
        this.container = container;
    }

    @Override
    public List<JdbcColumn> columns() {
        return this.columns;
    }

    @Override
    public boolean next() {
        if (!ArrayUtils.isEmpty(this.data) && this.row < this.data.length - 1) {
            this.row++;
            return true;
        }
        return false;
    }

    @Override
    public Object column(int column) {
        return this.data[this.row][column];
    }

    @Override
    public int batchSize() {
        return ArrayUtils.isEmpty(this.data) ? 0 : this.data.length;
    }

    @Override
    public void close() {
        if (this.container != null) {
            this.container.resultSetWasClosed(this);
        }
        this.container = null;
    }

    @Override
    public List<String> warnings() {
        return Collections.emptyList();
    }

    @Override
    public void clearWarnings() {
    }
}