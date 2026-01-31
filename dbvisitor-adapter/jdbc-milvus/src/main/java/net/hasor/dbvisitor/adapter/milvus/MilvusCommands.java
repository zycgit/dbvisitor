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
package net.hasor.dbvisitor.adapter.milvus;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser;
import net.hasor.dbvisitor.driver.*;

abstract class MilvusCommands {
    protected static final JdbcColumn COL_ID_LONG          = new JdbcColumn("ID", AdapterType.Long, "", "", "");
    protected static final JdbcColumn COL_DATABASE_STRING  = new JdbcColumn("DATABASE", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_PARTITION_STRING = new JdbcColumn("PARTITION", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_TABLE_STRING     = new JdbcColumn("TABLE", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_CREATE_STRING    = new JdbcColumn("CREATE SCRIPT", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_INDEX_STRING     = new JdbcColumn("INDEX", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_FIELD_STRING     = new JdbcColumn("FIELD", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_TYPE_STRING      = new JdbcColumn("TYPE", AdapterType.String, "", "", "");

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

    protected static Map<String, Object> readHints(AtomicInteger argIndex, AdapterRequest request, List<MilvusParser.HintContext> hint) throws SQLException {
        Map<String, Object> hintMap = new LinkedCaseInsensitiveMap<>();
        if (hint == null || hint.isEmpty()) {
            return hintMap;
        }

        for (MilvusParser.HintContext ctx : hint) {
            if (ctx == null || ctx.hints() == null) {
                continue;
            }
            for (MilvusParser.HintItemContext it : ctx.hints().hintItem()) {
                String key = getIdentifier(it.name.getText());
                MilvusParser.HintValueContext valCtx = it.value;
                Object value = null;

                if (valCtx != null) {
                    if (valCtx.literal() != null) {
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

    protected static Map<String, String> readProperties(AtomicInteger argIndex, AdapterRequest request, MilvusParser.PropertiesListContext propertiesList) throws SQLException {
        Map<String, String> properties = new LinkedHashMap<>();
        if (propertiesList == null) {
            return properties;
        }

        for (MilvusParser.PropertyContext property : propertiesList.property()) {
            List<MilvusParser.IdentifierContext> identifiers = property.identifier();
            List<?> stringLiterals = property.STRING_LITERAL();

            boolean keyIsString = identifiers == null || identifiers.isEmpty();
            String key;
            if (keyIsString) {
                if (stringLiterals == null || stringLiterals.isEmpty()) {
                    throw new SQLException("Property key is missing.");
                }
                key = getIdentifier(property.STRING_LITERAL(0).getText());
            } else {
                key = argAsName(argIndex, request, identifiers.get(0));
            }

            String value = null;
            int valueStringIndex = keyIsString ? 1 : 0;
            if (stringLiterals != null && stringLiterals.size() > valueStringIndex) {
                value = getIdentifier(property.STRING_LITERAL(valueStringIndex).getText());
            } else if (identifiers != null && identifiers.size() > (keyIsString ? 0 : 1)) {
                int index = keyIsString ? 0 : 1;
                value = argAsName(argIndex, request, identifiers.get(index));
            } else if (property.INTEGER() != null) {
                value = property.INTEGER().getText();
            } else if (property.FLOAT_LITERAL() != null) {
                value = property.FLOAT_LITERAL().getText();
            } else if (property.TRUE() != null) {
                value = "true";
            } else if (property.FALSE() != null) {
                value = "false";
            }

            if (StringUtils.isBlank(key)) {
                throw new SQLException("Property key is blank.");
            }
            properties.put(key, value);
        }

        return properties;
    }

    //

    protected static String argAsDbName(AtomicInteger argIndex, AdapterRequest request, MilvusParser.IdentifierContext ctx, MilvusCmd milvusCmd) throws SQLException {
        if (ctx == null) {
            return milvusCmd.getCatalog();
        }
        if (ctx.ARG() != null) {
            Object arg = getArg(argIndex, request);
            return arg == null ? null : arg.toString();
        }
        return getIdentifier(ctx.getText());
    }

    protected static String argAsName(AtomicInteger argIndex, AdapterRequest request, MilvusParser.IdentifierContext ctx) throws SQLException {
        if (ctx == null) {
            return null;
        }
        if (ctx.ARG() != null) {
            Object arg = getArg(argIndex, request);
            return arg == null ? null : arg.toString();
        }
        return getIdentifier(ctx.getText());
    }

    protected static String getIdentifier(String nodeText) {
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

    private static Object parseLiteral(MilvusParser.LiteralContext ctx, AtomicInteger argIndex, AdapterRequest request) throws SQLException {
        if (ctx == null) {
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
            List<Object> list = new ArrayList<>();
            for (MilvusParser.LiteralContext item : ctx.listLiteral().literal()) {
                list.add(parseLiteral(item, argIndex, request));
            }
            return list;
        }
        if (ctx.identifier() != null) {
            return getIdentifier(ctx.identifier().getText());
        }
        return ctx.getText();
    }

    //

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
        for (Map<String, Object> row : result) {
            cursor.pushData(row);
        }
        cursor.pushFinish();
        return cursor;
    }
}