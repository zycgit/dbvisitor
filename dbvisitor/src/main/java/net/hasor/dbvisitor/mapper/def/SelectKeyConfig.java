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
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dynamic.RegistryManager;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.dynamic.args.MapSqlArgSource;
import net.hasor.dbvisitor.dynamic.logic.ArrayDynamicSql;
import net.hasor.dbvisitor.mapper.ResultSetType;

import java.sql.SQLException;
import java.util.Map;
import java.util.function.Function;

/**
 * <selectKey> 标签
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-10-04
 */
public class SelectKeyConfig extends SqlConfig {
    private       int           fetchSize     = 256;
    private       ResultSetType resultSetType = ResultSetType.DEFAULT;
    private       String        keyProperty   = null;
    private       String        keyColumn     = null;
    private       String        order         = null;
    private       String        resultType    = null;
    private       String        resultHandler = null;
    private final boolean       ignoreBuild;

    public SelectKeyConfig(ArrayDynamicSql target, Function<String, String> config, boolean ignoreBuild) {
        super(target, config);

        if (config != null) {
            this.fetchSize = Integer.parseInt(config.andThen(s -> StringUtils.isBlank(s) ? "256" : s).apply(FETCH_SIZE));
            this.resultSetType = config.andThen(s -> ResultSetType.valueOfCode(s, ResultSetType.DEFAULT)).apply(RESULT_SET_TYPE);
            this.keyProperty = config.apply(KEY_PROPERTY);
            this.keyColumn = config.apply(KEY_COLUMN);
            this.order = config.apply(ORDER);
            this.resultType = config.apply(RESULT_TYPE);
            this.resultHandler = config.apply(RESULT_HANDLER);
        }

        this.ignoreBuild = ignoreBuild;
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

    public String getResultType() {
        return this.resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public String getResultHandler() {
        return this.resultHandler;
    }

    public void setResultHandler(String resultHandler) {
        this.resultHandler = resultHandler;
    }

    @Override
    public void buildQuery(SqlArgSource data, RegistryManager context, SqlBuilder sqlBuilder) throws SQLException {
        if (!this.ignoreBuild) {
            super.buildQuery(data, context, sqlBuilder);
        }
    }

    @Override
    public SqlBuilder buildQuery(SqlArgSource data, RegistryManager context) throws SQLException {
        SqlBuilder fxBuilder = new SqlBuilder();
        if (!this.ignoreBuild) {
            this.buildQuery(data, context, fxBuilder);
        }
        return fxBuilder;
    }

    @Override
    public SqlBuilder buildQuery(Map<String, Object> data, RegistryManager context) throws SQLException {
        SqlBuilder fxBuilder = new SqlBuilder();
        if (!this.ignoreBuild) {
            this.buildQuery(new MapSqlArgSource(data), context, fxBuilder);
        }
        return fxBuilder;
    }
}