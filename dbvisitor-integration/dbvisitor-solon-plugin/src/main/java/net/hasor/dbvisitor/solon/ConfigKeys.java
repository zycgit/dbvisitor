/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.solon;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.mapper.Mapper;
import net.hasor.dbvisitor.mapper.MapperDef;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2025-03-20
 */
public enum ConfigKeys {
    /** 加载的 Mapper 资源 */
    MapperLocations("dbvisitor", "mapperLocations", "dbvisitor/mapper/*.xml"),
    MapperDisabled("dbvisitor", "mapperDisabled", "false"),
    MapperPackages("dbvisitor", "mapperPackages", null),
    ScanMarkerAnnotation("dbvisitor", "markerAnnotation", MapperDef.class.getName()),
    ScanMarkerInterface("dbvisitor", "markerInterface", Mapper.class.getName()),

    OptAutoMapping("dbvisitor", "autoMapping", null),
    OptCamelCase("dbvisitor", "camelCase", null),
    OptCaseInsensitive("dbvisitor", "caseInsensitive", null),
    OptUseDelimited("dbvisitor", "useDelimited", null),
    OptIgnoreNonExistStatement("dbvisitor", "ignoreNonExistStatement", null),
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
