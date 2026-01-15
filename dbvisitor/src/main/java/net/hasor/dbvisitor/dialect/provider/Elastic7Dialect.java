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
import net.hasor.cobble.StringUtils;

/**
 * ES7 方言
 * @author 赵永春 (zyc@hasor.net)
 * @version 2025-12-31
 */
public class Elastic7Dialect extends AbstractElasticDialect {
    @Override
    protected String getSearchEndpoint() {
        return buildPath("_search");
    }

    @Override
    protected String getInsertEndpoint() {
        return buildPath("_doc");
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
        if (StringUtils.isNotBlank(action)) {
            sb.append("/").append(action);
        }
        return sb.toString();
    }

    @Override
    public AbstractElasticDialect newBuilder() {
        return new Elastic7Dialect();
    }
}
