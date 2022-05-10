/*
 * Copyright 2002-2010 the original author or authors.
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
import net.hasor.cobble.convert.ConverterUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * <resultMap> or <mapper>
 * @version : 2021-06-21
 * @author 赵永春 (zyc@hasor.net)
 */
public class MappingOptions {
    public static String OPT_KEY_CASE_INSENSITIVE = "caseInsensitive";
    public static String OPT_KEY_TO_CAMELCASE     = "mapUnderscoreToCamelCase";
    public static String OPT_KEY_AUTO_MAPPING     = "autoMapping";

    private Boolean autoMapping;
    private Boolean mapUnderscoreToCamelCase;
    private Boolean caseInsensitive;

    public MappingOptions() {
    }

    public MappingOptions(MappingOptions options) {
        if (options != null) {
            this.autoMapping = options.autoMapping;
            this.mapUnderscoreToCamelCase = options.mapUnderscoreToCamelCase;
            this.caseInsensitive = options.caseInsensitive;
        }
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

    public static MappingOptions resolveOptions(Node refData) {
        if (refData == null) {
            return MappingOptions.buildNew();
        }
        NamedNodeMap nodeAttributes = refData.getAttributes();
        Node autoMappingNode = nodeAttributes.getNamedItem(OPT_KEY_AUTO_MAPPING);
        Node mapUnderscoreToCamelCaseNode = nodeAttributes.getNamedItem(OPT_KEY_TO_CAMELCASE);
        Node caseInsensitiveNode = nodeAttributes.getNamedItem(OPT_KEY_CASE_INSENSITIVE);

        String autoMapping = (autoMappingNode != null) ? autoMappingNode.getNodeValue() : null;
        String mapUnderscoreToCamelCase = (mapUnderscoreToCamelCaseNode != null) ? mapUnderscoreToCamelCaseNode.getNodeValue() : null;
        String caseInsensitive = (caseInsensitiveNode != null) ? caseInsensitiveNode.getNodeValue() : null;

        MappingOptions options = new MappingOptions();
        options.autoMapping = (autoMapping == null) ? null : Boolean.TRUE.equals(ConverterUtils.convert(autoMapping, Boolean.TYPE));
        options.mapUnderscoreToCamelCase = (mapUnderscoreToCamelCase == null) ? null : Boolean.TRUE.equals(ConverterUtils.convert(mapUnderscoreToCamelCase, Boolean.TYPE));
        options.caseInsensitive = (caseInsensitive == null) ? null : Boolean.TRUE.equals(ConverterUtils.convert(caseInsensitive, Boolean.TYPE));
        return options;
    }

    public static MappingOptions resolveOptions(Node refData, MappingOptions defaultOptions) {
        MappingOptions options = resolveOptions(refData);
        if (options.autoMapping == null) {
            options.autoMapping = defaultOptions.autoMapping;
        }
        if (options.mapUnderscoreToCamelCase == null) {
            options.mapUnderscoreToCamelCase = defaultOptions.mapUnderscoreToCamelCase;
        }
        if (options.caseInsensitive == null) {
            options.caseInsensitive = defaultOptions.caseInsensitive;
        }
        return options;
    }

    public static MappingOptions buildNew() {
        return new MappingOptions();
    }

    public static MappingOptions buildNew(MappingOptions options) {
        return new MappingOptions(options);
    }

}
