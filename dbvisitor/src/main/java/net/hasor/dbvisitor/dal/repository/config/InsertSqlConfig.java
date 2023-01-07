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
package net.hasor.dbvisitor.dal.repository.config;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dal.dynamic.DynamicSql;
import net.hasor.dbvisitor.dal.repository.QueryType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Insert SqlConfig
 * @version : 2021-06-19
 * @author 赵永春 (zyc@hasor.net)
 */
public class InsertSqlConfig extends DmlSqlConfig {
    private boolean useGeneratedKeys = false;
    private String  keyProperty      = null;
    private String  parameterType    = null;

    public InsertSqlConfig(DynamicSql target) {
        super(target);
    }

    public InsertSqlConfig(DynamicSql target, Node operationNode) {
        super(target, operationNode);
        NamedNodeMap nodeAttributes = operationNode.getAttributes();

        // 1st: SelectKey
        if (this.getSelectKey() == null) {
            Node useGeneratedKeysNode = nodeAttributes.getNamedItem("useGeneratedKeys");
            Node keyPropertyNode = nodeAttributes.getNamedItem("keyProperty");
            Node parameterTypeNode = nodeAttributes.getNamedItem("parameterType");
            String useGeneratedKeys = (useGeneratedKeysNode != null) ? useGeneratedKeysNode.getNodeValue() : null;
            String keyProperty = (keyPropertyNode != null) ? keyPropertyNode.getNodeValue() : null;
            String parameterType = (parameterTypeNode != null) ? parameterTypeNode.getNodeValue() : null;

            // 2st: useGeneratedKeys & keyProperty
            this.useGeneratedKeys = StringUtils.equalsIgnoreCase(useGeneratedKeys, "true");
            this.keyProperty = StringUtils.isBlank(keyProperty) ? null : keyProperty;

            // 3st: parameterType
            if (!this.useGeneratedKeys) {
                this.parameterType = StringUtils.isBlank(parameterType) ? null : parameterType;
            }
        }
    }

    @Override
    public QueryType getDynamicType() {
        return QueryType.Insert;
    }

    public boolean isUseGeneratedKeys() {
        return useGeneratedKeys;
    }

    public void setUseGeneratedKeys(boolean useGeneratedKeys) {
        this.useGeneratedKeys = useGeneratedKeys;
    }

    public String getKeyProperty() {
        return keyProperty;
    }

    public void setKeyProperty(String keyProperty) {
        this.keyProperty = keyProperty;
    }

    public String getParameterType() {
        return this.parameterType;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }
}