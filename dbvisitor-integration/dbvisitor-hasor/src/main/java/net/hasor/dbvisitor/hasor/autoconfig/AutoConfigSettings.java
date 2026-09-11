/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.hasor.autoconfig;

import net.hasor.cobble.StringUtils;
import net.hasor.cobble.setting.Settings;

final class AutoConfigSettings {
    private final Settings settings;
    private final String   sourceName;

    AutoConfigSettings(Settings settings, String sourceName) {
        this.settings = settings;
        this.sourceName = sourceName;
    }

    String[] getDataSourceNames() {
        String value = this.getString("dbvisitor.multiple-datasource", null);
        if (StringUtils.isBlank(value)) {
            return new String[] { null };
        }
        String[] sourceNames = value.split(",");
        for (int i = 0; i < sourceNames.length; i++) {
            sourceNames[i] = sourceNames[i].trim();
        }
        return sourceNames;
    }

    String getDataSourceConfigKey() {
        return StringUtils.isBlank(this.sourceName) ? "dbvisitor.jdbc-ds" : "dbvisitor." + this.sourceName + ".jdbc-ds";
    }

    String getDataSourceType() {
        return this.getString(this.getDataSourceConfigKey(), DefaultDataSource.class.getName());
    }

    String getString(String key, String defaultValue) {
        String value = this.settings.getString(key, null);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        String underscoreKey = key.replace('-', '_');
        if (!underscoreKey.equals(key)) {
            value = this.settings.getString(underscoreKey, null);
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        String camelKey = this.keyToCamelPath(key);
        if (!camelKey.equals(key)) {
            value = this.settings.getString(camelKey, null);
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return defaultValue;
    }

    private String keyToCamelPath(String key) {
        String[] segments = key.split("\\.");
        for (int i = 0; i < segments.length; i++) {
            if (segments[i].indexOf('-') >= 0) {
                segments[i] = StringUtils.lineToHump(segments[i].replace('-', '_'));
            }
        }
        return String.join(".", segments);
    }
}
