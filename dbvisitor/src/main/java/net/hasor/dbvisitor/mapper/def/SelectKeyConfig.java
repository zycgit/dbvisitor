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
package net.hasor.dbvisitor.mapper.def;
import java.util.function.Function;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dynamic.logic.ArrayDynamicSql;
import net.hasor.dbvisitor.mapper.ResultSetType;

/**
 * <selectKey> 标签配置类，用于处理数据库主键生成策略
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-10-04
 */
public class SelectKeyConfig extends SqlConfig {
    private int           fetchSize     = 256;
    private ResultSetType resultSetType = ResultSetType.DEFAULT;
    private String        keyProperty   = null;
    private String        keyColumn     = null;
    private String        order         = null;

    /**
     * 构造函数
     * @param target 动态SQL构建目标对象
     * @param config 配置获取函数
     */
    public SelectKeyConfig(ArrayDynamicSql target, Function<String, String> config) {
        super(target, config);

        if (config != null) {
            this.fetchSize = Integer.parseInt(config.andThen(s -> StringUtils.isBlank(s) ? "256" : s).apply(FETCH_SIZE));
            this.resultSetType = config.andThen(s -> ResultSetType.valueOfCode(s, ResultSetType.DEFAULT)).apply(RESULT_SET_TYPE);
            this.keyProperty = config.apply(KEY_PROPERTY);
            this.keyColumn = config.apply(KEY_COLUMN);
            this.order = config.apply(ORDER);
        }
    }

    @Override
    public QueryType getType() {
        return QueryType.Select;
    }

    public ArrayDynamicSql getTarget() {
        return this.target;
    }

    public void setTarget(ArrayDynamicSql target) {
        this.target = target;
    }

    public int getFetchSize() {
        return this.fetchSize;
    }

    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    public ResultSetType getResultSetType() {
        return this.resultSetType;
    }

    public void setResultSetType(ResultSetType resultSetType) {
        this.resultSetType = resultSetType;
    }

    public String getKeyProperty() {
        return this.keyProperty;
    }

    public void setKeyProperty(String keyProperty) {
        this.keyProperty = keyProperty;
    }

    public String getKeyColumn() {
        return this.keyColumn;
    }

    public void setKeyColumn(String keyColumn) {
        this.keyColumn = keyColumn;
    }

    public String getOrder() {
        return this.order;
    }

    public void setOrder(String order) {
        this.order = order;
    }
}