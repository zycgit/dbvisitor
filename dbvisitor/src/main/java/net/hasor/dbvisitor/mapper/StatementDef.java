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
import net.hasor.dbvisitor.dynamic.RegistryManager;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.mapper.def.SqlConfig;
import net.hasor.dbvisitor.template.jdbc.RowMapper;

import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

/**
 * 引用 Mapper 配置文件中的 SQL。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-05-19
 */
public class StatementDef {
    private String       namespace;
    private SqlConfig    config;
    private Class<?>     mappingType;
    private RowMapper<?> rowMapper;

    public StatementDef(String namespace, SqlConfig config) {
        this.namespace = Objects.requireNonNull(namespace);
        this.config = Objects.requireNonNull(config);
    }

    public String getNamespace() {
        return this.namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public SqlConfig getConfig() {
        return this.config;
    }

    public void setConfig(SqlConfig config) {
        this.config = config;
    }

    public Class<?> getMappingType() {
        return this.mappingType;
    }

    public void setMappingType(Class<?> mappingType) {
        this.mappingType = mappingType;
    }

    public RowMapper<?> getRowMapper() {
        return this.rowMapper;
    }

    public void setRowMapper(RowMapper<?> rowMapper) {
        this.rowMapper = rowMapper;
    }

    public SqlBuilder buildQuery(Map<String, Object> ctx, RegistryManager registryManager) throws SQLException {
        return this.config.buildQuery(ctx, registryManager);
    }
}