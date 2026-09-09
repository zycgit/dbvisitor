package net.hasor.dbvisitor.adapter.milvus.commands.write;
import static net.hasor.dbvisitor.adapter.milvus.MilvusRequest.checkActive;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.*;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusExpression.parseTerm;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.milvus.grpc.FieldSchema;
import io.milvus.param.Constant;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.QueryIteratorReq;
import io.milvus.v2.service.vector.request.UpsertReq;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.UpsertResp;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.milvus.MilvusCmd;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommands;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusRetry;
import net.hasor.dbvisitor.adapter.milvus.mapping.MilvusSchema;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.*;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.AdapterResultCursor;

/** Row encoding and bounded INSERT/UPSERT batches; UPDATE selection belongs to Data. */
public final class MilvusCommandsForWrite extends MilvusCommands {
    private MilvusCommandsForWrite() {
    }

    private static final Gson GSON = new com.google.gson.GsonBuilder().serializeNulls().create();

    public static Future<?> execInsertCmd(Future<Object> future, MilvusCmd cmd, HintCommandContext h, InsertCmdContext c, AdapterRequest request, AdapterReceive receive, int start) throws SQLException {
        return write(future, cmd, h, c.collectionName, c.partitionName, c.columnList, c.valuesClause(), request, receive, start, false);
    }

    public static Future<?> execUpsertCmd(Future<Object> future, MilvusCmd cmd, HintCommandContext h, UpsertCmdContext c, AdapterRequest request, AdapterReceive receive, int start) throws SQLException {
        return write(future, cmd, h, c.collectionName, c.partitionName, c.columnList, c.valuesClause(), request, receive, start, true);
    }

