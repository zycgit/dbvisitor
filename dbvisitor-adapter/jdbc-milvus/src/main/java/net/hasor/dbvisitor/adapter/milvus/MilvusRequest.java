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
package net.hasor.dbvisitor.adapter.milvus;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import net.hasor.dbvisitor.adapter.milvus.commands.query.MilvusResultCursor;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.JdbcErrorCode;

public class MilvusRequest extends AdapterRequest {
    // Immutable SQL execution settings.
    private final    String                  commandBody;
    private final    ConsistencyLevelEnum    consistencyLevel;
    private final    int                     maxRetry;
    private volatile boolean                 cancelled;
    private          long                    startedAt;
    // Execution remains active until both SQL dispatch and all lazy reads have finished.
    private final    Set<MilvusResultCursor> cursors = ConcurrentHashMap.newKeySet();
    private volatile boolean                 executing;
    private          Runnable                completed;

    public MilvusRequest(String commandBody, ConsistencyLevelEnum consistencyLevel, int maxRetry) {
        this.commandBody = commandBody;
        this.consistencyLevel = consistencyLevel;
        this.maxRetry = maxRetry;
    }

    public String getCommandBody() {
        return commandBody;
    }

    public ConsistencyLevelEnum getConsistencyLevel() {
        return consistencyLevel;
    }

    public int getMaxRetry() {
        return maxRetry;
    }

    void begin(Runnable completed) {
        this.completed = completed;
        this.executing = true;
        this.startedAt = System.nanoTime();
    }

    void endExecution() {
        this.executing = false;
        completeIfIdle();
    }

    public void addCursor(MilvusResultCursor cursor) {
        this.cursors.add(cursor);
        if (this.cancelled) {
            cursor.cancel();
        }
    }

    public void removeCursor(MilvusResultCursor cursor) {
        this.cursors.remove(cursor);
        completeIfIdle();
    }

    private void completeIfIdle() {
        if (!this.executing && this.cursors.isEmpty() && this.completed != null) {
            this.completed.run();
        }
    }

    void cancel() {
        this.cancelled = true;
        this.cursors.forEach(MilvusResultCursor::cancel);
    }

    public void checkActive() throws SQLException {
        if (this.cancelled || Thread.currentThread().isInterrupted()) {
            throw new SQLException("Operation cancelled.", JdbcErrorCode.SQL_STATE_IS_CANCELLED);
        }
        if (this.getTimeoutSec() > 0 && System.nanoTime() - this.startedAt >= TimeUnit.SECONDS.toNanos(this.getTimeoutSec())) {
            this.cancelled = true;
            throw new SQLTimeoutException("Milvus operation timed out.", JdbcErrorCode.SQL_STATE_QUERY_TIMEOUT);
        }
    }

    public static void checkActive(AdapterRequest request) throws SQLException {
        if (request instanceof MilvusRequest) {
            ((MilvusRequest) request).checkActive();
        }
    }
}
