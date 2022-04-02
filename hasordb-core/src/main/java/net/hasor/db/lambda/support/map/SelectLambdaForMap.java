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
package net.hasor.db.lambda.support.map;
import net.hasor.db.lambda.LambdaTemplate;
import net.hasor.db.lambda.MapQueryOperation;
import net.hasor.db.lambda.core.AbstractSelectLambda;
import net.hasor.db.mapping.TableReader;
import net.hasor.db.mapping.def.TableMapping;

import java.util.Map;

/**
 * 提供 lambda update 能力，是 LambdaUpdate 接口的实现类。
 * @version : 2020-10-27
 * @author 赵永春 (zyc@hasor.net)
 */
public class SelectLambdaForMap extends AbstractSelectLambda<MapQueryOperation, Map<String, Object>, String> //
        implements MapQueryOperation {

    public SelectLambdaForMap(TableMapping<?> tableMapping, LambdaTemplate jdbcTemplate) {
        super(Map.class, tableMapping, jdbcTemplate);
    }

    @Override
    protected MapQueryOperation getSelf() {
        return this;
    }

    @Override
    protected String getPropertyName(String property) {
        return property;
    }

    @Override
    protected TableReader<Map<String, Object>> getTableReader() {
        return getTableMapping().toMapReader();
    }
}