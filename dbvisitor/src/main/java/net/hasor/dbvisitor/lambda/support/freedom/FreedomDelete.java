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
package net.hasor.dbvisitor.lambda.support.freedom;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.support.map.MapDeleteImpl;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;

/**
 * 提供 lambda delete 能力。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-02
 */
public class FreedomDelete extends MapDeleteImpl {
    public FreedomDelete(TableMapping<?> tableMapping, MappingRegistry registry, JdbcTemplate jdbc, QueryContext ctx) {
        super(tableMapping, registry, jdbc, ctx);
    }

    @Override
    protected boolean isFreedom() {
        return true;
    }

    @Override
    protected ColumnMapping whenPropertyNotExist(String propertyName) {
        return FreedomUtils.initOrGetMapMapping(propertyName, this.getTableMapping().isToCamelCase());
    }
}
