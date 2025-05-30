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
package net.hasor.dbvisitor.session;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.convert.ConverterBean;
import net.hasor.cobble.ref.BeanMap;
import net.hasor.dbvisitor.mapper.StatementDef;
import net.hasor.dbvisitor.mapper.def.SelectKeyConfig;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 负责处理 SelectKey 的执行
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-11-05
 */
class SelectKeyStatementExecute {
    private final SelectKeyConfig          config;
    private final StatementDef             configDef;
    private final AbstractStatementExecute execute;

    SelectKeyStatementExecute(StatementDef parentDef, SelectKeyConfig config, AbstractStatementExecute execute) {
        this.config = config;
        this.configDef = new StatementDef(parentDef.getConfigNamespace(), parentDef.getConfigId() + "_selectKey", config);
        this.execute = execute;

    }

    public void processBefore(Connection conn, Map<String, Object> parameter) throws SQLException {
        if (StringUtils.equalsIgnoreCase("BEFORE", this.config.getOrder())) {
            this.processSelectKey(conn, parameter);
        }
    }

    public void processAfter(Connection conn, Map<String, Object> parameter) throws SQLException {
        if (StringUtils.equalsIgnoreCase("AFTER", this.config.getOrder())) {
            this.processSelectKey(conn, parameter);
        }
    }

    private void processSelectKey(Connection conn, Map<String, Object> parameter) throws SQLException {
        String keyColumn = this.config.getKeyColumn();
        String keyProperty = this.config.getKeyProperty();
        Object resultValue = this.execute.execute(conn, this.configDef, parameter);

        if (resultValue instanceof List) {
            resultValue = ((List<?>) resultValue).get(0);
        }

        if (StringUtils.isNotBlank(keyColumn)) {
            String[] properties = keyProperty.split(",");
            String[] columns = keyColumn.split(",");
            if (properties.length != columns.length) {
                throw new SQLException("SelectKey keyProperty size " + properties.length + " and keyColumn size " + columns.length + ", mismatch.");
            }

            Map<String, Object> keyResult = null;
            if (resultValue instanceof Map) {
                keyResult = (Map<String, Object>) resultValue;
            } else {
                BeanMap beanMap = new BeanMap(resultValue);
                beanMap.setTransformConvert(ConverterBean.getInstance());
                keyResult = beanMap;
            }
            for (int i = 0; i < columns.length; i++) {
                parameter.put(properties[i], keyResult.get(columns[i]));
            }

        } else {
            String[] properties = keyProperty.split(",");
            if (properties.length > 1) {
                throw new SQLException("SelectKey multiple property, keyColumn must be config.");
            }

            if (resultValue instanceof Map) {
                resultValue = ((Map<?, ?>) resultValue).values().stream().findFirst().orElse(null);
            }

            parameter.put(properties[0], resultValue);
        }
    }
}