package net.hasor.dbvisitor.driver;

import net.hasor.cobble.ArrayUtils;

import java.util.Collections;
import java.util.List;

class AdapterMemoryCursor implements AdapterCursor {
    private List<JdbcColumn> columns;
    private Object[][]       data;
    private int              row = -1;

    AdapterMemoryCursor(List<JdbcColumn> info, Object[][] data) {
        this.columns = info;
        this.data = data;
    }

    @Override
    public List<JdbcColumn> columns() {
        return this.columns;
    }

    @Override
    public boolean next() {
        if (this.data == null) {
            return false;
        }

        if (!ArrayUtils.isEmpty(this.data) && this.row < this.data.length - 1) {
            this.row++;
            return true;
        }
        return false;
    }

    @Override
    public Object column(int column) {
        if (this.data == null) {
            return null;
        }

        return this.data[this.row][column];
    }

    @Override
    public int batchSize() {
        if (this.data == null) {
            return 0;
        }

        return ArrayUtils.isEmpty(this.data) ? 0 : this.data.length;
    }

    @Override
    public void close() {
        this.data = null;
        this.columns = Collections.emptyList();
    }

    @Override
    public List<String> warnings() {
        return Collections.emptyList();
    }

    @Override
    public void clearWarnings() {
    }
}