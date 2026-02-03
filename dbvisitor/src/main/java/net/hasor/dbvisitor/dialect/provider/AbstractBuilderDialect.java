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
import net.hasor.dbvisitor.dialect.SqlCommandBuilder;
import net.hasor.dbvisitor.dialect.features.VectorSqlDialect;
import net.hasor.dbvisitor.lambda.core.MetricType;

/**
 * 扩展 AbstractDialect 以支持 CommandBuilder 接口
 * @author 赵永春 (zyc@hasor.net)
 * @version 2025-12-06
 */
public abstract class AbstractBuilderDialect extends AbstractDialect implements SqlCommandBuilder {
    @Override
    public abstract SqlCommandBuilder newBuilder();

    @Override
    public final void addVectorByOrder(String col, String colTerm, Object vector, String vectorTerm, MetricType metricType) {
        if (!(this instanceof VectorSqlDialect)) {
            throw new UnsupportedOperationException("Vector not supported by this dialect.");
        } else {
            ((VectorSqlDialect) this).addOrderByVector(col, colTerm, vector, vectorTerm, metricType);
        }
    }

    @Override
    public final void addVectorByConditionRange(ConditionLogic logic, String col, String colTerm,//
            Object vector, String vectorTerm, Object threshold, String thresholdTerm, MetricType metricType) {
        if (!(this instanceof VectorSqlDialect)) {
            throw new UnsupportedOperationException("Vector not supported by this dialect.");
        } else {
            ((VectorSqlDialect) this).addConditionForVectorRange(logic, col, colTerm, vector, vectorTerm, threshold, thresholdTerm, metricType);
        }
    }
}
