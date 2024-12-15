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
package net.hasor.dbvisitor.mapper.def;
import net.hasor.dbvisitor.dynamic.DynamicSql;

import java.util.function.Function;

/**
 * Insert SqlConfig
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-06-19
 */
public class InsertConfig extends DmlConfig {
    private String keyProperty;

    public InsertConfig(DynamicSql target, Function<String, String> config) {
        super(target, config);

        if (this.getSelectKey() == null) {
            this.keyProperty = config.apply(INSERT_KEY_PROPERTY);
        } else {
            this.keyProperty = this.getSelectKey().getKeyProperty();
        }
    }

    @Override
    public QueryType getType() {
        return QueryType.Insert;
    }

    public String getKeyProperty() {
        return keyProperty;
    }

    public void setKeyProperty(String keyProperty) {
        this.keyProperty = keyProperty;
    }
}