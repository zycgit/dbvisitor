package net.hasor.dbvisitor.adapter.elastic;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.driver.*;

abstract class ElasticCommands {
    // for db and collections
    protected static final JdbcColumn COL_ID_STRING              = new JdbcColumn("_ID", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_JSON_STRING            = new JdbcColumn("_JSON", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_DATABASE_STRING        = new JdbcColumn("DATABASE", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_COLLECTION_STRING      = new JdbcColumn("COLLECTION", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_VALUE_STRING           = new JdbcColumn("VALUE", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_COUNT_LONG             = new JdbcColumn("COUNT", AdapterType.Long, "", "", "");
    protected static final JdbcColumn COL_NAME_STRING            = new JdbcColumn("NAME", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_TYPE_STRING            = new JdbcColumn("TYPE", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_OPTIONS_STRING         = new JdbcColumn("OPTIONS", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_INFO_STRING            = new JdbcColumn("INFO", AdapterType.String, "", "", "");
    // for index
    protected static final JdbcColumn COL_IDX_V_INT              = new JdbcColumn("V", AdapterType.Int, "", "", "");
    protected static final JdbcColumn COL_IDX_KEY_STRING         = new JdbcColumn("KEY", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_IDX_NAME_STRING        = new JdbcColumn("NAME", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_IDX_NS_STRING          = new JdbcColumn("NS", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_IDX_UNIQUE_BOOLEAN     = new JdbcColumn("UNIQUE", AdapterType.Boolean, "", "", "");
    protected static final JdbcColumn COL_IDX_SPARSE_BOOLEAN     = new JdbcColumn("SPARSE", AdapterType.Boolean, "", "", "");
    protected static final JdbcColumn COL_IDX_BACKGROUND_BOOLEAN = new JdbcColumn("BACKGROUND", AdapterType.Boolean, "", "", "");
    protected static final JdbcColumn COL_IDX_HIDDEN_BOOLEAN     = new JdbcColumn("HIDDEN", AdapterType.Boolean, "", "", "");
    // for user and role
    protected static final JdbcColumn COL_USER_STRING            = new JdbcColumn("USER", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_DB_STRING              = new JdbcColumn("DB", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_ROLES_STRING           = new JdbcColumn("ROLES", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_ROLE_STRING            = new JdbcColumn("ROLE", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_IS_BUILTIN_BOOLEAN     = new JdbcColumn("IS_BUILTIN", AdapterType.Boolean, "", "", "");
    protected static final JdbcColumn COL_INHERITED_ROLES_STRING = new JdbcColumn("INHERITED_ROLES", AdapterType.String, "", "", "");

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