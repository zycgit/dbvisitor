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
package net.hasor.dbvisitor.mapping.resolve;
import net.hasor.dbvisitor.dialect.SqlDialect;

/**
 * <resultMap> or <mapper>
 * @version : 2021-06-21
 * @author 赵永春 (zyc@hasor.net)
 */
public class MappingOptions {
    private Boolean    autoMapping;
    private Boolean    mapUnderscoreToCamelCase;
    private Boolean    caseInsensitive;
    private Boolean    useDelimited;
    private SqlDialect defaultDialect;

    public MappingOptions() {
    }

    public MappingOptions(MappingOptions options) {
        if (options != null) {
            this.autoMapping = options.autoMapping;
            this.mapUnderscoreToCamelCase = options.mapUnderscoreToCamelCase;
            this.caseInsensitive = options.caseInsensitive;
            this.useDelimited = options.useDelimited;
            this.defaultDialect = options.defaultDialect;
        }
    }

    @Override
    public String toString() {
        String dialect = defaultDialect == null ? null : defaultDialect.getClass().getName();
        String key = this.autoMapping + "," + this.mapUnderscoreToCamelCase + "," + this.caseInsensitive + "," + this.useDelimited + "," + dialect;
        return "MappingOptions[" + key + "]";
    }

    public Boolean getAutoMapping() {
        return this.autoMapping;
    }

    public void setAutoMapping(Boolean autoMapping) {
        this.autoMapping = autoMapping;
    }

    public MappingOptions autoMapping(Boolean autoMapping) {
        setAutoMapping(autoMapping);
        return this;
    }

    public Boolean getMapUnderscoreToCamelCase() {
        return this.mapUnderscoreToCamelCase;
    }

    public void setMapUnderscoreToCamelCase(Boolean mapUnderscoreToCamelCase) {
        this.mapUnderscoreToCamelCase = mapUnderscoreToCamelCase;
    }

    public MappingOptions mapUnderscoreToCamelCase(Boolean mapUnderscoreToCamelCase) {
        setMapUnderscoreToCamelCase(mapUnderscoreToCamelCase);
        return this;
    }

    public Boolean getCaseInsensitive() {
        return this.caseInsensitive;
    }

    public void setCaseInsensitive(Boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    public MappingOptions caseInsensitive(Boolean caseInsensitive) {
        setCaseInsensitive(caseInsensitive);
        return this;
    }

    public SqlDialect getDefaultDialect() {
        return this.defaultDialect;
    }

    public void setDefaultDialect(SqlDialect defaultDialect) {
        this.defaultDialect = defaultDialect;
    }

    public MappingOptions defaultDialect(SqlDialect defaultDialect) {
        setDefaultDialect(defaultDialect);
        return this;
    }

    public Boolean getUseDelimited() {
        return this.useDelimited;
    }

    public void setUseDelimited(Boolean useDelimited) {
        this.useDelimited = useDelimited;
    }

    public MappingOptions defaultDialect(Boolean useDelimited) {
        setUseDelimited(useDelimited);
        return this;
    }

    public static MappingOptions buildNew() {
        return new MappingOptions();
    }

    public static MappingOptions buildNew(MappingOptions options) {
        return new MappingOptions(options);
    }
}