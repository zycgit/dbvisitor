package net.hasor.dbvisitor.adapter.milvus.commands.query;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.milvus.v2.service.vector.request.RunAnalyzerReq;
import io.milvus.v2.service.vector.response.RunAnalyzerResp;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.milvus.MilvusCmd;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandKeys;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommands;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.AnalyzeCmdContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.AnalyzerOptionContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.HintCommandContext;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.AdapterType;
import net.hasor.dbvisitor.driver.JdbcColumn;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.*;

/** Runs the server analyzer; preserves one result row per returned input, including empty token lists. */
public final class MilvusCommandsForAnalyzer extends MilvusCommands {
    private static final JdbcColumn TEXT_INDEX = new JdbcColumn("TEXT_INDEX", AdapterType.Long, "", "", "", ResultSetMetaData.columnNoNulls, false, AdapterType.Array);
    private static final JdbcColumn TOKENS     = new JdbcColumn("TOKENS", AdapterType.String, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    private static final Gson       JSON       = new Gson();

    private MilvusCommandsForAnalyzer() {
    }

    public static Future<?> execAnalyze(Future<Object> future, MilvusCmd cmd, HintCommandContext h, AnalyzeCmdContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger args = new AtomicInteger(startArgIdx);
        readHints(args, request, h.hint());
        Object texts = parseLiteral(c.texts, args, request);
        List<String> input = strings(texts instanceof String ? Collections.singletonList(texts) : texts, "ANALYZE texts");
        if (input.isEmpty()) {
            throw new SQLException("ANALYZE requires at least one text.");
        }
        RunAnalyzerReq.RunAnalyzerReqBuilder builder = RunAnalyzerReq.builder().texts(input);
        if (cmd.getCatalog() != null) {
            builder.databaseName(cmd.getCatalog());
        }
        if (c.collectionName != null) {
            builder.collectionName(readName(c.collectionName)).fieldName(readName(c.fieldName));
        }
        Map<String, Object> options = new LinkedHashMap<>();
        if (c.analyzerOptions() != null) {
            for (AnalyzerOptionContext option : c.analyzerOptions().analyzerOption()) {
                options.put(readName(option.identifier()), parseLiteral(option.literal(), args, request));
            }
        }
        applyOptions(builder, options);
        RunAnalyzerResp result = cmd.runAnalyzer(builder.build());
        List<Map<String, Object>> rows = new ArrayList<>();
        long index = 1;
        for (RunAnalyzerResp.AnalyzerResult item : result.getResults()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put(TEXT_INDEX.name, index++);
            row.put(TOKENS.name, JSON.toJson(item.getTokens()));
            rows.add(row);
        }
        receive.responseResult(request, listResult(request, Arrays.asList(TEXT_INDEX, TOKENS), rows));
        return completed(future);
    }

    private static void applyOptions(RunAnalyzerReq.RunAnalyzerReqBuilder builder, Map<String, Object> options) throws SQLException {
        for (Map.Entry<String, Object> option : options.entrySet()) {
            String key = option.getKey();
            Object value = option.getValue();
            if (MilvusCommandKeys.ANALYZER_PARAMS.equals(key)) {
                builder.analyzerParams(parameters(value));
            } else if (MilvusCommandKeys.ANALYZER_NAMES.equals(key)) {
                builder.analyzerNames(strings(value, key));
            } else if (MilvusCommandKeys.WITH_DETAIL.equals(key) || MilvusCommandKeys.WITH_HASH.equals(key)) {
                if (!(value instanceof Boolean)) {
                    throw new SQLException(key + " requires a boolean.");
                }
                if (MilvusCommandKeys.WITH_DETAIL.equals(key)) {
                    builder.withDetail((Boolean) value);
                } else {
                    builder.withHash((Boolean) value);
                }
            } else {
                throw new SQLException("Unknown ANALYZE option: " + key);
            }
        }
    }

    private static List<String> strings(Object value, String name) throws SQLException {
        if (value instanceof String[]) {
            value = Arrays.asList((String[]) value);
        }
        if (!(value instanceof Iterable<?>)) {
            throw new SQLException(name + " requires a list of non-null strings.");
        }
        List<String> values = new ArrayList<>();
        for (Object item : (Iterable<?>) value) {
            if (!(item instanceof String)) {
                throw new SQLException(name + " requires a list of non-null strings.");
            }
            values.add((String) item);
        }
        return values;
    }

    private static Map<String, Object> parameters(Object value) throws SQLException {
        try {
            if (value instanceof String) {
                value = JsonParser.parseString((String) value);
            }
            if (value instanceof JsonObject) {
                value = JSON.fromJson((JsonObject) value, Map.class);
            }
            if (value instanceof Map<?, ?>) {
                Map<String, Object> params = new LinkedHashMap<>();
                for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                    if (!(entry.getKey() instanceof String)) {
                        throw new SQLException("analyzer_params keys must be strings.");
                    }
                    params.put((String) entry.getKey(), entry.getValue());
                }
                return params;
            }
        } catch (RuntimeException error) {
            throw new SQLException("Invalid analyzer_params JSON object.", error);
        }
        throw new SQLException("analyzer_params requires a Map or JSON object string.");
    }
}
