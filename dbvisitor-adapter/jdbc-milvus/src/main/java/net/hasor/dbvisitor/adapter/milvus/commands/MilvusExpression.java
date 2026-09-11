/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus.commands;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import io.milvus.v2.utils.VectorUtils;
import net.hasor.dbvisitor.adapter.milvus.mapping.MilvusSchema;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.*;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.AdapterType;
import net.hasor.dbvisitor.driver.JdbcArg;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.*;

/** SQL structure and separately bound, typed Milvus filter values. */
public final class MilvusExpression {
    private MilvusExpression() {
    }

    public record Filter(String expression, Map<String, Object> parameters) {
        public Filter {
            parameters = Collections.unmodifiableMap(new LinkedHashMap<>(parameters));
        }

        public Filter and(Filter other) {
            if (expression.isEmpty()) {
                return other;
            }
            if (other.expression.isEmpty()) {
                return this;
            }
            Map<String, Object> values = new LinkedHashMap<>(parameters);
            values.putAll(other.parameters);
            return new Filter("(" + expression + ") && (" + other.expression + ")", values);
        }
    }

    // Preserve SQL structure; JDBC values never become expression text.

    public static Filter parseWhere(ExpressionContext ctx, AtomicInteger argIndex, AdapterRequest request) throws SQLException {
        Map<String, Object> parameters = new LinkedHashMap<>();
        String expression = ctx == null ? "" : rebuildExpression(argIndex, request, ctx, parameters);
        return new Filter(expression, parameters);
    }

    private static String rebuildExpression(AtomicInteger argIndex, AdapterRequest request, ExpressionContext ctx, Map<String, Object> parameters) throws SQLException {
        if (ctx instanceof NullExpressionContext nullCtx) {
            return readName(nullCtx.fieldName) + (nullCtx.NOT() == null ? " is null" : " is not null");
        }
        if (ctx instanceof ParenExpressionContext) {
            return "(" + rebuildExpression(argIndex, request, ((ParenExpressionContext) ctx).expression(), parameters) + ")";
        }

        if (ctx instanceof NotExpressionContext) {
            ExpressionContext operand = ((NotExpressionContext) ctx).expression();
            String expression = rebuildExpression(argIndex, request, operand, parameters);
            // Milvus NOT binds more tightly than SQL NOT; keep the SQL predicate as its operand.
            return operand instanceof ParenExpressionContext ? "not " + expression : "not (" + expression + ")";
        }

        if (ctx instanceof BinaryExpressionContext binaryCtx) {
            String op = binaryCtx.getChild(1).getText();
            return rebuildExpression(argIndex, request, binaryCtx.expression(0), parameters) + " " + op + " " + rebuildExpression(argIndex, request, binaryCtx.expression(1), parameters);
        }

        if (ctx instanceof ComparatorExpressionContext compCtx) {
            String op = compCtx.getChild(1).getText();
            if ("=".equals(op)) {
                op = "==";
            } else if ("<>".equals(op)) {
                op = "!=";
            }
            return rebuildExpression(argIndex, request, compCtx.expression(0), parameters) + " " + op + " " + rebuildExpression(argIndex, request, compCtx.expression(1), parameters);
        }

        if (ctx instanceof LogicalExpressionContext logicCtx) {
            String op = logicCtx.getChild(1).getText();
            return rebuildExpression(argIndex, request, logicCtx.expression(0), parameters) + " " + op + " " + rebuildExpression(argIndex, request, logicCtx.expression(1), parameters);
        }

        if (ctx instanceof InExpressionContext inCtx) {
            String field = inCtx.fieldName.getText();
            int firstArg = argIndex.get();
            Object val;
            if (inCtx.ARG() != null) {
                val = filterArgument(argIndex, request);
            } else {
                List<Object> list = new ArrayList<>();
                List<LiteralContext> literals = null;
                if (inCtx.listLiteral() != null) {
                    literals = inCtx.listLiteral().literal();
                } else if (inCtx.parenListLiteral() != null) {
                    literals = inCtx.parenListLiteral().literal();
                }

                if (literals != null) {
                    for (LiteralContext item : literals) {
                        list.add(filterLiteral(item, argIndex, request));
                    }
                }
                val = list;
            }
            String operator = inCtx.NOT() == null ? " in " : " not in ";
            return field + operator + renderValue(val, firstArg, argIndex, parameters);
        }

        if (ctx instanceof LikeExpressionContext likeCtx) {
            String field = likeCtx.fieldName.getText();
            int firstArg = argIndex.get();
            Object val;
            if (likeCtx.ARG() != null) {
                val = filterArgument(argIndex, request);
            } else {
                val = getIdentifier(likeCtx.pattern.getText());
            }
            return field + " like " + renderValue(val, firstArg, argIndex, parameters);
        }

        if (ctx instanceof BetweenExpressionContext between) {
            String field = between.fieldName.getText();
            String lower = rebuildTerm(argIndex, request, between.lower, parameters);
            String upper = rebuildTerm(argIndex, request, between.upper, parameters);
            if (between.NOT() != null) {
                return "(" + field + " < " + lower + " OR " + field + " > " + upper + ")";
            }
            return "(" + field + " >= " + lower + " AND " + field + " <= " + upper + ")";
        }

        if (ctx instanceof FuncExpressionContext funcCtx) {
            String funcName = funcCtx.funcName.getText();
            StringBuilder args = new StringBuilder();
            if (funcCtx.funcArgs() != null) {
                for (ExpressionContext e : funcCtx.funcArgs().expression()) {
                    if (args.length() > 0) {
                        args.append(", ");
                    }
                    args.append(rebuildExpression(argIndex, request, e, parameters));
                }
            }
            return funcName + "(" + args + ")";
        }

        if (ctx instanceof TermExpressionContext termCtx) {
            return rebuildTerm(argIndex, request, termCtx.term(), parameters);
        }
        return ctx.getText();
    }

