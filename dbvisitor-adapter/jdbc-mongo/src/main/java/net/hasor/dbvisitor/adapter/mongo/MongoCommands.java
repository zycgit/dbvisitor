package net.hasor.dbvisitor.adapter.mongo;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.ListCollectionNamesIterable;
import com.mongodb.client.model.*;
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoParser;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoParser.CollectionContext;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoParser.DatabaseNameContext;
import net.hasor.dbvisitor.driver.*;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.ObjectId;

abstract class MongoCommands {
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

    protected static Map<String, Object> readHints(AtomicInteger argIndex, AdapterRequest request, List<MongoParser.HintContext> hint) throws SQLException {
        Map<String, Object> hintMap = new LinkedHashMap<>();
        if (hint == null || hint.isEmpty()) {
            return hintMap;
        }

        for (MongoParser.HintContext ctx : hint) {
            if (ctx == null || ctx.hints() == null) {
                continue;
            }
            for (MongoParser.HintItContext it : ctx.hints().hintIt()) {
                String key = getIdentifier(it.name.getText());
                MongoParser.HintValueContext valCtx = it.value;
                Object value = null;

                if (valCtx.literal() != null) {
                    value = parseLiteral(valCtx.literal(), argIndex, request);
                } else if (valCtx.identifier() != null) {
                    value = getIdentifier(valCtx.identifier().getText());
                }

                hintMap.put(key, value);
            }
        }
        return hintMap;
    }

