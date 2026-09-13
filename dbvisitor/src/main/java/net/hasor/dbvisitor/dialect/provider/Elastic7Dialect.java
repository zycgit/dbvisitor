/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dialect.provider;
import java.sql.SQLException;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.features.VectorSqlDialect;
import net.hasor.dbvisitor.lambda.core.MetricType;
import net.hasor.dbvisitor.lambda.segment.Segment;

/**
 * ES7 方言
 * @author 赵永春 (zyc@hasor.net)
 * @version 2025-12-31
 */
public class Elastic7Dialect extends AbstractElasticDialect implements VectorSqlDialect {
    public static final SqlDialect DEFAULT = new Elastic7Dialect();

    @Override
    public AbstractElasticDialect newBuilder() {
        return new Elastic7Dialect();
    }

    @Override
    protected String getSearchEndpoint() {
        return buildPath("_search");
    }

    @Override
    protected String getInsertEndpoint() {
        return buildPath("_doc");
    }

    @Override
    protected String getUpdateEndpoint() {
        return buildPath("_update_by_query");
    }

    @Override
    protected String getDeleteEndpoint() {
        return buildPath("_delete_by_query");
    }

    private String buildPath(String action) {
        StringBuilder sb = new StringBuilder();
        sb.append("/");
        sb.append(StringUtils.isBlank(this.index) ? "*" : this.index);
        if (StringUtils.isNotBlank(action)) {
            sb.append("/").append(action);
        }
        return sb.toString();
    }

    private Segment vectorScore;

    @Override
    public void clearAll() {
        super.clearAll();
        this.vectorScore = null;
    }

    @Override
    protected String renderSelectQuery(boolean useQualifier) throws SQLException {
        String query = super.renderSelectQuery(useQualifier);
        if (this.vectorScore == null) {
            return query;
        }
        return "{ \"script_score\": { \"query\": " + query + ", \"script\": " + this.vectorScore.getSqlSegment(useQualifier, this) + " } }";
    }

    @Override
    public void addOrderByVector(String col, String colTerm, Object vector, String vectorTerm, MetricType metricType) {
        if (this.vectorScore != null) {
            throw new UnsupportedOperationException("Elasticsearch supports one vector score per query.");
        }
        String source = scoreSource(metricType);
        String field = StringUtils.isNotBlank(colTerm) ? colTerm : col;
        this.vectorScore = (delimited, dialect) -> vectorScript(source, field, vector, vectorTerm, null, null);
        this.sorts.addSegment((delimited, dialect) -> "{ \"_score\": { \"order\": \"desc\" } }");
    }

    @Override
    public void addConditionForVectorRange(ConditionLogic logic, String col, String colTerm, Object vector, String vectorTerm, Object threshold, String thresholdTerm, MetricType metricType) {
        String comparison = distanceSource(metricType) + " < params.threshold";
        String source = "if (doc[params.field].size() == 0) return 0; return (" + comparison + ") ? 1 : 0;";
        String field = StringUtils.isNotBlank(colTerm) ? colTerm : col;
        addPredicate(logic, (delimited, dialect) -> "{ \"script_score\": { \"query\": { \"match_all\": {} }, \"script\": " + vectorScript(source, field, vector, vectorTerm, threshold, thresholdTerm) + ", \"min_score\": 1 } }");
    }

    private String vectorScript(String source, String field, Object vector, String vectorTerm, Object threshold, String thresholdTerm) {
        StringBuilder script = new StringBuilder("{ \"source\": \"").append(source).append("\", \"params\": {");
        script.append("\"field\": ").append(formatValue(field, null));
        script.append(", \"vector\": ").append(formatValue(vector, vectorTerm));
        if (threshold != null || StringUtils.isNotBlank(thresholdTerm)) {
            script.append(", \"threshold\": ").append(formatValue(threshold, thresholdTerm));
        }

        return script.append("} }").toString();
    }

    private String scoreSource(MetricType metric) {
        String prefix = "if (doc[params.field].size() == 0) return 0; ";
        return switch (metric) {
            case L2 -> prefix + "return 1.0 / (1.0 + l2norm(params.vector, params.field));";
            case COSINE -> prefix + "return Math.max(0.0, 1.0 + cosineSimilarity(params.vector, params.field));";
            case IP -> prefix + "double dot = dotProduct(params.vector, params.field); return dot >= 0 ? dot + 1.0 : 1.0 / (1.0 - dot);";
            default -> throw unsupportedMetric(metric);
        };
    }

    private String distanceSource(MetricType metric) {
        return switch (metric) {
            case L2 -> "l2norm(params.vector, params.field)";
            case COSINE -> "(1.0 - cosineSimilarity(params.vector, params.field))";
            case IP -> "(-dotProduct(params.vector, params.field))";
            default -> throw unsupportedMetric(metric);
        };
    }

    private UnsupportedOperationException unsupportedMetric(MetricType metric) {
        return new UnsupportedOperationException("MetricType " + metric + " is not supported by Elasticsearch dense_vector.");
    }
}
