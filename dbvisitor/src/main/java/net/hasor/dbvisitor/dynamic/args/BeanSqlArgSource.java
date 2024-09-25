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
package net.hasor.dbvisitor.dynamic.args;
import net.hasor.cobble.BeanUtils;
import net.hasor.cobble.function.Property;
import net.hasor.dbvisitor.dynamic.SqlArgSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @version : 2014-3-31
 * @author 赵永春 (zyc@hasor.net)
 */
public class BeanSqlArgSource implements SqlArgSource, SqlArgDisposer {
    private final Object                dataBean;
    private final Map<String, Property> dataProperty;
    private final Map<String, Object>   bindData;

    public BeanSqlArgSource(Object dataBean) {
        this.dataBean = Objects.requireNonNull(dataBean);
        this.dataProperty = BeanUtils.getPropertyFunc(dataBean.getClass());
        this.bindData = new HashMap<>();
    }

    @Override
    public boolean hasValue(final String paramName) {
        return this.dataProperty.containsKey(paramName);
    }

    @Override
    public Object getValue(final String paramName) throws IllegalArgumentException {
        Property property = this.dataProperty.get(paramName);
        if (property == null || this.bindData.containsKey(paramName)) {
            return this.bindData.get(paramName);
        } else {
            return property.get(this.dataBean);
        }
    }

    @Override
    public void putValue(String paramName, Object value) {
        Property property = this.dataProperty.get(paramName);
        if (property == null || property.isReadOnly()) {
            this.bindData.put(paramName, value);
        } else {
            property.set(this.dataBean, value);
        }
    }

    @Override
    public String[] getParameterNames() {
        return this.dataProperty.keySet().toArray(new String[0]);
    }

    @Override
    public void cleanupParameters() {
        if (this.dataBean instanceof SqlArgDisposer) {
            ((SqlArgDisposer) this.dataBean).cleanupParameters();
        }
    }
}