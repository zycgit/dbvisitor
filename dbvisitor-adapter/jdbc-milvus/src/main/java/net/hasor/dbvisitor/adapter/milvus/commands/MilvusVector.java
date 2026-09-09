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
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import io.milvus.v2.common.IndexParam.MetricType;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.*;
import net.hasor.dbvisitor.driver.AdapterRequest;
import org.antlr.v4.runtime.tree.ParseTree;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.*;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusExpression.parseTerm;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusExpression.parseWhere;

/** Single-vector values, distance operators and supported range predicates. */
public final class MilvusVector {
    private MilvusVector() {
    }

    public static Object readVectorValue(VectorValueContext ctx, AtomicInteger argIndex, AdapterRequest request) throws SQLException {
        if (ctx == null)
            throw new SQLException("ORDER BY requires a vector distance expression.");
        if (ctx.ARG() != null) {
            return getArg(argIndex, request);
        }
        if (ctx.STRING_LITERAL() != null)
            return getIdentifier(ctx.STRING_LITERAL().getText());
        return parseListLiteral(ctx.listLiteral(), argIndex, request);
    }

    // Vector range expressions

    public static class VectorRangeExpr {
        public String     fieldName;
        public Object     vectorValue;
        public double     radius;
        public MetricType metricType;
        public MilvusExpression.Filter scalarFilter = new MilvusExpression.Filter("", java.util.Collections.emptyMap());
    }

    public static MetricType vectorMetric(DistanceOperatorContext operator) throws SQLException {
        if (operator.LT_MINUS_GT() != null) {
            return MetricType.L2;
        }
        if (operator.LT_EQ_GT() != null) {
            return MetricType.COSINE;
        }
        if (operator.LT_HASH_GT() != null) {
            return MetricType.IP;
        }
        if (operator.TILDE_EQ() != null)
            return MetricType.HAMMING;
        if (operator.LT_PCT_GT() != null)
            return MetricType.JACCARD;
        if (operator.LT_Q_GT() != null)
            return MetricType.BM25;
        throw new SQLFeatureNotSupportedException("Unsupported vector distance operator: " + operator.getText());
    }

    public static boolean isVectorRange(ExpressionContext ctx) {
        return containsVectorExpression(ctx);
    }

    private static boolean containsVectorExpression(ParseTree node) {
        if (node == null) {
            return false;
        }
        if (node instanceof TermContext && ((TermContext) node).distanceOperator() != null) {
            return true;
        }
        if (node instanceof FuncExpressionContext && "vector_range".equalsIgnoreCase(((FuncExpressionContext) node).funcName.getText())) {
            return true;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            if (containsVectorExpression(node.getChild(i))) {
                return true;
            }
        }
        return false;
    }

    public static VectorRangeExpr parseVectorRange(ExpressionContext ctx, AtomicInteger argIndex, AdapterRequest request) throws SQLException {
        // Structural detection must not consume scalar parameters.
        if (!isVectorRange(ctx)) {
            return null;
        }
        if (ctx instanceof ParenExpressionContext) {
            return parseVectorRange(((ParenExpressionContext) ctx).expression(), argIndex, request);
        }
        if (ctx instanceof LogicalExpressionContext) {
            return parseRangeConjunction((LogicalExpressionContext) ctx, argIndex, request);
        }
        VectorRangeExpr range = new VectorRangeExpr();
        ExpressionContext radiusExpression;
        if (ctx instanceof FuncExpressionContext function) {
            if (function.funcArgs() == null || function.funcArgs().expression().size() != 3) {
                throw new SQLException("vector_range requires exactly three arguments: field, vector, radius.");
            }
            List<ExpressionContext> args = function.funcArgs().expression();
            TermContext field = rangeTerm(args.get(0));
            if (field.identifier() == null || field.distanceOperator() != null || "?".equals(field.getText())) {
                throw new SQLException("vector_range requires a vector field name.");
            }
            range.fieldName = getIdentifier(field.identifier().getText());
            range.vectorValue = parseTerm(rangeTerm(args.get(1)), argIndex, request);
            range.metricType = MetricType.L2;
            radiusExpression = args.get(2);
        } else if (ctx instanceof ComparatorExpressionContext comparison) {
            TermContext vector = rangeTerm(comparison.expression(0));
            if (vector.distanceOperator() == null) {
                throw new SQLFeatureNotSupportedException("The vector expression must be on the left of the range comparison.");
            }
            range.fieldName = getIdentifier(vector.identifier().getText());
            range.metricType = vectorMetric(vector.distanceOperator());
            boolean distance = range.metricType == MetricType.L2 || range.metricType == MetricType.HAMMING || range.metricType == MetricType.JACCARD;
            String expectedComparison = distance ? "<" : ">";
            if (!expectedComparison.equals(comparison.getChild(1).getText())) {
                throw new SQLFeatureNotSupportedException(range.metricType + " range search requires '" + expectedComparison + "'; other comparison directions are not supported.");
            }
            VectorValueContext value = vector.vectorValue();
            range.vectorValue = readVectorValue(value, argIndex, request);
            radiusExpression = comparison.expression(1);
        } else {
            throw new SQLFeatureNotSupportedException("Unsupported vector range expression. Use a distance comparison combined with scalar conditions using AND.");
        }
        if (range.vectorValue == null) {
            throw new SQLException("Range search requires a non-empty query vector.");
        }
        Object radiusValue = parseTerm(rangeTerm(radiusExpression), argIndex, request);
        if (!(radiusValue instanceof Number)) {
            throw new SQLException("Range radius must be a finite number.");
        }
        range.radius = ((Number) radiusValue).doubleValue();
        boolean finite = Double.isFinite(range.radius) && Float.isFinite((float) range.radius);
        boolean invalidL2 = range.metricType == MetricType.L2 && range.radius < 0;
        boolean invalidCosine = range.metricType == MetricType.COSINE && (range.radius < -1 || range.radius > 1);
        if (!finite || invalidL2 || invalidCosine) {
            throw new SQLException("Invalid range radius for " + range.metricType + ": " + radiusValue);
        }
        return range;
    }

    private static VectorRangeExpr parseRangeConjunction(LogicalExpressionContext logical, AtomicInteger argIndex, AdapterRequest request) throws SQLException {
        String operator = logical.getChild(1).getText();
        boolean conjunction = "AND".equalsIgnoreCase(operator) || "&&".equals(operator);
        boolean leftRange = isVectorRange(logical.expression(0));
        boolean rightRange = isVectorRange(logical.expression(1));
        if (!conjunction || (leftRange && rightRange)) {
            throw new SQLFeatureNotSupportedException("A range search supports one vector range combined with scalar conditions using AND.");
        }
        // Consume parameters in SQL order, even when the vector range is on the right.
        VectorRangeExpr range;
        MilvusExpression.Filter scalar;
        if (leftRange) {
            range = parseVectorRange(logical.expression(0), argIndex, request);
            scalar = parseWhere(logical.expression(1), argIndex, request);
        } else {
            scalar = parseWhere(logical.expression(0), argIndex, request);
            range = parseVectorRange(logical.expression(1), argIndex, request);
        }
        range.scalarFilter = range.scalarFilter.and(scalar);
        return range;
    }

    private static TermContext rangeTerm(ExpressionContext expression) throws SQLException {
        while (expression instanceof ParenExpressionContext) {
            expression = ((ParenExpressionContext) expression).expression();
        }
        if (!(expression instanceof TermExpressionContext)) {
            throw new SQLFeatureNotSupportedException("Range arguments must be field names, literals or parameters.");
        }
        return ((TermExpressionContext) expression).term();
    }

}
