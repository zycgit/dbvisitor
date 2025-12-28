package net.hasor.dbvisitor.adapter.elastic;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.driver.*;

abstract class ElasticCommands {
    // for db and collections
    protected static final JdbcColumn COL_ID_STRING           = new JdbcColumn("_ID", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_JSON_STRING         = new JdbcColumn("_JSON", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_VALUE_STRING        = new JdbcColumn("VALUE", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_COUNT_LONG          = new JdbcColumn("COUNT", AdapterType.Long, "", "", "");
    protected static final JdbcColumn COL_NAME_STRING         = new JdbcColumn("NAME", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_INFO_STRING         = new JdbcColumn("INFO", AdapterType.String, "", "", "");
    // for index
    protected static final JdbcColumn COL_IDX_V_INT           = new JdbcColumn("V", AdapterType.Int, "", "", "");
    protected static final JdbcColumn COL_HEALTH_STRING       = new JdbcColumn("health", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_STATUS_STRING       = new JdbcColumn("status", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_PRIMARIES_STRING    = new JdbcColumn("primaries", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_REPLICAS_STRING     = new JdbcColumn("replicas", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_DOCS_COUNT_STRING   = new JdbcColumn("docs_count", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_STORAGE_SIZE_STRING = new JdbcColumn("storage_size", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_DATA_STREAM_STRING  = new JdbcColumn("data_stream", AdapterType.String, "", "", "");

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

    //

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

    protected static AdapterResultCursor listResult(AdapterRequest request, List<JdbcColumn> columns, List<Map<String, Object>> result) throws SQLException {
        long maxRows = request.getMaxRows();
        AdapterResultCursor receiveCur = new AdapterResultCursor(request, columns);
        int affectRows = 0;
        for (Map<String, Object> item : result) {
            receiveCur.pushData(item);

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