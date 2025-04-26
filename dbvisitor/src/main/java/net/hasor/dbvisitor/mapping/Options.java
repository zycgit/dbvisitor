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
package net.hasor.dbvisitor.mapping;
import net.hasor.dbvisitor.dialect.SqlDialect;

/**
 * <resultMap> or <mapper>
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-21
 */
public class Options {
    private String     catalog;
    private String     schema;
    private Boolean    autoMapping;
    private Boolean    mapUnderscoreToCamelCase;
    private Boolean    caseInsensitive;
    private Boolean    useDelimited;
    private SqlDialect dialect;
    private Boolean    ignoreNonExistStatement;

    public Options() {
    }

    public Options(Options options) {
        if (options != null) {
            this.catalog = options.catalog;
            this.schema = options.schema;
            this.autoMapping = options.autoMapping;
            this.mapUnderscoreToCamelCase = options.mapUnderscoreToCamelCase;
            this.caseInsensitive = options.caseInsensitive;
            this.useDelimited = options.useDelimited;
            this.dialect = options.dialect;
            this.ignoreNonExistStatement = options.ignoreNonExistStatement;
        }
    }

    public static Options of() {
        return new Options();
    }

    public static Options of(Options options) {
        return new Options(options);
    }

    @Override
    public String toString() {
        String dialect = this.dialect == null ? null : this.dialect.getClass().getName();
        String key = autoMapping + "," + //
                this.mapUnderscoreToCamelCase + "," +//
                this.caseInsensitive + "," +//
                this.useDelimited + "," +//
                this.ignoreNonExistStatement + "," +//
                dialect;
        return "Options[" + key + "]";
    }

    public String getCatalog() {
        return this.catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public Options catalog(String catalog) {
        setCatalog(catalog);
        return this;
    }

    public String getSchema() {
        return this.schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public Options schema(String schema) {
        setSchema(schema);
        return this;
    }

    public Boolean getAutoMapping() {
        return this.autoMapping;
    }

    public void setAutoMapping(Boolean autoMapping) {
        this.autoMapping = autoMapping;
    }

    public Options autoMapping(Boolean autoMapping) {
        setAutoMapping(autoMapping);
        return this;
    }

    public Boolean getMapUnderscoreToCamelCase() {
        return this.mapUnderscoreToCamelCase;
    }

    public void setMapUnderscoreToCamelCase(Boolean mapUnderscoreToCamelCase) {
        this.mapUnderscoreToCamelCase = mapUnderscoreToCamelCase;
    }

    public Options mapUnderscoreToCamelCase(Boolean mapUnderscoreToCamelCase) {
        setMapUnderscoreToCamelCase(mapUnderscoreToCamelCase);
        return this;
    }

    public Boolean getCaseInsensitive() {
        return this.caseInsensitive;
    }

    public void setCaseInsensitive(Boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    public Options caseInsensitive(Boolean caseInsensitive) {
        setCaseInsensitive(caseInsensitive);
        return this;
    }

    public SqlDialect getDialect() {
        return this.dialect;
    }

    public void setDialect(SqlDialect dialect) {
        this.dialect = dialect;
    }

    public Options dialect(SqlDialect dialect) {
        setDialect(dialect);
        return this;
    }

    public Boolean getUseDelimited() {
        return this.useDelimited;
    }

    public void setUseDelimited(Boolean useDelimited) {
        this.useDelimited = useDelimited;
    }

    public Options useDelimited(Boolean useDelimited) {
        setUseDelimited(useDelimited);
        return this;
    }

    public Boolean getIgnoreNonExistStatement() {
        return this.ignoreNonExistStatement;
    }

    public void setIgnoreNonExistStatement(Boolean ignoreNonExistStatement) {
        this.ignoreNonExistStatement = ignoreNonExistStatement;
    }

    public Options ignoreNonExistStatement(Boolean ignoreNonExistStatement) {
        setIgnoreNonExistStatement(ignoreNonExistStatement);
        return this;
    }
}