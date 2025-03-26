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
import net.hasor.dbvisitor.dynamic.SqlArgSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 一个 Map 到 SqlParameterSource 的桥，同时支持自动识别 Supplier 接口以获取具体参数。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2014-3-31
 */
public class BindSqlArgSource implements SqlArgSource, SqlArgDisposer {
    protected final Map<String, Object> bindValues;

    public BindSqlArgSource() {
        this.bindValues = new HashMap<>();
    }

    public BindSqlArgSource(Map<String, Object> paramMap) {
        this.bindValues = new HashMap<>(paramMap == null ? Collections.emptyMap() : paramMap);
    }

    @Override
    public boolean hasValue(final String paramName) {
        return this.bindValues.containsKey(paramName);
    }

    @Override
    public Object getValue(final String paramName) throws IllegalArgumentException {
        Object object = this.bindValues.get(paramName);
        if (object instanceof Supplier) {
            object = ((Supplier<?>) object).get();
        }
        return object;
    }

    @Override
    public void putValue(String paramName, Object value) {
        this.bindValues.put(paramName, value);
    }

    @Override
    public String[] getParameterNames() {
        return this.bindValues.keySet().toArray(new String[0]);
    }

    @Override
    public void cleanupParameters() {
        for (String name : this.bindValues.keySet()) {
            Object obj = this.bindValues.get(name);
            if (obj instanceof SqlArgDisposer) {
                ((SqlArgDisposer) obj).cleanupParameters();
            }
        }
    }
}