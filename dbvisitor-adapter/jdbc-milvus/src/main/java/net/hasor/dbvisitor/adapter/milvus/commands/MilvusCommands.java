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
package net.hasor.dbvisitor.adapter.milvus.commands;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.AdapterResultCursor;
import net.hasor.dbvisitor.driver.AdapterType;
import net.hasor.dbvisitor.driver.JdbcColumn;

/** Common command execution and JDBC response handling. */
public abstract class MilvusCommands {
    // Shared result columns

    protected static final JdbcColumn COL_ID_LONG          = new JdbcColumn("ID", AdapterType.Long, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    protected static final JdbcColumn COL_DATABASE_STRING  = new JdbcColumn("DATABASE", AdapterType.String, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    protected static final JdbcColumn COL_PARTITION_STRING = new JdbcColumn("PARTITION", AdapterType.String, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    protected static final JdbcColumn COL_TABLE_STRING     = new JdbcColumn("TABLE", AdapterType.String, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    protected static final JdbcColumn COL_USER_STRING      = new JdbcColumn("USER", AdapterType.String, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    protected static final JdbcColumn COL_ROLE_STRING      = new JdbcColumn("ROLE", AdapterType.String, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    protected static final JdbcColumn COL_INDEX_STRING     = new JdbcColumn("INDEX", AdapterType.String, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    protected static final JdbcColumn COL_FIELD_STRING     = new JdbcColumn("FIELD", AdapterType.String, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    protected static final JdbcColumn COL_TYPE_STRING      = new JdbcColumn("TYPE", AdapterType.String, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    protected static final JdbcColumn COL_COUNT_LONG       = new JdbcColumn("COUNT", AdapterType.Long, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);

    // Interruptible waits shared by synchronous commands and retry backoff

    protected static void sleepQuietly(long millis) throws SQLException {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting Milvus operation.", e);
        }
    }

    // JDBC results and completion

    protected static Future<Object> completed(Future<Object> sync) {
        sync.completed(true);
        return sync;
    }

    public static Future<Object> failed(Future<Object> sync, Exception e) {
        sync.failed(e);
        return sync;
    }

    protected static <T> Map<String, T> singletonMap(String column, T keyCol) {
        Map<String, T> dataMap = new LinkedHashMap<>();
        dataMap.put(column, keyCol);
        return dataMap;
    }

    protected static AdapterResultCursor singleResult(AdapterRequest request, JdbcColumn col, Object value) throws SQLException {
        AdapterResultCursor result = new AdapterResultCursor(request, Collections.singletonList(col));
        result.pushData(singletonMap(col.name, value));
        result.pushFinish();
        return result;
    }

    protected static AdapterResultCursor twoResult(AdapterRequest request, JdbcColumn firstCol, Object firstValue, JdbcColumn secondCol, Object secondValue) throws SQLException {
        AdapterResultCursor result = new AdapterResultCursor(request, Arrays.asList(firstCol, secondCol));
        Map<String, Object> dataMap = new LinkedHashMap<>();
        dataMap.put(firstCol.name, firstValue);
        dataMap.put(secondCol.name, secondValue);
        result.pushData(dataMap);
        result.pushFinish();
        return result;
    }

    protected static AdapterResultCursor listResult(AdapterRequest request, JdbcColumn col, Collection<?> result) throws SQLException {
        long maxRows = request.getMaxRows();
        AdapterResultCursor receiveCur = new AdapterResultCursor(request, Collections.singletonList(col));
        int affectRows = 0;
        for (Object item : result) {
            receiveCur.pushData(CollectionUtils.asMap(col.name, item));

            affectRows++;
            if (maxRows > 0 && affectRows >= maxRows) {
                break;
            }
        }
        receiveCur.pushFinish();
        return receiveCur;
    }

    protected static AdapterResultCursor listResult(AdapterRequest request, JdbcColumn[] cols, List<Map<String, Object>> result) throws SQLException {
        return listResult(request, Arrays.asList(cols), result);
    }

    protected static AdapterResultCursor listResult(AdapterRequest request, List<JdbcColumn> cols, List<Map<String, Object>> result) throws SQLException {
        AdapterResultCursor cursor = new AdapterResultCursor(request, cols);
        long count = 0;
        for (Map<String, Object> row : result) {
            if (request.getMaxRows() > 0 && count >= request.getMaxRows()) {
                break;
            }

            cursor.pushData(row);
            count++;
        }
        cursor.pushFinish();
        return cursor;
    }

}
