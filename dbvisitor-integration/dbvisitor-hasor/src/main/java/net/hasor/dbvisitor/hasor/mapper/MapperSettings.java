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
package net.hasor.dbvisitor.hasor.mapper;

import net.hasor.cobble.StringUtils;
import net.hasor.cobble.setting.Settings;
import net.hasor.dbvisitor.mapper.Mapper;
import net.hasor.dbvisitor.mapper.MapperDef;

final class MapperSettings {
    private final Settings settings;
    private final String   sourceName;

    MapperSettings(Settings settings, String sourceName) {
        this.settings = settings;
        this.sourceName = sourceName;
    }

    String getMapperDisabled() {
        return this.getString(this.buildKey("mapper-disabled"), "false");
    }

    String getMapperPackages() {
        return this.getString(this.buildKey("mapper-packages"), null);
    }

    String getMarkerAnnotation() {
        return this.getString(this.buildKey("marker-annotation"), MapperDef.class.getName());
    }

    String getMarkerInterface() {
        return this.getString(this.buildKey("marker-interface"), Mapper.class.getName());
    }

    private String buildKey(String name) {
        return StringUtils.isBlank(this.sourceName) ? "dbvisitor." + name : "dbvisitor." + this.sourceName + "." + name;
    }

    private String getString(String key, String defaultValue) {
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
