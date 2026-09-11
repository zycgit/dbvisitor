/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapper.def;
import java.util.function.Function;
import net.hasor.cobble.ArrayUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dynamic.logic.ArrayDynamicSql;

/**
 * Execute SqlConfig
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-19
 */
public class ExecuteConfig extends SqlConfig {
    private String   resultType = null;
    private String[] bindOut    = ArrayUtils.EMPTY_STRING_ARRAY;

    /**
     * 构造函数
     * @param target 动态SQL目标对象
     * @param config 配置函数，用于获取配置项
     */
    public ExecuteConfig(ArrayDynamicSql target, Function<String, String> config) {
        super(target, config);

        if (config != null) {
            this.resultType = config.apply(RESULT_TYPE);
            this.bindOut = config.andThen(s -> StringUtils.isNotBlank(s) ? s.split(",") : ArrayUtils.EMPTY_STRING_ARRAY).apply(BIND_OUT);
        }
    }

    @Override
    public QueryType getType() {
        return QueryType.Execute;
    }

    public String getResultType() {
        return this.resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public String[] getBindOut() {
        return this.bindOut;
    }

    public void setBindOut(String[] bindOut) {
        this.bindOut = bindOut;
    }
}
