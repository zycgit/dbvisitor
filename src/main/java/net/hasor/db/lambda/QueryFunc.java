/*
 * Copyright 2002-2005 the original author or authors.
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
package net.hasor.db.lambda;
import net.hasor.cobble.reflect.SFunction;
import net.hasor.db.dialect.Page;

/**
 * Query 复杂操作构造器。
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public interface QueryFunc<T, R> {
    /**
     * 查询所有列
     * 在分组查询下：返回所有分组列 */
    public R selectAll();

    /**
     * 查询属性
     * 在分组查询下：设置参数中，只有 group by 列才会被查询。 */
    public R select(String... columns);

    /**
     * 查询属性
     * 在分组查询下：设置参数中，只有 group by 列才会被查询。 */
    public R select(SFunction<T>... properties);

    /**分组，类似：group by xxx */
    public R groupBy(SFunction<T>... properties);

    /** 排序，类似：order by xxx */
    public R orderBy(SFunction<T>... properties);

    /** 排序(升序)，类似：order by xxx desc */
    public R asc(SFunction<T>... properties);

    /** 排序(降序)，类似：order by xxx desc */
    public R desc(SFunction<T>... properties);

    /** 设置分页信息 */
    public R usePage(Page pageInfo);

    /** 获取对应的分页对象 */
    public Page pageInfo();

    /** 生成分页对象 */
    public R initPage(int pageSize, int pageNumber);
}
