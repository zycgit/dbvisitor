/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
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
