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
package net.hasor.dbvisitor.dal.repository.config;
import net.hasor.dbvisitor.dal.dynamic.DynamicContext;
import net.hasor.dbvisitor.dal.dynamic.nodes.SelectKeyDynamicSql;
import net.hasor.dbvisitor.dal.repository.MultipleResultsType;
import net.hasor.dbvisitor.dialect.SqlBuilder;

import java.sql.SQLException;
import java.util.Map;

/**
 * SelectKey SqlConfig
 * @version : 2021-06-19
 * @author 赵永春 (zyc@hasor.net)
 */
public class SelectKeySqlConfig extends QuerySqlConfig {
    private String keyProperty;
    private String keyColumn;
    private String order;
    private String handler;

    public SelectKeySqlConfig(SelectKeyDynamicSql target) {
        super(target);
        this.setMultipleResultType(MultipleResultsType.LAST);
        this.setResultMap(null); // is always null, in dtd and xsd do not be config.
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

    public String getHandler() {
        return this.handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    @Override
    public void buildQuery(Map<String, Object> data, DynamicContext context, SqlBuilder sqlBuilder) throws SQLException {
        ((SelectKeyDynamicSql) this.target).buildSqlQuery(data, context, sqlBuilder);
    }
}
