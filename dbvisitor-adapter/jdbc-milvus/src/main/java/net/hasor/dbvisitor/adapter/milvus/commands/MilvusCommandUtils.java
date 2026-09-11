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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.param.Constant;
import io.milvus.response.QueryResultsWrapper.RowRecord;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.service.vector.request.QueryIteratorReq;
import io.milvus.v2.service.vector.request.QueryReq;
import io.milvus.v2.service.vector.request.SearchIteratorReqV2;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.response.SearchResp.SearchResult;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.adapter.milvus.MilvusCmd;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.*;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.JdbcArg;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * SQL arguments, hints, identifiers, limits and shared SDK request settings.
 * Expressions, vector semantics and schema conversion have their own helpers.
 */
public final class MilvusCommandUtils {
    private MilvusCommandUtils() {
    }

    // Parameters, hints and WITH values

    public static Object getArg(AtomicInteger argIndex, AdapterRequest request) throws SQLException {
        int argIdx = argIndex.getAndIncrement();
        String argName = "arg" + (argIdx + 1);
        JdbcArg jdbcArg = request.getArgMap().get(argName);
        if (jdbcArg == null) {
            throw new SQLException(argName + " not found in request.");
        } else {
            return jdbcArg.getValue();
        }
    }

    public static Map<String, Object> readHints(AtomicInteger argIndex, AdapterRequest request, List<HintContext> hint) throws SQLException {
        Map<String, Object> hintMap = new LinkedCaseInsensitiveMap<>();
        if (hint == null || hint.isEmpty()) {
            return hintMap;
        }

        for (HintContext ctx : hint) {
            if (ctx == null || ctx.hints() == null) {
                continue;
            }

            for (HintItemContext it : ctx.hints().hintItem()) {
                String key = getIdentifier(it.name.getText());
                HintValueContext valCtx = it.value;
                Object value = null;

                if (valCtx != null) {
                    if ("?".equals(valCtx.getText())) {
                        value = getArg(argIndex, request);
                    } else if (valCtx.literal() != null) {
                        value = parseLiteral(valCtx.literal(), argIndex, request);
                    } else if (valCtx.identifier() != null) {
                        value = getIdentifier(valCtx.identifier().getText());
                    }
                }

                hintMap.put(key, value);
            }
        }

        return hintMap;
    }

    public static boolean hintAsBoolean(Map<String, Object> hints, String key, boolean defaultValue) {
        Object value = hints.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean boolValue) {
            return boolValue;
        }
        if (value instanceof Number numberValue) {
            return numberValue.longValue() != 0;
        }