    public static Object parseTerm(TermContext ctx, AtomicInteger argIndex, AdapterRequest request) throws SQLException {
        if (ctx.identifier() != null) {
            return getIdentifier(ctx.identifier().getText());
        }

        if (ctx.literal() != null) {
            return parseLiteral(ctx.literal(), argIndex, request);
        }

        if (ctx.ARG() != null || "?".equals(ctx.getText().trim())) {
            return getArg(argIndex, request);
        }

        return ctx.getText();
    }

    private static String rebuildTerm(AtomicInteger argIndex, AdapterRequest request, TermContext ctx, Map<String, Object> parameters) throws SQLException {
        if (ctx.identifier() != null) {
            return ctx.identifier().getText();
        }
        if (ctx.literal() != null) {
            int firstArg = argIndex.get();
            Object val = filterLiteral(ctx.literal(), argIndex, request);
            return renderValue(val, firstArg, argIndex, parameters);
        }

        String text = ctx.getText();
        if (ctx.ARG() != null || "?".equals(text.trim())) {
            int firstArg = argIndex.get();
            Object val = filterArgument(argIndex, request);
            return renderValue(val, firstArg, argIndex, parameters);
        }
        return text;
    }

    // Lists containing JDBC placeholders bind as one array template, including literal members.
    private static Object filterLiteral(LiteralContext ctx, AtomicInteger argIndex, AdapterRequest request) throws SQLException {
        if (ctx.ARG() != null) {
            return filterArgument(argIndex, request);
        }
        if (ctx.listLiteral() != null) {
            List<Object> values = new ArrayList<>();
            for (LiteralContext item : ctx.listLiteral().literal()) {
                values.add(filterLiteral(item, argIndex, request));
            }
            return values;
        }
        return parseLiteral(ctx, argIndex, request);
    }

    private static String renderValue(Object value, int firstArg, AtomicInteger argIndex, Map<String, Object> parameters) throws SQLException {
        if (argIndex.get() == firstArg) {
            return literalToString(value);
        }
        String name = "arg" + (firstArg + 1);
        try {
            // Validate with the SDK serializer before any query or mutation is issued.
            VectorUtils.deduceAndCreateTemplateValue(value);
        } catch (RuntimeException e) {
            throw new SQLException("Unsupported Milvus filter parameter " + name + ": " + e.getMessage(), e);
        }
        parameters.put(name, value);
        return "{" + name + "}";
    }

    private static Object filterArgument(AtomicInteger argIndex, AdapterRequest request) throws SQLException {
        String name = "arg" + (argIndex.get() + 1);
        Object value = getArg(argIndex, request);
        JdbcArg argument = request.getArgMap().get(name);
        try {
            // Explicit VARCHAR bindings are text even when the supplied Java object is not String.
            if (value != null && AdapterType.String.equals(argument.getType())) {
                value = value instanceof java.util.Date date ? MilvusSchema.temporalText(date) : String.valueOf(value);
            }
            return templateValue(value);
        } catch (IllegalArgumentException | ArithmeticException e) {
            throw new SQLException("Invalid Milvus filter parameter " + name + ": " + e.getMessage(), e);
        }
    }

    private static Object templateValue(Object value) throws SQLException {
        if (value == null) {
            throw new SQLException("Milvus filter templates do not support null; use IS NULL or IS NOT NULL.");
        }
        if (value instanceof java.util.Date date) {
            return MilvusSchema.temporalText(date);
        }
        if (value instanceof String || value instanceof Boolean || value instanceof Integer || value instanceof Long) {
            return value;
        }
        if (value instanceof CharSequence || value instanceof Character) {
            return value.toString();
        }
        if (value instanceof Byte || value instanceof Short) {
            return ((Number) value).longValue();
        }
        if (value instanceof BigInteger integer) {
            return integer.longValueExact();
        }
        if (value instanceof Float || value instanceof Double || value instanceof BigDecimal) {
            double number = ((Number) value).doubleValue();
            if (!Double.isFinite(number)) {
                throw new SQLException("Milvus filter parameters require finite numbers.");
            }
            return number;
        }
        if (value instanceof java.sql.Array array) {
            return templateValue(array.getArray());
        }
        if (value instanceof List<?> || value.getClass().isArray()) {
            List<Object> values = new ArrayList<>();
            if (value instanceof List<?> list) {
                for (Object item : list) {
                    values.add(templateValue(item));
                }
            } else {
                for (int i = 0; i < Array.getLength(value); i++) {
                    values.add(templateValue(Array.get(value, i)));
                }
            }
            return Collections.unmodifiableList(values);
        }
        throw new SQLException("Unsupported Milvus filter parameter type: " + value.getClass().getName());
    }

    // Only SQL literals reach this renderer, never JDBC-bound values.
    private static String literalToString(Object val) {
        if (val instanceof String) {
            return quoteExpressionString((String) val);
        }
        if (val instanceof List) {
            return "[" + ((List<?>) val).stream().map(MilvusExpression::literalToString).collect(Collectors.joining(", ")) + "]";
        }
        return String.valueOf(val);
    }

    private static String quoteExpressionString(String value) {
        StringBuilder escaped = new StringBuilder(value.length() + 2);
        escaped.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
            }
        }
        return escaped.append('"').toString();
    }

}
