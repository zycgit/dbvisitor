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
package net.hasor.dbvisitor;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.mapper.Mapper;
import net.hasor.dbvisitor.mapper.MapperDef;

import javax.inject.Singleton;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-07-18
 */
public enum ConfigKeys {
    /** 若配置多数据源，则每个数据源的名字 */
    MultipleDataSource("", "dbvisitor.multiple-datasource", null),
    /** 数据源配置 */
    DataSourceType("dbvisitor", "jdbc-ds", DefaultDataSource.class.getName()),

    /** 加载的 Mapper 资源 */
    MapperLocations("dbvisitor", "mapper-locations", "dbvisitor/mapper/*.xml"),
    MapperDisabled("dbvisitor", "mapper-disabled", "false"),
    MapperPackages("dbvisitor", "mapper-packages", null),
    ScanMarkerAnnotation("dbvisitor", "marker-annotation", MapperDef.class.getName()),
    ScanMarkerInterface("dbvisitor", "marker-interface", Mapper.class.getName()),

    OptAutoMapping("dbvisitor", "auto-mapping", null),
    OptCamelCase("dbvisitor", "camel-case", null),
    OptCaseInsensitive("dbvisitor", "case-insensitive", null),
    OptUseDelimited("dbvisitor", "use-delimited", null),
    OptIgnoreNonExistStatement("dbvisitor", "ignore-nonexist-statement", null),
    OptSqlDialect("dbvisitor", "dialect", null),
    ;

    private final String prefix;
    private final String configKey;
    private final String defaultValue;

    ConfigKeys(String prefix, String configKey, String defaultValue) {
        this.prefix = prefix;
        this.configKey = configKey;
        this.defaultValue = defaultValue;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getConfigKey() {
        return this.configKey;
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

    public String buildConfigKey(String dsName) {
        if (StringUtils.isNotBlank(dsName)) {
            return prefix + "." + dsName + "." + configKey;
        } else {
            return prefix + "." + configKey;
        }
    }
}
