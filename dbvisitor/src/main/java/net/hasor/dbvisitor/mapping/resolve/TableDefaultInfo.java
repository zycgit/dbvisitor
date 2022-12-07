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
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.DefaultSqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialectRegister;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.mapping.TableDefault;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * 承载 @TableDefault 配置信息
 * @version : 2021-06-21
 * @author 赵永春 (zyc@hasor.net)
 */
class TableDefaultInfo implements TableDefault, Table {
    private final String     catalog;
    private final String     schema;
    private final String     table;
    private final boolean    autoMapping;
    private final boolean    mapUnderscoreToCamelCase;
    private final boolean    useDelimited;
    private final boolean    caseInsensitive;
    private final SqlDialect dialect;
    private final String     dialectName;

    TableDefaultInfo(Map<String, String> attrMaps, ClassLoader classLoader, MappingOptions options) {
        String catalog = attrMaps.get("catalog");
        String schema = attrMaps.get("schema");
        String table = attrMaps.get("value");
        if (StringUtils.isBlank(table)) {
            table = attrMaps.get("name");
        }
        String autoMapping = attrMaps.get("autoMapping");
        String mapUnderscoreToCamelCase = attrMaps.get("mapUnderscoreToCamelCase");
        String useDelimited = attrMaps.get("useDelimited");
        String caseInsensitive = attrMaps.get("caseInsensitive");
        String dialect = attrMaps.get("dialect");

        this.catalog = (catalog == null) ? "" : catalog;
        this.schema = (schema == null) ? "" : schema;
        this.table = (table == null) ? "" : table;

        if (StringUtils.isNotBlank(autoMapping)) {
            this.autoMapping = Boolean.parseBoolean(autoMapping);
        } else {
            this.autoMapping = options.getAutoMapping() == null || options.getAutoMapping();
        }

        if (StringUtils.isNotBlank(mapUnderscoreToCamelCase)) {
            this.mapUnderscoreToCamelCase = Boolean.parseBoolean(mapUnderscoreToCamelCase);
        } else {
            this.mapUnderscoreToCamelCase = Boolean.TRUE.equals(options.getMapUnderscoreToCamelCase());
        }

        if (StringUtils.isNotBlank(useDelimited)) {
            this.useDelimited = Boolean.parseBoolean(useDelimited);
        } else {
            this.useDelimited = Boolean.TRUE.equals(options.getUseDelimited());
        }

        if (StringUtils.isNotBlank(caseInsensitive)) {
            this.caseInsensitive = Boolean.parseBoolean(caseInsensitive);
        } else {
            this.caseInsensitive = options.getCaseInsensitive() == null || options.getCaseInsensitive();
        }

        if (StringUtils.isNotBlank(dialect)) {
            this.dialect = SqlDialectRegister.findOrCreate(dialect, classLoader);
            this.dialectName = dialect;
        } else {
            this.dialect = options.getDefaultDialect() == null ? DefaultSqlDialect.DEFAULT : options.getDefaultDialect();
            this.dialectName = this.dialect.getClass().getName();
        }
    }

    @Override
    public String catalog() {
        return this.catalog;
    }

    @Override
    public String schema() {
        return this.schema;
    }

    @Override
    public String value() {
        return this.table;
    }

    @Override
    public String name() {
        return this.table;
    }

    @Override
    public boolean autoMapping() {
        return this.autoMapping;
    }

    @Override
    public boolean mapUnderscoreToCamelCase() {
        return this.mapUnderscoreToCamelCase;
    }

    @Override
    public boolean useDelimited() {
        return this.useDelimited;
    }

    @Override
    public boolean caseInsensitive() {
        return this.caseInsensitive;
    }

    @Override
    public String dialect() {
        return this.dialectName;
    }

    public SqlDialect sqlDialect() {
        return this.dialect;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Table.class;
    }
}