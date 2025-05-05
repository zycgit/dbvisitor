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

import java.util.*;

/**
 * 基于Java Bean 对象的 SQL 参数源实现类。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2014-3-31
 */
public class BeanSqlArgSource extends BindSqlArgSource implements SqlArgSource, SqlArgDisposer {
    private final Object                dataBean;
    private final Map<String, Property> dataProperty;

    /**
     * 构造函数，初始化Bean参数源
     * @param dataBean 作为参数源的Java Bean对象
     * @throws NullPointerException 如果dataBean为null
     */
    public BeanSqlArgSource(Object dataBean) {
        this.dataBean = Objects.requireNonNull(dataBean);
        this.dataProperty = BeanUtils.getPropertyFunc(dataBean.getClass());
    }

    /**
     * 检查参数是否存在
     * @param paramName 参数名
     * @return true表示存在(在绑定参数或Bean属性中存在)
     */
    @Override
    public boolean hasValue(final String paramName) {
        if (this.bindValues.containsKey(paramName)) {
            return true;
        } else {
            return this.dataProperty.containsKey(paramName);
        }
    }

    /**
     * 获取参数值
     * @param paramName 参数名
     * @return 参数值(优先返回绑定参数 ， 其次返回Bean属性值)
     * @throws IllegalArgumentException 如果参数不存在
     */
    @Override
    public Object getValue(final String paramName) throws IllegalArgumentException {
        if (this.bindValues.containsKey(paramName)) {
            return this.bindValues.get(paramName);
        } else if (this.dataProperty.containsKey(paramName)) {
            Property property = this.dataProperty.get(paramName);
            return property.get(this.dataBean);
        } else {
            return null;
        }
    }

    /**
     * 获取所有参数名，合并绑定参数名和 Bean 属性名的数组
     */
    @Override
    public String[] getParameterNames() {
        Set<String> names = new HashSet<>();
        names.addAll(Arrays.asList(super.getParameterNames()));
        names.addAll(this.dataProperty.keySet());
        return names.toArray(new String[0]);
    }

    /**
     * 清理参数资源
     * 会遍历所有参数值，如果参数实现了 {@link SqlArgDisposer} 接口，则调用其方法进行清理。
     */
    @Override
    public void cleanupParameters() {
        super.cleanupParameters();
        if (this.dataBean instanceof SqlArgDisposer) {
            ((SqlArgDisposer) this.dataBean).cleanupParameters();
        }
    }
}