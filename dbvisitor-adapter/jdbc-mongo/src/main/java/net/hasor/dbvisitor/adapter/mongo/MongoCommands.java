package net.hasor.dbvisitor.adapter.mongo;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoParser.DatabaseNameContext;
import net.hasor.dbvisitor.driver.*;

abstract class MongoCommands {
    // value (value / element / the value hash)
    protected static final JdbcColumn COL_DATABASE_STRING = new JdbcColumn("DATABASE", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_VALUE_LONG      = new JdbcColumn("VALUE", AdapterType.Long, "", "", "");
    protected static final JdbcColumn COL_VALUE_BYTES     = new JdbcColumn("VALUE", AdapterType.Bytes, "", "", "");
    protected static final JdbcColumn COL_ELEMENT_STRING  = new JdbcColumn("ELEMENT", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_RANK_LONG       = new JdbcColumn("RANK", AdapterType.Long, "", "", "");
    protected static final JdbcColumn COL_SCORE_DOUBLE    = new JdbcColumn("SCORE", AdapterType.Double, "", "", "");
    // result (not value)
    protected static final JdbcColumn COL_RESULT_BOOLEAN  = new JdbcColumn("RESULT", AdapterType.Boolean, "", "", "");
    protected static final JdbcColumn COL_RESULT_STRING   = new JdbcColumn("RESULT", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_RESULT_LONG     = new JdbcColumn("RESULT", AdapterType.Long, "", "", "");
    protected static final JdbcColumn COL_RESULT_DOUBLE   = new JdbcColumn("RESULT", AdapterType.Double, "", "", "");
    // other
    protected static final JdbcColumn COL_KEY_STRING      = new JdbcColumn("KEY", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_FIELD_STRING    = new JdbcColumn("FIELD", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_CURSOR_STRING   = new JdbcColumn("CURSOR", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_LOCAL_LONG      = new JdbcColumn("LOCAL", AdapterType.Long, "", "", "");
    protected static final JdbcColumn COL_REPLICAS_LONG   = new JdbcColumn("REPLICAS", AdapterType.Long, "", "", "");
    protected static final JdbcColumn COL_GROUP_STRING    = new JdbcColumn("GROUP", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_NAME_STRING     = new JdbcColumn("NAME", AdapterType.String, "", "", "");

    protected static Object getArg(AtomicInteger argIndex, AdapterRequest request) throws SQLException {
        int argIdx = argIndex.getAndIncrement();
        String argName = "arg" + (argIdx + 1);
        JdbcArg jdbcArg = request.getArgMap().get(argName);
        if (jdbcArg == null) {
            throw new SQLException(argName + " not found in request.");
        } else {
            return jdbcArg.getValue();
        }
    }

    protected static String getIdentifier(String nodeText) {
        char firstChar = nodeText.charAt(0);
        char endChar = nodeText.charAt(nodeText.length() - 1);

        if (firstChar == '"' && endChar == '"') {
            return nodeText.substring(1, nodeText.length() - 1);
        } else if (firstChar == '\'' && endChar == '\'') {
            return nodeText.substring(1, nodeText.length() - 1);
        } else {
            return nodeText;
        }
    }

    protected static String argAsString(AtomicInteger argIndex, AdapterRequest request, DatabaseNameContext ctx, MongoCmd mongoCmd) throws SQLException {
        if (ctx.ARG() != null) {
            Object arg = getArg(argIndex, request);
            return arg == null ? null : arg.toString();
        }

        String text = getIdentifier(ctx.getText());
        if ("db".equals(text)) {
            return mongoCmd.getCatalog();
        } else {
            return text;
        }
    }

    protected static Future<?> completed(Future<Object> sync) {
        sync.completed(true);
        return sync;
    }

    public static Future<?> failed(Future<Object> sync, Exception e) {
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

    protected static AdapterResultCursor listResult(AdapterRequest request, JdbcColumn keyCol, JdbcColumn valCol, Map<?, ?> result) throws SQLException {
        long maxRows = request.getMaxRows();
        AdapterResultCursor receiveCur = new AdapterResultCursor(request, Arrays.asList(keyCol, valCol));
        int affectRows = 0;
        for (Map.Entry<?, ?> item : result.entrySet()) {
            receiveCur.pushData(CollectionUtils.asMap(keyCol.name, item.getKey(), valCol.name, item.getValue()));

            affectRows++;
            if (maxRows > 0 && affectRows >= maxRows) {
                break;
            }
        }
        receiveCur.pushFinish();
        return receiveCur;
    }
}