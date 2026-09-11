/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dialect.provider;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.SqlDialect;

/**
 * ES6 方言
 * @author 赵永春 (zyc@hasor.net)
 * @version 2025-12-31
 */
public class Elastic6Dialect extends AbstractElasticDialect {
    public static final SqlDialect DEFAULT = new Elastic6Dialect();

    @Override
    public AbstractElasticDialect newBuilder() {
        return new Elastic6Dialect();
    }

    @Override
    protected String getSearchEndpoint() {
        return buildPath("_search");
    }

    @Override
    protected String getInsertEndpoint() {
        return buildPath(null);
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
        if (StringUtils.isNotBlank(this.type)) {
            sb.append("/").append(this.type);
        } else {
            sb.append("/_doc");
        }
        if (StringUtils.isNotBlank(action)) {
            sb.append("/").append(action);
        }
        return sb.toString();
    }
}
