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
package net.hasor.dbvisitor.mapper;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.jdbc.RowCallbackHandler;
import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.mapper.def.SqlConfig;

import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

/**
 * 引用 Mapper 配置文件中的 SQL。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-05-19
 */
public class StatementDef {
    private final String                configNamespace;
    private final String                configId;
    private       SqlConfig             config;
    private       boolean               usingCollection;
    private       Class<?>              resultType;
    private       ResultSetExtractor<?> resultExtractor;
    private       RowCallbackHandler    resultRowCallback;
    private       RowMapper<?>          resultRowMapper;

    public StatementDef(String configNamespace, String configId, SqlConfig config) {
        this.configNamespace = Objects.requireNonNull(configNamespace);
        this.configId = Objects.requireNonNull(configId);
        this.config = Objects.requireNonNull(config);
        this.usingCollection = true;
    }

    public String getConfigNamespace() {
        return this.configNamespace;
    }

    public String getConfigId() {
        return this.configId;
    }

    public SqlConfig getConfig() {
        return this.config;
    }

    public void setConfig(SqlConfig config) {
        this.config = config;
    }

    public boolean isUsingCollection() {
        return this.usingCollection;
    }

    public void setUsingCollection(boolean usingCollection) {
        this.usingCollection = usingCollection;
    }

    public Class<?> getResultType() {
        return this.resultType;
    }

    public void setResultType(Class<?> resultType) {
        this.resultType = resultType;
    }

    public ResultSetExtractor<?> getResultExtractor() {
        return this.resultExtractor;
    }

    public void setResultExtractor(ResultSetExtractor<?> resultExtractor) {
        this.resultExtractor = resultExtractor;
    }

    public RowCallbackHandler getResultRowCallback() {
        return this.resultRowCallback;
    }

    public void setResultRowCallback(RowCallbackHandler resultRowCallback) {
        this.resultRowCallback = resultRowCallback;
    }

    public RowMapper<?> getResultRowMapper() {
        return this.resultRowMapper;
    }

    public void setResultRowMapper(RowMapper<?> resultRowMapper) {
        this.resultRowMapper = resultRowMapper;
    }

    public SqlBuilder buildQuery(Map<String, Object> ctx, QueryContext context) throws SQLException {
        return this.config.buildQuery(ctx, context);
    }

    public String toConfigId() {
        return StringUtils.isBlank(this.configNamespace) ? this.configId : (this.configNamespace + "." + this.configId);
    }
}