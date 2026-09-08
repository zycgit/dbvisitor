/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.driver;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class AdapterResultCursor implements AdapterCursor {
    private final    AdapterRequest             request;
    private final    List<JdbcColumn>           columns;
    private final    Map<Integer, String>       columnIdMap;
    //
    private final    List<String>               warnings;
    private final    Queue<Map<String, Object>> rowSet;
    private volatile Map<String, Object>        currentRow;
    private volatile boolean                    closed;
    private volatile boolean                    pending;

    public AdapterResultCursor(AdapterRequest request, List<JdbcColumn> columns) {
        this.request = request;
        this.columns = columns;
        this.columnIdMap = new HashMap<>();
        this.warnings = new ArrayList<>();
        this.rowSet = new ConcurrentLinkedQueue<>();
        this.closed = false;
        this.pending = true;

        for (int i = 0; i < columns.size(); i++) {
            JdbcColumn column = columns.get(i);
            this.columnIdMap.put(i + 1, column.name);
        }
    }

    @Override
    public List<JdbcColumn> columns() {
        return this.columns;
    }

    @Override
    public synchronized boolean next() throws SQLException {
        long remaining = TimeUnit.SECONDS.toNanos(this.request.getTimeoutSec());
        long deadline = System.nanoTime() + remaining;
        while (!this.closed && this.pending && this.rowSet.isEmpty()) {
            try {
                if (this.request.getTimeoutSec() == 0) {
                    this.wait();
                } else {
                    if (remaining <= 0) {
                        throw new SQLTimeoutException("cursor wait timeout.");
                    }

                    TimeUnit.NANOSECONDS.timedWait(this, remaining);
                    remaining = deadline - System.nanoTime();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SQLException("Interrupted while waiting for cursor data.", e);
            }
        }

        if (this.closed) {
            throw new SQLException("cursor is closed.");
        }

        if (this.rowSet.isEmpty()) {
            this.currentRow = null;
            return false;
        }

        this.currentRow = this.rowSet.poll();
        return true;
    }

    @Override
    public int batchSize() {
        return this.request.fetchSize;
    }

    @Override
    public synchronized void close() throws IOException {
        this.closed = true;
        this.pending = false;
        this.rowSet.clear();
        this.currentRow = null;
        this.notifyAll();
    }

    public synchronized void pushData(Map<String, Object> row) throws SQLException {
        if (this.closed) {
            throw new SQLException("cursor is closed.");
        }
        if (!this.pending) {
            throw new SQLException("cursor is finish.");
        }

        this.rowSet.offer(Objects.requireNonNull(row, "row is null."));
        this.notifyAll();
    }

    public synchronized void pushFinish() {
        this.pending = false;
        this.notifyAll();
    }

    @Override
    public Object column(int column) throws SQLException {
        if (this.currentRow == null) {
            throw new SQLException("empty ResultSet or After end of ResultSet");
        }
        if (!this.columnIdMap.containsKey(column)) {
            throw new SQLException("Before start of ResultSet.");
        }

        return this.currentRow.get(this.columnIdMap.get(column));
    }

    @Override
    public List<String> warnings() {
        return this.warnings;
    }

    @Override
    public void clearWarnings() {
        this.warnings.clear();
    }

    @Override
    public boolean isPending() {
        return this.pending;
    }

    @Override
    public boolean isClose() {
        return this.closed;
    }
}