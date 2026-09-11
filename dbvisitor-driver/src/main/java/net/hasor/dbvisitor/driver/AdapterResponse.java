/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.driver;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import net.hasor.cobble.concurrent.future.BasicFuture;
import net.hasor.cobble.io.IOUtils;

public class AdapterResponse extends BasicFuture<AdapterResponse> {

    private boolean          resultIsResult;
    private boolean          resultIsError;
    private List<JdbcColumn> columns;
    private AdapterCursor    resultSet;
    private long             updateCount;
    private boolean          updateCountRead;
    private Throwable        exception;
    private AdapterCursor    generatedKeysResultSet;
    private JdbcResultSet    jdbcResultSet;
    private JdbcResultSet    jdbcGeneratedKeys;

    JdbcResultSet resultSet(JdbcStatement statement) {
        if (this.jdbcResultSet == null) {
            this.jdbcResultSet = new JdbcResultSet(statement, this.resultSet);
        }
        return this.jdbcResultSet;
    }

    JdbcResultSet generatedKeys(JdbcStatement statement) {
        if (this.jdbcGeneratedKeys == null) {
            AdapterCursor cursor = this.generatedKeysResultSet;
            if (cursor == null) {
                cursor = new AdapterMemoryCursor(Collections.emptyList(), new Object[0][]);
            }
            this.jdbcGeneratedKeys = new JdbcResultSet(statement, cursor);
        }
        return this.jdbcGeneratedKeys;
    }

    boolean resultsClosed() {
        return (this.resultSet == null || this.resultSet.isClose()) && //
                (this.generatedKeysResultSet == null || this.generatedKeysResultSet.isClose());
    }

    boolean hasPendingCursor() {
        return this.resultSet != null && this.resultSet.isPending();
    }

    boolean currentResultComplete() {
        return !this.resultIsError && (this.resultIsResult || this.updateCountRead) && this.resultsClosed();
    }

    public void closeResult() {
        if (this.jdbcResultSet != null) {
            this.jdbcResultSet.close();
        } else {
            IOUtils.closeQuietly(this.resultSet);
        }
        if (this.jdbcGeneratedKeys != null) {
            this.jdbcGeneratedKeys.close();
        } else {
            IOUtils.closeQuietly(this.generatedKeysResultSet);
        }
    }

    public List<JdbcColumn> getColumnList() {
        return this.columns;
    }

    public boolean isResult() {
        return this.resultIsResult;
    }

    public boolean isError() {
        return this.resultIsError;
    }

    public SQLException toError() {
        if (this.exception instanceof SQLException) {
            return (SQLException) this.exception;
        } else {
            return new SQLException(this.exception);
        }
    }

    public long getUpdateCount() {
        this.updateCountRead = true;
        return this.updateCount;
    }

    public static AdapterResponse ofError(Throwable e) {
        AdapterResponse res = new AdapterResponse();
        res.resultIsError = true;

        res.columns = Collections.emptyList();
        res.resultSet = null;
        res.updateCount = 0;
        res.exception = e;
        return res;
    }

    public static AdapterResponse ofCursor(AdapterCursor cursor, AdapterCursor generatedKeys) {
        AdapterResponse res = new AdapterResponse();
        res.resultIsResult = true;

        res.columns = cursor.columns();
        res.resultSet = cursor;
        res.generatedKeysResultSet = generatedKeys;
        res.updateCount = 0;
        res.exception = null;
        return res;
    }

    public static AdapterResponse ofUpdateCount(long updateCount, AdapterCursor generatedKeys) {
        AdapterResponse res = new AdapterResponse();
        res.resultIsResult = false;

        res.columns = Collections.emptyList();
        res.resultSet = null;
        res.generatedKeysResultSet = generatedKeys;
        res.updateCount = updateCount;
        res.exception = null;
        return res;
    }
}