        String text = value.toString();
        return StringUtils.equalsIgnoreCase(text, "true") || StringUtils.equalsIgnoreCase(text, "yes") || StringUtils.equalsIgnoreCase(text, "on") || "1".equals(text);
    }

    public static long hintAsLong(Map<String, Object> hints, String key, long defaultValue) throws SQLException {
        Object value = hints.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number numberValue) {
            return numberValue.longValue();
        }

        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            throw new SQLException("Invalid hint '" + key + "': " + value, e);
        }
    }

    public static Map<String, String> readStringProperties(AtomicInteger argIndex, AdapterRequest request, PropertiesListContext propertiesList) throws SQLException {
        Map<String, String> properties = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : readProperties(argIndex, request, propertiesList).entrySet()) {
            if (entry.getValue() == null) {
                throw new SQLException("Property '" + entry.getKey() + "' cannot be null; use DROP PROPERTIES to remove it.");
            }
            properties.put(entry.getKey(), String.valueOf(entry.getValue()));
        }

        return properties;
    }

    public static Map<String, Object> readProperties(AtomicInteger argIndex, AdapterRequest request, PropertiesListContext propertiesList) throws SQLException {
        Map<String, Object> properties = new LinkedHashMap<>();
        if (propertiesList == null) {
            return properties;
        }

        for (PropertyContext property : propertiesList.property()) {
            String key = getIdentifier(property.getChild(0).getText());
            if (StringUtils.isBlank(key) || "?".equals(key)) {
                throw new SQLException("Property key must be a non-empty name.");
            }

            ParseTree valueNode = property.getChild(2);
            String text = valueNode.getText();
            Object value;
            if ("?".equals(text)) {
                value = getArg(argIndex, request);
            } else if (text.startsWith("\"") || text.startsWith("'")) {
                value = getIdentifier(text);
            } else if ("true".equalsIgnoreCase(text) || "false".equalsIgnoreCase(text)) {
                value = Boolean.valueOf(text);
            } else if (property.INTEGER() != null || property.FLOAT_LITERAL() != null) {
                value = new BigDecimal(text);
            } else {
                value = getIdentifier(text);
            }
            if (value != null && !(value instanceof String || value instanceof Number || value instanceof Boolean)) {
                throw new SQLException("Property '" + key + "' requires a scalar value.");
            }
            properties.put(key, value);
        }
        return properties;
    }

    public static String propertiesToJson(Map<String, Object> properties) throws SQLException {
        try {
            return new com.google.gson.GsonBuilder().serializeNulls().create().toJson(properties);
        } catch (RuntimeException e) {
            throw new SQLException("Failed to serialize search properties.", e);
        }
    }

    // Identifiers and literal values

    public static String readDatabaseName(IdentifierContext ctx, MilvusCmd milvusCmd) {
        if (ctx == null) {
            return milvusCmd.getCatalog();
        } else {
            return getIdentifier(ctx.getText());
        }
    }

    public static String readName(IdentifierContext ctx) {
        if (ctx == null) {
            return null;
        }
        return getIdentifier(ctx.getText());
    }

    public static String getIdentifier(String nodeText) {
        if (nodeText == null || nodeText.length() < 2) {
            return nodeText;
        }
        char firstChar = nodeText.charAt(0);
        char endChar = nodeText.charAt(nodeText.length() - 1);

        if (firstChar == '"' && endChar == '"') {
            String unwrap = nodeText.substring(1, nodeText.length() - 1);
            if (unwrap.indexOf('"') >= 0 || unwrap.indexOf('\\') >= 0) {
                return unwrap.replace("\"\"", "\"").replace("\\\"", "\"");
            }
            return unwrap;
        } else if (firstChar == '\'' && endChar == '\'') {
            String unwrap = nodeText.substring(1, nodeText.length() - 1);
            if (unwrap.indexOf('\'') >= 0 || unwrap.indexOf('\\') >= 0) {
                return unwrap.replace("''", "'").replace("\\'", "'");
            }
            return unwrap;
        } else {
            return nodeText;
        }
    }

    public static Object parseLiteral(LiteralContext ctx, AtomicInteger argIndex, AdapterRequest request) throws SQLException {
        if (ctx == null || ctx.NULL() != null) {
            return null;
        }
        if (ctx.ARG() != null) {
            return getArg(argIndex, request);
        }
        if (ctx.INTEGER() != null) {
            return Long.parseLong(ctx.INTEGER().getText());
        }
        if (ctx.FLOAT_LITERAL() != null) {
            return Double.parseDouble(ctx.FLOAT_LITERAL().getText());
        }
        if (ctx.STRING_LITERAL() != null) {
            return getIdentifier(ctx.STRING_LITERAL().getText());
        }
        if (ctx.TRUE() != null) {
            return Boolean.TRUE;
        }
        if (ctx.FALSE() != null) {
            return Boolean.FALSE;
        }
        if (ctx.listLiteral() != null) {
            return parseListLiteral(ctx.listLiteral(), argIndex, request);
        }
        if (ctx.identifier() != null) {
            return getIdentifier(ctx.identifier().getText());
        }
        return ctx.getText();
    }

    public static Object parseListLiteral(ListLiteralContext ctx, AtomicInteger argIndex, AdapterRequest request) throws SQLException {
        if (ctx == null) {
            return null;
        }

        List<Object> list = new ArrayList<>();
        for (LiteralContext item : ctx.literal()) {
            list.add(parseLiteral(item, argIndex, request));
        }

        return list;
    }

    // LIMIT and OFFSET validation

    public static Long readLimit(Token limit, AtomicInteger argIndex, AdapterRequest request) throws SQLException {
        return readBound(limit, argIndex, request, "LIMIT", 1);
    }

    public static Long readBound(Token token, AtomicInteger argIndex, AdapterRequest request, String name, long minimum) throws SQLException {
        if (token == null) {
            return null;
        }
        Object value = token.getType() == MilvusParser.ARG ? getArg(argIndex, request) : new BigInteger(token.getText());
        return integerBound(value, name, minimum);
    }

    public static long integerBound(Object value, String name, long minimum) throws SQLException {
        if (!(value instanceof Number)) {
            throw new SQLException(name + " must be an integer.");
        }
        long result;
        try {
            if (value instanceof BigInteger) {
                result = ((BigInteger) value).longValueExact();
            } else if (value instanceof BigDecimal) {
                result = ((BigDecimal) value).longValueExact();
            } else if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
                result = ((Number) value).longValue();
            } else {
                throw new ArithmeticException("not an exact integer type");
            }
        } catch (ArithmeticException e) {
            throw new SQLException(name + " must be an integer within the BIGINT range.", e);
        }
        if (result < minimum) {
            throw new SQLException(name + " must be " + (minimum == 0 ? "greater than or equal to 0." : "greater than 0."));
        }
        return result;
    }

    public static Integer readTopK(Token limit, AtomicInteger argIndex, AdapterRequest request) throws SQLException {
        Long value = readLimit(limit, argIndex, request);
        if (value == null) {
            return null;
        }
        if (value > Integer.MAX_VALUE) {
            throw new SQLException("LIMIT is too large for a vector search: " + value);
        }
        return value.intValue();
    }

    // Convert bounded and paged search hits through the same result mapping.

    public static List<RowRecord> searchRecords(List<SearchResult> hits) {
        List<RowRecord> records = new ArrayList<>();
        if (hits == null) {
            return records;
        }
        for (SearchResult hit : hits) {
            RowRecord record = new RowRecord();
            hit.getEntity().forEach(record::put);
            if (StringUtils.isNotBlank(hit.getPrimaryKey())) {
                record.put(hit.getPrimaryKey(), hit.getId());
            }
            record.put("score", hit.getScore());
            records.add(record);
        }
        return records;
    }

    // SDK request settings

    public static void applyFetchSize(AdapterRequest request, QueryIteratorReq.QueryIteratorReqBuilder builder) {
        if (request.getFetchSize() > 0) {
            builder.batchSize(Math.min(request.getFetchSize(), Constant.MAX_BATCH_SIZE));
        }
    }

    public static void applyFetchSize(AdapterRequest request, SearchIteratorReqV2.SearchIteratorReqV2Builder builder) {
        if (request.getFetchSize() > 0) {
            builder.batchSize(Math.min(request.getFetchSize(), Constant.MAX_BATCH_SIZE));
        }
    }

    public static void applyConsistencyLevel(ConsistencyLevelEnum connLevel, QueryReq.QueryReqBuilder builder) {
        if (connLevel != null) {
            builder.consistencyLevel(ConsistencyLevel.valueOf(connLevel.name()));
        }
    }

    public static void applyConsistencyLevel(ConsistencyLevelEnum connLevel, SearchReq.SearchReqBuilder builder) {
        if (connLevel != null) {
            builder.consistencyLevel(ConsistencyLevel.valueOf(connLevel.name()));
        }
    }

    public static void applyConsistencyLevel(ConsistencyLevelEnum connLevel, QueryIteratorReq.QueryIteratorReqBuilder builder) {
        if (connLevel != null) {
            builder.consistencyLevel(ConsistencyLevel.valueOf(connLevel.name()));
        }
    }

    public static void applyConsistencyLevel(ConsistencyLevelEnum connLevel, SearchIteratorReqV2.SearchIteratorReqV2Builder builder) {
        if (connLevel != null) {
            builder.consistencyLevel(ConsistencyLevel.valueOf(connLevel.name()));
        }
    }

}
