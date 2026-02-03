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
package net.hasor.dbvisitor.dialect.provider;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.features.VectorSqlDialect;
import net.hasor.dbvisitor.lambda.core.MetricType;

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

    // --- VectorSqlDialect impl ---

    @Override
    public void addOrderByVector(String col, String colTerm, Object vector, String vectorTerm, MetricType metricType) {
        this.sorts.addSegment((delimited, dialect) -> {
            String field = StringUtils.isNotBlank(colTerm) ? colTerm : col;
            String vectorPlaceholder = formatValue(vector, vectorTerm);
            String scriptSource;
            String order;

            switch (metricType) {
                case L2:
                    scriptSource = "doc['" + field + "'].size() == 0 ? 0 : l2norm(params.vector, doc['" + field + "'])";
                    order = "asc";
                    break;
                case COSINE:
                    scriptSource = "doc['" + field + "'].size() == 0 ? 0 : cosineSimilarity(params.vector, doc['" + field + "'])";
                    order = "desc";
                    break;
                case IP:
                    scriptSource = "doc['" + field + "'].size() == 0 ? 0 : dotProduct(params.vector, doc['" + field + "'])";
                    order = "desc";
                    break;
                case HAMMING:
                    scriptSource = "doc['" + field + "'].size() == 0 ? 0 : l1norm(params.vector, doc['" + field + "'])";
                    order = "asc";
                    break;
                default:
                    throw new UnsupportedOperationException("MetricType " + metricType + " is not supported by Elastic7Dialect.");
            }

            return "{ \"_script\": { \"type\": \"number\", \"script\": { \"source\": \"" + scriptSource + "\", \"params\": { \"vector\": " + vectorPlaceholder + " } }, \"order\": \"" + order + "\" } }";
        });
    }

    @Override
    public void addConditionForVectorRange(ConditionLogic logic, String col, String colTerm, Object vector, String vectorTerm, Object threshold, String thresholdTerm, MetricType metricType) {
        this.conditions.addSegment((delimited, dialect) -> {
            String field = StringUtils.isNotBlank(colTerm) ? colTerm : col;
            String vecVal = formatValue(vector, vectorTerm);
            String thrVal = formatValue(threshold, thresholdTerm);

            String scriptCode;
            switch (metricType) {
                case L2:
                    scriptCode = "l2norm(params.vector, doc['" + field + "']) < " + thrVal;
                    break;
                case COSINE:
                    scriptCode = "(1 - cosineSimilarity(params.vector, doc['" + field + "'])) < " + thrVal;
                    break;
                case IP:
                    scriptCode = "dotProduct(params.vector, doc['" + field + "']) < " + thrVal;
                    break;
                case HAMMING:
                    scriptCode = "l1norm(params.vector, doc['" + field + "']) < " + thrVal;
                    break;
                default:
                    throw new UnsupportedOperationException("MetricType " + metricType + " is not supported by Elastic7Dialect range query.");
            }

            return "{ \"script\": { \"script\": { \"source\": \"" + scriptCode + "\", \"params\": { \"vector\": " + vecVal + " } } } }";
        });
    }
}
