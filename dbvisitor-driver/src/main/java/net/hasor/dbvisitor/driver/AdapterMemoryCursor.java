/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.driver;
import java.util.Collections;
import java.util.List;
import net.hasor.cobble.ArrayUtils;

class AdapterMemoryCursor implements AdapterCursor {
    private boolean          closed;
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

        return this.data[this.row][column - 1];
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
        this.closed = true;
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

    @Override
    public boolean isPending() {
        return false;
    }

    @Override
    public boolean isClose() {
        return this.closed;
    }
}
