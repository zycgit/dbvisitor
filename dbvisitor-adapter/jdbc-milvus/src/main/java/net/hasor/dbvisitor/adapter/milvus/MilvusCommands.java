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
import java.util.stream.Collectors;
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.*;
import net.hasor.dbvisitor.driver.*;

abstract class MilvusCommands {
    protected static final JdbcColumn COL_ID_LONG          = new JdbcColumn("ID", AdapterType.Long, "", "", "");
    protected static final JdbcColumn COL_DATABASE_STRING  = new JdbcColumn("DATABASE", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_PARTITION_STRING = new JdbcColumn("PARTITION", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_TABLE_STRING     = new JdbcColumn("TABLE", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_USER_STRING      = new JdbcColumn("USER", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_ROLE_STRING      = new JdbcColumn("ROLE", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_INDEX_STRING     = new JdbcColumn("INDEX", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_FIELD_STRING     = new JdbcColumn("FIELD", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_TYPE_STRING      = new JdbcColumn("TYPE", AdapterType.String, "", "", "");
    protected static final JdbcColumn COL_COUNT_LONG       = new JdbcColumn("COUNT", AdapterType.Long, "", "", "");

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

    protected static Map<String, Object> readHints(AtomicInteger argIndex, AdapterRequest request, List<HintContext> hint) throws SQLException {
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

    protected static Map<String, String> readProperties(AtomicInteger argIndex, AdapterRequest request, PropertiesListContext propertiesList) throws SQLException {
        Map<String, String> properties = new LinkedHashMap<>();
        if (propertiesList == null) {
            return properties;
        }

        for (PropertyContext property : propertiesList.property()) {
            List<IdentifierContext> identifiers = property.identifier();
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

    protected static String argAsDbName(AtomicInteger argIndex, AdapterRequest request, IdentifierContext ctx, MilvusCmd milvusCmd) throws SQLException {
        if (ctx == null) {
            return milvusCmd.getCatalog();
        }
        if (ctx.ARG() != null) {
            Object arg = getArg(argIndex, request);
            return arg == null ? null : arg.toString();
        }
        return getIdentifier(ctx.getText());
    }

    protected static String argAsName(AtomicInteger argIndex, AdapterRequest request, IdentifierContext ctx) throws SQLException {
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

    protected static Object parseLiteral(LiteralContext ctx, AtomicInteger argIndex, AdapterRequest request) throws SQLException {
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
            for (LiteralContext item : ctx.listLiteral().literal()) {
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

    //

    protected static Object parseListLiteral(ListLiteralContext ctx, AtomicInteger argIndex, AdapterRequest request) throws SQLException {
        if (ctx == null)
            return null;
        List<Object> list = new ArrayList<>();
        for (LiteralContext item : ctx.literal()) {
            list.add(parseLiteral(item, argIndex, request));
        }
        return list;
    }

    protected static String rebuildExpression(AtomicInteger argIndex, AdapterRequest request, ExpressionContext ctx) throws SQLException {
        if (ctx instanceof ParenExpressionContext) {
            return "(" + rebuildExpression(argIndex, request, ((ParenExpressionContext) ctx).expression()) + ")";
        }
        if (ctx instanceof NotExpressionContext) {
            return "not " + rebuildExpression(argIndex, request, ((NotExpressionContext) ctx).expression());
        }
        if (ctx instanceof BinaryExpressionContext) {
            BinaryExpressionContext binaryCtx = (BinaryExpressionContext) ctx;
            String op = binaryCtx.getChild(1).getText();
            return rebuildExpression(argIndex, request, binaryCtx.expression(0)) + " " + op + " " + rebuildExpression(argIndex, request, binaryCtx.expression(1));
        }
        if (ctx instanceof ComparatorExpressionContext) {
            ComparatorExpressionContext compCtx = (ComparatorExpressionContext) ctx;
            String op = compCtx.getChild(1).getText();
            if ("=".equals(op)) {
                op = "==";
            }
            return rebuildExpression(argIndex, request, compCtx.expression(0)) + " " + op + " " + rebuildExpression(argIndex, request, compCtx.expression(1));
        }
        if (ctx instanceof LogicalExpressionContext) {
            LogicalExpressionContext logicCtx = (LogicalExpressionContext) ctx;
            String op = logicCtx.getChild(1).getText();
            return rebuildExpression(argIndex, request, logicCtx.expression(0)) + " " + op + " " + rebuildExpression(argIndex, request, logicCtx.expression(1));
        }
        if (ctx instanceof InExpressionContext) {
            InExpressionContext inCtx = (InExpressionContext) ctx;
            String field = inCtx.fieldName.getText();
            Object val;
            if (inCtx.ARG() != null) {
                val = getArg(argIndex, request);
            } else {
                List<Object> list = new ArrayList<>();
                for (LiteralContext item : inCtx.listLiteral().literal()) {
                    list.add(parseLiteral(item, argIndex, request));
                }
                val = list;
            }
            return field + " in " + resultValueToString(val);
        }
        if (ctx instanceof LikeExpressionContext) {
            LikeExpressionContext likeCtx = (LikeExpressionContext) ctx;
            String field = likeCtx.fieldName.getText();
            Object val;
            if (likeCtx.ARG() != null) {
                val = getArg(argIndex, request);
            } else {
                val = getIdentifier(likeCtx.pattern.getText());
            }
            return field + " like " + resultValueToString(val);
        }
        if (ctx instanceof TermExpressionContext) {
            TermExpressionContext termCtx = (TermExpressionContext) ctx;
            return rebuildTerm(argIndex, request, termCtx.term());
        }
        return ctx.getText();
    }

    private static String rebuildTerm(AtomicInteger argIndex, AdapterRequest request, TermContext ctx) throws SQLException {
        if (ctx.identifier() != null) {
            return ctx.identifier().getText();
        }
        if (ctx.literal() != null) {
            Object val = parseLiteral(ctx.literal(), argIndex, request);
            return resultValueToString(val);
        }
        return ctx.getText();
    }

    private static String resultValueToString(Object val) {
        if (val instanceof String) {
            return "\"" + val + "\"";
        }
        if (val instanceof List) {
            return "[" + ((List<?>) val).stream().map(MilvusCommands::resultValueToString).collect(Collectors.joining(", ")) + "]";
        }
        return String.valueOf(val);
    }

    protected static java.util.List<Float> toFloatList(Object obj) throws SQLException {
        if (obj == null) {
            return null;
        }
        if (!(obj instanceof java.util.List)) {
            throw new SQLException("The argument must be a List for vector search.");
        }
        java.util.List<?> list = (java.util.List<?>) obj;
        if (list.isEmpty()) {
            return new java.util.ArrayList<>();
        }

        java.util.List<Float> floatList = new java.util.ArrayList<>(list.size());
        for (Object item : list) {
            if (item instanceof Number) {
                floatList.add(((Number) item).floatValue());
            } else {
                try {
                    floatList.add(Float.parseFloat(item.toString()));
                } catch (NumberFormatException e) {
                    throw new SQLException("Failed to parse vector item to Float: " + item);
                }
            }
        }
        return floatList;
    }

    protected static class VectorRangeExpr {
        public String  fieldName;
        public List<?> vectorValue;
        public Double  radius;
        public String  scalarFilter = "";
    }

    protected static VectorRangeExpr parseVectorRange(ExpressionContext ctx, AtomicInteger argIndex, AdapterRequest request) throws SQLException {
        if (ctx == null)
            return null;

        // check if (VectorExpr) AND (ScalarExpr)
        if (ctx instanceof LogicalExpressionContext) {
            LogicalExpressionContext logCtx = (LogicalExpressionContext) ctx;
            if ("AND".equalsIgnoreCase(logCtx.getChild(1).getText()) || "&&".equalsIgnoreCase(logCtx.getChild(1).getText())) {
                VectorRangeExpr left = parseVectorRange(logCtx.expression(0), argIndex, request);
                if (left != null) {
                    String rightExpr = rebuildExpression(argIndex, request, logCtx.expression(1));
                    left.scalarFilter = StringUtils.isBlank(left.scalarFilter) ? rightExpr : "(" + left.scalarFilter + ") && (" + rightExpr + ")";
                    return left;
                }
                VectorRangeExpr right = parseVectorRange(logCtx.expression(1), argIndex, request);
                if (right != null) {
                    String leftExpr = rebuildExpression(argIndex, request, logCtx.expression(0));
                    right.scalarFilter = StringUtils.isBlank(right.scalarFilter) ? leftExpr : "(" + right.scalarFilter + ") && (" + leftExpr + ")";
                    return right;
                }
            }
        }

        // Check if Comparator (Vec Term < Radius)
        if (ctx instanceof ComparatorExpressionContext) {
            ComparatorExpressionContext compCtx = (ComparatorExpressionContext) ctx;
            if (compCtx.getChildCount() == 3 && "<".equals(compCtx.getChild(1).getText())) {
                ExpressionContext leftExpr = compCtx.expression(0); // expect TermExpression -> VectorTerm
                ExpressionContext rightExpr = compCtx.expression(1); // expect Radius

                if (leftExpr instanceof TermExpressionContext) {
                    TermContext termCtx = ((TermExpressionContext) leftExpr).term();
                    // Check if it is vector term (identifier distanceOperator vectorValue)
                    if (termCtx.distanceOperator() != null) {
                        VectorRangeExpr res = new VectorRangeExpr();
                        res.fieldName = getIdentifier(termCtx.identifier().getText());
                        // parse vector
                        if (termCtx.vectorValue().listLiteral() != null) {
                            res.vectorValue = (List<?>) parseListLiteral(termCtx.vectorValue().listLiteral(), argIndex, request);
                        } else if (termCtx.vectorValue().ARG() != null) {
                            res.vectorValue = (List<?>) getArg(argIndex, request);
                        }

                        // parse radius
                        String radiusStr = rebuildExpression(argIndex, request, rightExpr); // should be a number string
                        try {
                            res.radius = Double.parseDouble(radiusStr);
                        } catch (NumberFormatException e) {
                            // ignore or handle
                        }
                        return res;
                    }
                }
            }
        }
        // Handle ParenExpressionContext for recursive search
        if (ctx instanceof ParenExpressionContext) {
            return parseVectorRange(((ParenExpressionContext) ctx).expression(), argIndex, request);
        }

        return null;
    }
}