    private static Object parseLiteral(MongoParser.LiteralContext ctx, AtomicInteger argIndex, AdapterRequest request) throws SQLException {
        if (ctx == null) {
            return null;
        }
        if (ctx.ARG() != null) {
            return getArg(argIndex, request);
        }
        if (ctx.NUMERIC_LITERAL() != null) {
            String text = ctx.NUMERIC_LITERAL().getText();
            if (text.contains(".") || text.contains("e") || text.contains("E")) {
                return Double.parseDouble(text);
            }
            return Long.parseLong(text);
        }
        if (ctx.DOUBLE_QUOTED_STRING_LITERAL() != null || ctx.SINGLE_QUOTED_STRING_LITERAL() != null) {
            return getIdentifier(ctx.getText());
        }
        if (ctx.TRUE() != null) {
            return Boolean.TRUE;
        }
        if (ctx.FALSE() != null) {
            return Boolean.FALSE;
        }
        if (ctx.NULL() != null) {
            return null;
        }
        return ctx.getText();
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

    protected static String argAsDbName(AtomicInteger argIndex, AdapterRequest request, DatabaseNameContext ctx, MongoCmd mongoCmd) throws SQLException {
        if (ctx == null) {
            return mongoCmd.getCatalog();
        }

        if (ctx.ARG() != null) {
            Object arg = getArg(argIndex, request);
            return arg == null ? null : arg.toString();
        }

        String text = getIdentifier(ctx.getText());
        if ("db".equals(text)) {
            String dbName = mongoCmd.getCatalog();
            if (StringUtils.isBlank(dbName)) {
                throw new SQLException("No database selected.");
            } else {
                return dbName;
            }
        } else {
            return text;
        }
    }

    protected static String argAsCollectionName(AtomicInteger argIndex, AdapterRequest request, CollectionContext ctx) throws SQLException {
        if (ctx.ARG() != null) {
            Object arg = getArg(argIndex, request);
            return arg == null ? null : arg.toString();
        }
        return getIdentifier(ctx.getText());
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

    protected static AdapterResultCursor listResult(AdapterRequest request, JdbcColumn col, DistinctIterable<?> result) throws SQLException {
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

    protected static AdapterResultCursor listResult(AdapterRequest request, JdbcColumn col, ListCollectionNamesIterable result) throws SQLException {
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

    protected static AdapterResultCursor listResult(AdapterRequest request, JdbcColumn col, Map<Integer, BsonValue> result, int count) throws SQLException {
        if (result == null) {
            return null;
        }

        AdapterResultCursor receiveCur = new AdapterResultCursor(request, Collections.singletonList(col));
        for (int i = 0; i < count; i++) {
            BsonValue bsonValue = result.get(i);
            receiveCur.pushData(CollectionUtils.asMap(col.name, bsonValue.asObjectId().getValue().toHexString()));
        }
        receiveCur.pushFinish();
        return receiveCur;
    }

    //

    protected static String hexObjectId(Document doc) {
        try {
            ObjectId docId = doc.getObjectId("_id");
            return docId == null ? null : docId.toHexString();
        } catch (Exception e) {
            return null;
        }
    }

    protected static Collation jsonb2Collation(Map<String, Object> options) {
        if (options == null) {
            return null;
        }
        Collation.Builder builder = Collation.builder();
        if (options.containsKey("locale")) {
            builder.locale((String) options.get("locale"));
        }
        if (options.containsKey("caseLevel")) {
            builder.caseLevel((Boolean) options.get("caseLevel"));
        }
        if (options.containsKey("caseFirst")) {
            builder.collationCaseFirst(CollationCaseFirst.fromString((String) options.get("caseFirst")));
        }
        if (options.containsKey("strength")) {
            builder.collationStrength(CollationStrength.fromInt(((Number) options.get("strength")).intValue()));
        }
        if (options.containsKey("numericOrdering")) {
            builder.numericOrdering((Boolean) options.get("numericOrdering"));
        }
        if (options.containsKey("alternate")) {
            builder.collationAlternate(CollationAlternate.fromString((String) options.get("alternate")));
        }
        if (options.containsKey("maxVariable")) {
            builder.collationMaxVariable(CollationMaxVariable.fromString((String) options.get("maxVariable")));
        }
        if (options.containsKey("normalization")) {
            builder.normalization((Boolean) options.get("normalization"));
        }
        if (options.containsKey("backwards")) {
            builder.backwards((Boolean) options.get("backwards"));
        }
        return builder.build();
    }

    protected static Document toDocument(Map<?, ?> map) {
        Document doc = new Document();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = entry.getKey().toString();
            Object value = entry.getValue();
            if (value instanceof Map) {
                doc.put(key, toDocument((Map<?, ?>) value));
            } else if (value instanceof List) {
                doc.put(key, toList((List<?>) value));
            } else {
                doc.put(key, value);
            }
        }
        return doc;
    }

    protected static List<Object> toList(List<?> list) {
        List<Object> newList = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map) {
                newList.add(toDocument((Map<?, ?>) item));
            } else if (item instanceof List) {
                newList.add(toList((List<?>) item));
            } else {
                newList.add(item);
            }
        }
        return newList;
    }

    protected static Boolean getOptionBoolean(Map<String, Object> options, String key) throws SQLException {
        if (!options.containsKey(key)) {
            return null;
        }
        Object val = options.get(key);
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        throw new SQLException(key + " must be boolean");
    }

    protected static Integer getOptionInt(Map<String, Object> options, String key) throws SQLException {
        if (!options.containsKey(key)) {
            return null;
        }
        Object val = options.get(key);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        throw new SQLException(key + " must be number");
    }

    protected static Long getOptionLong(Map<String, Object> options, String key) throws SQLException {
        if (!options.containsKey(key)) {
            return null;
        }
        Object val = options.get(key);
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        throw new SQLException(key + " must be number");
    }

    protected static String getOptionString(Map<String, Object> options, String key) throws SQLException {
        if (!options.containsKey(key)) {
            return null;
        }
        Object val = options.get(key);
        if (val instanceof String) {
            return (String) val;
        }
        throw new SQLException(key + " must be string");
    }

    protected static Map<String, Object> getOptionMap(Map<String, Object> options, String key) throws SQLException {
        if (!options.containsKey(key)) {
            return null;
        }
        try {
            return toObjBson(options.get(key));
        } catch (Exception e) {
            throw new SQLException(key + " must be object", e);
        }
    }

    protected static <T> List<T> getOptionList(Map<String, Object> options, String key) throws SQLException {
        if (!options.containsKey(key)) {
            return null;
        }
        try {
            return (List<T>) toArrayBson(options.get(key));
        } catch (Exception e) {
            throw new SQLException(key + " must be array", e);
        }
    }

    protected static Number getOptionNumber(Map<String, Object> options, String key) throws SQLException {
        if (!options.containsKey(key)) {
            return null;
        }
        Object val = options.get(key);
        if (val instanceof Number) {
            return (Number) val;
        }
        throw new SQLException(key + " must be number");
    }

    protected static Double getOptionDouble(Map<String, Object> options, String key) throws SQLException {
        if (!options.containsKey(key)) {
            return null;
        }
        Object val = options.get(key);
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        throw new SQLException(key + " must be number");
    }

    protected static List<Object> toArrayBson(Object docOrList) throws SQLException {
        if (docOrList == null) {
            return null;
        }
        if (docOrList instanceof List) {
            return (List<Object>) docOrList;
        }
        if (docOrList instanceof String) {
            String json = ((String) docOrList).trim();
            try {
                if (json.startsWith("[")) {
                    org.bson.BsonArray bsonArray = org.bson.BsonArray.parse(json);
                    List<Object> list = new ArrayList<>();
                    for (org.bson.BsonValue val : bsonArray) {
                        if (val.isDocument()) {
                            list.add(Document.parse(val.asDocument().toJson()));
                        } else if (val.isString()) {
                            list.add(val.asString().getValue());
                        } else if (val.isBoolean()) {
                            list.add(val.asBoolean().getValue());
                        } else if (val.isInt32()) {
                            list.add(val.asInt32().getValue());
                        } else if (val.isInt64()) {
                            list.add(val.asInt64().getValue());
                        } else if (val.isDouble()) {
                            list.add(val.asDouble().getValue());
                        } else if (val.isNull()) {
                            list.add(null);
                        } else {
                            list.add(val);
                        }
                    }
                    return list;
                } else {
                    List<Object> list = new ArrayList<>();
                    list.add(Document.parse(json));
                    return list;
                }
            } catch (Exception e) {
                throw new SQLException("Parse JSON failed: " + e.getMessage(), e);
            }
        }
        List<Object> list = new ArrayList<>();
        list.add(docOrList);
        return list;
    }

    protected static Map<String, Object> toObjBson(Object docOrList) throws SQLException {
        if (docOrList == null) {
            return null;
        }
        if (docOrList instanceof Map) {
            return (Map<String, Object>) docOrList;
        }
        if (docOrList instanceof String) {
            String json = ((String) docOrList).trim();
            try {
                if (json.startsWith("[")) {
                    org.bson.BsonArray bsonArray = org.bson.BsonArray.parse(json);
                    if (bsonArray.size() == 1 && bsonArray.get(0).isDocument()) {
                        return Document.parse(bsonArray.get(0).asDocument().toJson());
                    }
                    throw new SQLException("Expected JSON Object, but got JSON Array with size != 1 or content is not document.");
                } else {
                    return Document.parse(json);
                }
            } catch (Exception e) {
                if (e instanceof SQLException) {
                    throw (SQLException) e;
                }
                throw new SQLException("Parse JSON failed: " + e.getMessage(), e);
            }
        }
        if (docOrList instanceof List) {
            List<?> list = (List<?>) docOrList;
            if (list.size() == 1 && list.get(0) instanceof Map) {
                return (Map<String, Object>) list.get(0);
            }
            throw new SQLException("Expected Object/Map, but got List with size != 1 or content is not map.");
        }
        throw new SQLException("Cannot convert " + docOrList.getClass().getName() + " to Bson Object/Map.");
    }
}