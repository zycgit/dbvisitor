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
package net.hasor.dbvisitor.wrapper.support.map;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.wrapper.MapDeleteWrapper;
import net.hasor.dbvisitor.wrapper.core.AbstractDeleteWrapper;

import java.util.Map;

/**
 * 提供 lambda delete 能力，是 MapDeleteOperation 接口的实现类。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2022-04-02
 */
public class DeleteWrapperForMap extends AbstractDeleteWrapper<MapDeleteWrapper, Map<String, Object>, String> //
        implements MapDeleteWrapper {

    public DeleteWrapperForMap(TableMapping<?> tableMapping, MappingRegistry registry, JdbcTemplate jdbc) {
        super(Map.class, tableMapping, registry, jdbc);
    }

    @Override
    protected MapDeleteWrapper getSelf() {
        return this;
    }

    @Override
    protected String getPropertyName(String property) {
        return property;
    }
}
