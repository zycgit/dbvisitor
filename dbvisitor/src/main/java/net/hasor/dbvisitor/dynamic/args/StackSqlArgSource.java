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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import net.hasor.dbvisitor.dynamic.SqlArgSource;

/**
 * 基于 Map 的 SQL 参数源实现类，实现了 SqlArgSource 和 SqlArgDisposer 接口
 * 主要功能：
 * 1. 提供参数绑定和访问的基础实现
 * 2. 支持Supplier接口的延迟参数值获取
 * 3. 管理参数资源的生命周期
 * @author 赵永春 (zyc@hasor.net)
 * @version 2014-3-31
 */
public class StackSqlArgSource extends BindSqlArgSource {
    private final SqlArgSource target;

    /** 默认构造函数，初始化空参数 Map */
    public StackSqlArgSource(SqlArgSource target) {
        this.target = Objects.requireNonNull(target);
    }

    /**
     * 检查参数是否存在
     * @param paramName 参数名
     * @return 如果参数存在返回 true
     */
    @Override
    public boolean hasValue(final String paramName) {
        if (this.bindValues.containsKey(paramName)) {
            return true;
        } else {
            return this.target.hasValue(paramName);
        }
    }

    /**
     * 获取参数值，支持 {@link Supplier} 接口的延迟获取
     * @param paramName 参数名
     * @return 参数值，如果是 Supplier 则调用 get() 方法获取实际值
     * @throws IllegalArgumentException 如果参数不存在
     */
    @Override
    public Object getValue(final String paramName) throws IllegalArgumentException {
        if (this.bindValues.containsKey(paramName)) {
            return super.getValue(paramName);
        } else {
            return this.target.getValue(paramName);
        }
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
        Set<String> tmpKeys = new HashSet<>();
        tmpKeys.addAll(this.bindValues.keySet());
        tmpKeys.addAll(Arrays.asList(this.target.getParameterNames()));
        return tmpKeys.toArray(new String[0]);
    }

    /**
     * 清理所有参数资源
     * 会遍历所有参数值，如果参数实现了 {@link SqlArgDisposer} 接口，则调用其方法进行清理。
     */
    @Override
    public void cleanupParameters() {
        if (this.target instanceof SqlArgDisposer) {
            ((SqlArgDisposer) this.target).cleanupParameters();
        }
        super.cleanupParameters();
    }
}