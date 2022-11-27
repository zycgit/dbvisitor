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
package net.hasor.dbvisitor.faker.generator.provider.conservative;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.setting.SettingNode;
import net.hasor.dbvisitor.faker.generator.TypeProcessor;
import net.hasor.dbvisitor.faker.generator.UseFor;
import net.hasor.dbvisitor.faker.generator.provider.DefaultTypeProcessorFactory;
import net.hasor.dbvisitor.faker.meta.JdbcColumn;

public class OracleConservativeTypeProcessorFactory extends DefaultTypeProcessorFactory {
    // oracle null and "" is same
    //                if (StringUtils.equalsIgnoreCase(JdbcUtils.ORACLE, dbType) && stringSeedConfig.isAllowNullable()) {
    //        stringSeedConfig.setAllowEmpty(false);
    //    }
    @Override
    public TypeProcessor createSeedFactory(JdbcColumn jdbcColumn, SettingNode columnConfig) {
        String columnType = jdbcColumn.getColumnType();
        if (StringUtils.isBlank(columnType)) {
            return defaultSeedFactory(jdbcColumn);
        }
        switch (columnType) {
            case "char":
            default: {
                TypeProcessor seedAndWriter = defaultSeedFactory(jdbcColumn);
                seedAndWriter.getDefaultIgnoreAct().add(UseFor.Insert);
                seedAndWriter.getDefaultIgnoreAct().add(UseFor.UpdateSet);
                return seedAndWriter;
            }
        }
    }
}
