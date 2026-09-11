/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dialect.features;
import net.hasor.dbvisitor.dialect.SqlCommandBuilder.ConditionLogic;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.lambda.core.MetricType;

/**
 * SQL 分页方言
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public interface VectorSqlDialect extends SqlDialect {
    /** 添加向量范围查询条件 {@code (col <-> vector) < threshold} */
    void addConditionForVectorRange(ConditionLogic logic, String col, String colTerm, //
            Object vector, String vectorTerm, Object threshold, String thresholdTerm, MetricType metricType);

    /** 添加向量排序 */
    void addOrderByVector(String col, String colTerm, Object vector, String vectorTerm, MetricType metricType);
}