    private static Future<?> write(Future<Object> future, MilvusCmd cmd, HintCommandContext hint, IdentifierContext table, IdentifierContext partition, IdentifiersContext columns, ValuesClauseContext values, AdapterRequest request, AdapterReceive receive, int start, boolean upsert) throws SQLException {
        AtomicInteger args = new AtomicInteger(start);
        readHints(args, request, hint.hint());
        String collection = readName(table);
        String part = partition == null ? "" : readName(partition);
        Map<String, FieldSchema> fields = cmd.describeFields(collection, request);
        List<String> names = new ArrayList<>();
        if (columns != null)
            for (IdentifierContext column : columns.identifier()) {
                String name = readName(column);
                if (!fields.containsKey(name))
                    throw new SQLException("Unknown field: " + name);
                if (names.contains(name))
                    throw new SQLException("Duplicate write column: " + name);
                names.add(name);
            }
        Iterator<?> rows;
        if (values.ARG() != null) {
            Object bound = getArg(args, request);
            if (bound instanceof Iterable<?>)
                rows = ((Iterable<?>) bound).iterator();
            else if (bound instanceof Iterator<?>)
                rows = (Iterator<?>) bound;
            else
                throw new SQLException("VALUES ? requires an Iterable or Iterator of rows.");
        } else {
            if (names.isEmpty())
                throw new SQLException("VALUES tuples require an explicit column list.");
            for (ValueRowContext row : values.valueRow()) {
                if (row.terms().term().size() != names.size())
                    throw new SQLException("Column count doesn't match value count.");
            }
            rows = values.valueRow().iterator();
        }

        FieldSchema primary = fields.values().stream().filter(FieldSchema::getIsPrimaryKey).findFirst().orElse(null);
        AdapterResultCursor keys = request.isGeneratedKeys() && primary != null ? new AdapterResultCursor(request, Collections.singletonList(MilvusSchema.column(primary, collection, cmd.getCatalog()))) : null;
        int batchSize = request.getFetchSize() > 0 ? Math.min(request.getFetchSize(), Constant.MAX_BATCH_SIZE) : (int) QueryIteratorReq.builder().collectionName(collection).build().getBatchSize();
        long confirmedRows = 0;
        long confirmedPages = 0;
        String phase = "read";
        List<JsonObject> page = new ArrayList<>(batchSize);
        try {
            while (true) {
                phase = "read";
                page.clear();
                checkActive(request);
                while (page.size() < batchSize && rows.hasNext()) {
                    checkActive(request);
                    page.add(encode(rows.next(), names, fields, args, request));
                }
                if (page.isEmpty())
                    break;
                checkActive(request); // An input iterator or row encoder may take time or cancel the statement.
                phase = "write";
                // INSERT/AutoID UPSERT are not idempotent. Never automatically replay an ambiguous write.
                List<Object> ids;
                long count;
                if (upsert) {
                    UpsertResp result = cmd.upsert(UpsertReq.builder().databaseName(cmd.getCatalog()).collectionName(collection).partitionName(part).data(new ArrayList<>(page)).build());
                    count = result.getUpsertCnt();
                    ids = result.getPrimaryKeys();
                } else {
                    InsertResp result = cmd.insert(InsertReq.builder().databaseName(cmd.getCatalog()).collectionName(collection).partitionName(part).data(new ArrayList<>(page)).build());
                    count = result.getInsertCnt();
                    ids = result.getPrimaryKeys();
                }
                confirmedRows = Math.addExact(confirmedRows, count);
                confirmedPages++;
                if (keys != null && ids != null)
                    for (Object id : ids)
                        keys.pushData(Collections.singletonMap(primary.getName(), id));
                checkActive(request);
            }
            if (keys != null)
                keys.pushFinish();
            receive.responseUpdateCount(request, confirmedRows, keys);
            return completed(future);
        } catch (SQLException | RuntimeException failure) {
            if (keys != null) {
                try {
                    keys.close();
                } catch (java.io.IOException closeFailure) {
                    failure.addSuppressed(closeFailure);
                }
            }
            SQLException cause = MilvusRetry.sqlException(failure);
            // @formatter:off
            String message = "Milvus " + (upsert ? "UPSERT" : "INSERT") + " failed: phase=" + phase
                    + ", confirmedPages=" + confirmedPages
                    + ", confirmedRows=" + confirmedRows
                    + ", currentPageRows=" + page.size()
                    + "; failed write outcome may be unknown; not retried. " + cause.getMessage();
            // @formatter:on
            if (cause instanceof java.sql.SQLTimeoutException) {
                throw new java.sql.SQLTimeoutException(message, cause.getSQLState(), cause.getErrorCode(), cause);
            }
            throw new SQLException(message, cause.getSQLState(), cause.getErrorCode(), cause);
        }
    }

    private static JsonObject encode(Object row, List<String> names, Map<String, FieldSchema> fields, AtomicInteger args, AdapterRequest request) throws SQLException {
        Map<String, Object> values = new LinkedHashMap<>();
        if (row instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!(entry.getKey() instanceof String))
                    throw new SQLException("Row map keys must be field names.");
                values.put((String) entry.getKey(), entry.getValue());
            }
            if (!names.isEmpty() && !values.keySet().equals(new HashSet<>(names)))
                throw new SQLException("Row map keys do not match the column list.");
        } else {
            List<?> items;
            if (row instanceof ValueRowContext tuple) {
                List<Object> parsed = new ArrayList<>();
                for (TermContext term : tuple.terms().term())
                    parsed.add(parseTerm(term, args, request));
                items = parsed;
            } else if (row instanceof List<?>)
                items = (List<?>) row;
            else if (row instanceof Object[])
                items = Arrays.asList((Object[]) row);
            else
                throw new SQLException("Each VALUES row must be a Map, List or Object[].");
            if (names.isEmpty() || names.size() != items.size())
                throw new SQLException("Column count doesn't match value count.");
            for (int i = 0; i < names.size(); i++)
                values.put(names.get(i), items.get(i));
        }
        JsonObject result = new JsonObject();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            FieldSchema field = fields.get(entry.getKey());
            if (field == null)
                throw new SQLException("Unknown field: " + entry.getKey());
            result.add(entry.getKey(), GSON.toJsonTree(MilvusSchema.convertFieldValue(field, entry.getValue())));
        }
        return result;
    }
}
