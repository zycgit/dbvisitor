package net.hasor.dbvisitor.adapter.milvus.commands.imports;
import static net.hasor.dbvisitor.adapter.milvus.MilvusRequest.checkActive;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.*;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.milvus.MilvusCmd;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandKeys;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommands;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusRetry;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.HintCommandContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.ImportCmdContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.ShowCmdContext;
import net.hasor.dbvisitor.adapter.milvus.transport.MilvusImportClient;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.AdapterType;
import net.hasor.dbvisitor.driver.JdbcColumn;

/** Server-side file imports: submission, bounded waiting and JDBC job inspection. */
public final class MilvusCommandsForImport extends MilvusCommands {
    private MilvusCommandsForImport() {
    }

    private static final long       DEFAULT_WAIT_TIMEOUT_MS = 60_000;
    private static final long       POLL_INTERVAL_MS        = 100;
    private static final JdbcColumn JOB_ID                  = new JdbcColumn("JOB_ID", AdapterType.String, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    // @formatter:off
    private static final List<JdbcColumn> JOB_COLUMNS             = Arrays.asList(
        JOB_ID,
        new JdbcColumn("STATE", AdapterType.String, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array),
        new JdbcColumn("PROGRESS", AdapterType.Long, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array),
        new JdbcColumn("TOTAL_ROWS", AdapterType.Long, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array),
        new JdbcColumn("IMPORTED_ROWS", AdapterType.Long, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array),
        new JdbcColumn("REASON", AdapterType.String, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array),
        new JdbcColumn("DETAILS", "JSON", "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array)
    );
    // @formatter:on

    public static Future<?> execImportCmd(Future<Object> future, MilvusCmd cmd, HintCommandContext hint, ImportCmdContext c, AdapterRequest request, AdapterReceive receive, int start) throws SQLException {
        AtomicInteger args = new AtomicInteger(start);
        Map<String, Object> hints = readHints(args, request, hint.hint());
        List<List<String>> groups = files(parseLiteral(c.files, args, request));
        Map<String, Object> options = readProperties(args, request, c.propertiesList());
        boolean returning = c.resultName != null;
        if (returning && !"JOB_ID".equalsIgnoreCase(readName(c.resultName))) {
            throw new SQLException("IMPORT supports RETURNING JOB_ID.");
        }
        long timeout = hintAsLong(hints, MilvusCommandKeys.TIMEOUT, DEFAULT_WAIT_TIMEOUT_MS);
        if (timeout <= 0) {
            throw new SQLException("Import timeout must be positive.");
        }
        MilvusImportClient client = cmd.importClient();
        String database = cmd.getCatalog();
        String jobId = client.start(database, readName(c.collectionName), readName(c.partitionName), groups, options, request, timeout);
        if (hintAsBoolean(hints, MilvusCommandKeys.SYNC, true)) {
            await(client, database, jobId, request, timeout);
        }
        if (returning) {
            receive.responseResult(request, singleResult(request, JOB_ID, jobId));
        } else {
            receive.responseUpdateCount(request, 0); // Existing IMPORT remains an update-count command.
        }
        return completed(future);
    }

    public static Future<?> execShowImport(Future<Object> future, MilvusCmd cmd, HintCommandContext hint, ShowCmdContext c, AdapterRequest request, AdapterReceive receive, int start) throws SQLException {
        AtomicInteger args = new AtomicInteger(start);
        Map<String, Object> hints = readHints(args, request, hint.hint());
        long timeout = hintAsLong(hints, MilvusCommandKeys.TIMEOUT, DEFAULT_WAIT_TIMEOUT_MS);
        if (timeout <= 0) {
            throw new SQLException("Import timeout must be positive.");
        }
        MilvusImportClient client = cmd.importClient();
        List<Map<String, Object>> rows = new ArrayList<>();
        if (c.IMPORTS() != null) {
            Map<String, Object> options = readProperties(args, request, c.propertiesList());
            Long pageSize = null;
            Long currentPage = null;
            for (Map.Entry<String, Object> entry : options.entrySet()) {
                String key = entry.getKey();
                if (MilvusCommandKeys.IMPORT_PAGE_SIZE.equals(key)) {
                    pageSize = integerBound(entry.getValue(), key, 1);
                } else if (MilvusCommandKeys.IMPORT_CURRENT_PAGE.equals(key)) {
                    currentPage = integerBound(entry.getValue(), key, 1);
                } else {
                    throw new SQLException("Unknown import list option: " + key);
                }
            }
            JsonObject response = client.list(cmd.getCatalog(), readName(c.collectionName), pageSize, currentPage, request, timeout);
            JsonElement records = response.get(MilvusCommandKeys.REST_RECORDS);
            if (records == null || !records.isJsonArray()) {
                throw new SQLException("Import list response did not contain " + MilvusCommandKeys.REST_RECORDS + ".");
            }
            for (JsonElement record : records.getAsJsonArray()) {
                rows.add(jobRow(record.getAsJsonObject(), null));
            }
        } else {
            Object value = parseLiteral(c.jobId, args, request);
            if (!(value instanceof String jobId) || jobId.isBlank()) {
                throw new SQLException("Import job ID must be a non-empty string.");
            }
            rows.add(jobRow(client.progress(cmd.getCatalog(), jobId, request, timeout), jobId));
        }
        receive.responseResult(request, listResult(request, JOB_COLUMNS, rows));
        return completed(future);
    }

    private static List<List<String>> files(Object value) throws SQLException {
        if (value instanceof String) {
            value = Collections.singletonList(Collections.singletonList(value));
        }
        if (!(value instanceof List<?> outer) || outer.isEmpty()) {
            throw new SQLException("IMPORT requires a file path or a non-empty list of file groups.");
        }
        List<List<String>> groups = new ArrayList<>();
        for (Object item : outer) {
            if (!(item instanceof List<?> group) || group.isEmpty()) {
                throw new SQLException("Each import file group must be a non-empty list.");
            }
            List<String> paths = new ArrayList<>();
            for (Object path : group) {
                if (!(path instanceof String) || ((String) path).isBlank()) {
                    throw new SQLException("Import paths must be non-empty strings in server-accessible object storage.");
                }
                paths.add((String) path);
            }
            groups.add(paths);
        }
        return groups;
    }

    private static void await(MilvusImportClient client, String database, String jobId, AdapterRequest request, long timeout) throws SQLException {
        long started = System.nanoTime();
        JsonObject last = new JsonObject();
        try {
            while (true) {
                checkActive(request);
                long remaining = timeout - (System.nanoTime() - started) / 1_000_000;
                if (remaining <= 0) {
                    throw new SQLException("Timeout waiting bulk insert");
                }
                last = client.progress(database, jobId, request, remaining);
                String state = text(last, MilvusCommandKeys.REST_STATE);
                if ("Completed".equals(state)) {
                    return;
                }
                if ("Failed".equals(state)) {
                    throw new SQLException("Bulk insert failed");
                }
                sleepQuietly(Math.min(POLL_INTERVAL_MS, remaining));
            }
        } catch (SQLException | RuntimeException failure) {
            SQLException cause = MilvusRetry.sqlException(failure);
            // @formatter:off
            String message = cause.getMessage()
                    + ", " + MilvusCommandKeys.REST_JOB_ID + "=" + jobId
                    + ", " + MilvusCommandKeys.REST_STATE + "=" + text(last, MilvusCommandKeys.REST_STATE)
                    + ", " + MilvusCommandKeys.REST_IMPORTED_ROWS + "=" + number(last, MilvusCommandKeys.REST_IMPORTED_ROWS)
                    + ", " + MilvusCommandKeys.REST_REASON + "=" + reason(last)
                    + "; the server job is not cancelled or resubmitted. Inspect it with SHOW IMPORT.";
            // @formatter:on
            if (cause instanceof java.sql.SQLTimeoutException) {
                throw new java.sql.SQLTimeoutException(message, cause.getSQLState(), cause.getErrorCode(), cause);
            }
            throw new SQLException(message, cause.getSQLState(), cause.getErrorCode(), cause);
        }
    }

    private static Map<String, Object> jobRow(JsonObject data, String id) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("JOB_ID", data.has(MilvusCommandKeys.REST_JOB_ID) ? text(data, MilvusCommandKeys.REST_JOB_ID) : id);
        row.put("STATE", text(data, MilvusCommandKeys.REST_STATE));
        row.put("PROGRESS", number(data, MilvusCommandKeys.REST_PROGRESS));
        row.put("TOTAL_ROWS", number(data, MilvusCommandKeys.REST_TOTAL_ROWS));
        row.put("IMPORTED_ROWS", number(data, MilvusCommandKeys.REST_IMPORTED_ROWS));
        row.put("REASON", reason(data));
        row.put("DETAILS", data);
        return row;
    }

    private static String reason(JsonObject data) {
        return data.has(MilvusCommandKeys.REST_REASON) ? text(data, MilvusCommandKeys.REST_REASON) : text(data, MilvusCommandKeys.REST_FAILED_REASON);
    }

    private static String text(JsonObject data, String key) {
        return data.has(key) && !data.get(key).isJsonNull() ? data.get(key).getAsString() : null;
    }

    private static Long number(JsonObject data, String key) {
        String value = text(data, key);
        return value == null ? null : Long.valueOf(value);
    }
}
