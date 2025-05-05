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
 * 基于Map的SQL参数源实现类，实现了 SqlArgSource 和 SqlArgDisposer 接口
 * 主要功能：
 * 1. 提供参数绑定和访问的基础实现
 * 2. 支持Supplier接口的延迟参数值获取
 * 3. 管理参数资源的生命周期
 * @author 赵永春 (zyc@hasor.net)
 * @version 2014-3-31
 */
public class BindSqlArgSource implements SqlArgSource, SqlArgDisposer {
    protected final Map<String, Object> bindValues;

    /** 默认构造函数，初始化空参数 Map */
    public BindSqlArgSource() {
        this.bindValues = new HashMap<>();
    }

    /**
     * 使用已有参数Map初始化
     * @param paramMap 初始参数 Map，如果 为null 则使用空 Map
     */
    public BindSqlArgSource(Map<String, Object> paramMap) {
        this.bindValues = new HashMap<>(paramMap == null ? Collections.emptyMap() : paramMap);
    }

    /**
     * 检查参数是否存在
     * @param paramName 参数名
     * @return 如果参数存在返回 true
     */
    @Override
    public boolean hasValue(final String paramName) {
        return this.bindValues.containsKey(paramName);
    }

    /**
     * 获取参数值，支持 {@link Supplier} 接口的延迟获取
     * @param paramName 参数名
     * @return 参数值，如果是 Supplier 则调用 get() 方法获取实际值
     * @throws IllegalArgumentException 如果参数不存在
     */
    @Override
    public Object getValue(final String paramName) throws IllegalArgumentException {
        Object object = this.bindValues.get(paramName);
        if (object instanceof Supplier) {
            object = ((Supplier<?>) object).get();
        }
        return object;
    }

    /**
     * 添加或更新参数值
     * @param paramName 参数名
     * @param value 参数值
     */
    @Override
    public void putValue(String paramName, Object value) {
        this.bindValues.put(paramName, value);
    }

    /**
     * 获取所有参数名的数组
     * @return 包含所有参数名的 String 数组
     */
    @Override
    public String[] getParameterNames() {
        return this.bindValues.keySet().toArray(new String[0]);
    }

    /**
     * 清理所有参数资源
     * 会遍历所有参数值，如果参数实现了 {@link SqlArgDisposer} 接口，则调用其方法进行清理。
     */
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