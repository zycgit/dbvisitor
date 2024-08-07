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
package net.hasor.dbvisitor.lambda.support.map;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.lambda.MapDeleteOperation;
import net.hasor.dbvisitor.lambda.core.AbstractDeleteLambda;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.mapping.resolve.MappingOptions;

import java.util.Map;

/**
 * 提供 lambda delete 能力，是 MapDeleteOperation 接口的实现类。
 * @version : 2022-04-02
 * @author 赵永春 (zyc@hasor.net)
 */
public class DeleteLambdaForMap extends AbstractDeleteLambda<MapDeleteOperation, Map<String, Object>, String> //
        implements MapDeleteOperation {
    private final boolean toCamelCase;

    public DeleteLambdaForMap(TableMapping<?> tableMapping, MappingOptions opt, LambdaTemplate jdbcTemplate) {
        super(Map.class, tableMapping, opt, jdbcTemplate);
        this.toCamelCase = getTableMapping().isToCamelCase();
    }

    @Override
    protected MapDeleteOperation getSelf() {
        return this;
    }

    @Override
    protected String getPropertyName(String property) {
        if (this.toCamelCase) {
            return StringUtils.humpToLine(property);
        } else {
            return property;
        }
    }

}
