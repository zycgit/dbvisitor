/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus.commands.query;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import net.hasor.dbvisitor.adapter.milvus.MilvusRequest;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusRetry;
import net.hasor.dbvisitor.adapter.milvus.mapping.MilvusVectorCodec;
import net.hasor.dbvisitor.driver.AdapterCursor;
import net.hasor.dbvisitor.driver.AdapterType;
import net.hasor.dbvisitor.driver.JdbcColumn;
import net.hasor.dbvisitor.driver.JdbcErrorCode;

/** JDBC pull cursor: retains one page and owns the SDK iterator opened for this SQL execution. */
public final class MilvusResultCursor implements AdapterCursor {
    @FunctionalInterface
    interface SourceFactory {
        PageSource open() throws SQLException;
    }

    record PageSource(Supplier<List<Map<String, Object>>> read, Runnable close) {
    }

    private final    MilvusRequest                 request;
    private final    List<JdbcColumn>              columns;
    private final    int                           batchSize;
    private final    Long                          limit;
    private final    ReentrantLock                 lock     = new ReentrantLock();
    private final    List<String>                  warnings = new ArrayList<>();
    private          PageSource                    source;
    private          Iterator<Map<String, Object>> page     = Collections.emptyIterator();
    private          Map<String, Object>           currentRow;
    private          long                          offset;
    private          long                          returned;
    private volatile boolean                       exhausted;
    private volatile boolean                       closed;
    private volatile boolean                       cancelled;
    private volatile SQLException                  failure;

    MilvusResultCursor(MilvusRequest request, List<JdbcColumn> columns, int batchSize, Long limit, long offset, SourceFactory factory) throws SQLException {
        this.request = request;
        this.columns = Collections.unmodifiableList(new ArrayList<>(columns));
        this.batchSize = batchSize;
        this.limit = limit;
        this.offset = offset;
        request.checkActive();
        // Establish the query before later SQL statements execute; SDK constructors may prime their first page.
        this.source = factory.open();
        try {
            request.addCursor(this);
            request.checkActive();
        } catch (SQLException e) {
            try {
                finish();
            } catch (RuntimeException closeFailure) {
                e.addSuppressed(closeFailure);
            }
            throw e;
        }
    }

    @Override
    public boolean next() throws SQLException {
        lock.lock();
        try {
            checkReadable();
            currentRow = null;
            if (exhausted) {
                return false;
            }
            request.checkActive();
            while (true) {
                checkReadable();
                request.checkActive();
                if (!page.hasNext()) {
                    List<Map<String, Object>> rows = source.read.get();
                    checkReadable();
                    request.checkActive();
                    if (rows == null || rows.isEmpty()) {
                        finish();
                        return false;
                    }
                    page = rows.iterator();
                }
                Map<String, Object> row = page.next();
                if (offset > 0) {
                    offset--;
                    continue;
                }
                currentRow = normalize(row);
                returned++;
                if (limit != null && returned >= limit) {
                    finish();
                }
                return true;
            }
        } catch (SQLException | RuntimeException e) {
            failure = MilvusRetry.sqlException(e);
            try {
                finish();
            } catch (RuntimeException closeFailure) {
                failure.addSuppressed(closeFailure);
            }
            currentRow = null;
            throw failure;
        } catch (Error e) {
            try {
                finish();
            } catch (RuntimeException closeFailure) {
                e.addSuppressed(closeFailure);
            }
            throw e;
        } finally {
            try {
                if (closed || cancelled) {
                    finish();
                }
            } finally {
                lock.unlock();
            }
        }
    }

    private Map<String, Object> normalize(Map<String, Object> row) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (JdbcColumn column : columns) {
            Object value = row.get(column.name);
            if ("JSON".equals(column.type) && value instanceof com.google.gson.JsonNull) {
                value = null;
            }
            if (AdapterType.Bytes.equals(column.type) && value instanceof java.nio.ByteBuffer) {
                value = MilvusVectorCodec.sdkBytes((java.nio.ByteBuffer) value);
            }
            if (AdapterType.Array.equals(column.type) && value instanceof List<?> values && (AdapterType.Byte.equals(column.elementType) || AdapterType.Short.equals(column.elementType))) {
                List<Object> converted = new ArrayList<>(values.size());
                for (Object item : values) {
                    if (item == null) {
                        converted.add(null);
                    } else if (AdapterType.Byte.equals(column.elementType)) {
                        converted.add(((Number) item).byteValue());
                    } else {
                        converted.add(((Number) item).shortValue());
                    }
                }
                value = converted;
            }
            if (value instanceof Number) {
                if (AdapterType.Byte.equals(column.type)) {
                    value = ((Number) value).byteValue();
                } else if (AdapterType.Short.equals(column.type)) {
                    value = ((Number) value).shortValue();
                }
            }
            result.put(column.name, value);
        }
        return result;
    }

    private void checkReadable() throws SQLException {
        if (failure != null) {
            throw failure;
        }
        if (closed) {
            throw new SQLException("Milvus result cursor is closed.");
        }
    }

    private void finish() {
        exhausted = true;
        page = Collections.emptyIterator();
        PageSource owned = source;
        source = null;
        try {
            if (owned != null) {
                owned.close.run();
            }
        } finally {
            request.removeCursor(this);
        }
    }

    public void cancel() {
        failure = new SQLException("Operation cancelled.", JdbcErrorCode.SQL_STATE_IS_CANCELLED);
        cancelled = true;
        // Do not block Statement.cancel() on an in-flight SDK call; next() releases it on return.
        if (lock.tryLock()) {
            try {
                finish();
            } catch (RuntimeException closeFailure) {
                failure.addSuppressed(closeFailure);
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public void close() throws IOException {
        closed = true;
        if (lock.tryLock()) {
            try {
                currentRow = null;
                finish();
            } catch (RuntimeException closeFailure) {
                throw new IOException("Failed to close Milvus result iterator.", closeFailure);
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public Object column(int column) throws SQLException {
        if (currentRow == null || column < 1 || column > columns.size()) {
            throw new SQLException("No current Milvus result column: " + column);
        }
        return currentRow.get(columns.get(column - 1).name);
    }

    @Override
    public List<JdbcColumn> columns() {
        return columns;
    }

    @Override
    public int batchSize() {
        return batchSize;
    }

    @Override
    public List<String> warnings() {
        return warnings;
    }

    @Override
    public void clearWarnings() {
        warnings.clear();
    }

    @Override
    public boolean isPending() {
        return !closed && !exhausted;
    }

    @Override
    public boolean isClose() {
        return closed;
    }
}
