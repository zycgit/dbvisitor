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
package net.hasor.dbvisitor.dialect;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 分页
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-02-04
 */
public interface Page {
    /** 获取页大小，默认是 -1 表示无穷大。 */
    long getPageSize();

    /** 设置分页的页大小，-1 表示无穷大 */
    void setPageSize(long pageSize);

    /** 取当前页号 */
    long getCurrentPage();

    /** 设置前页号 */
    void setCurrentPage(long currentPage);

    /** 页码偏移量（例如：从1页作为起始页，可以设置为 1。否则第一页的页码是 0） */
    long getPageNumberOffset();

    /** 设置页码偏移量（例如：从1页作为起始页，可以设置为 1。否则第一页的页码是 0） */
    void setPageNumberOffset(long pageNumberOffset);

    /** 获取本页第一个记录的索引位置 */
    long getFirstRecordPosition();

    /** 获取总页数 */
    long getTotalPage();

    /** 获取记录总数 */
    long getTotalCount();

    /** 设置记录总数 */
    void setTotalCount(long totalCount);

    /** 无论 totalCount 是否设置了值，分页查询都将会执行 select count 语句用以刷新总数 */
    void refreshTotalCount();

    /** 获取是否刷新总记录数 */
    boolean isRefreshTotalCount();

    /** 移动到第一页 */
    default void firstPage() {
        setCurrentPage(0);
    }

    /** 移动到上一页 */
    default void previousPage() {
        setCurrentPage(getCurrentPage() - 1);
    }

    /** 移动到下一页 */
    default void nextPage() {
        setCurrentPage(getCurrentPage() + 1);
    }

    /** 移动到最后一页 */
    default void lastPage() {
        setCurrentPage(getTotalPage() - 1);
    }

    /** 获取分页信息 */
    default Map<String, Object> toPageInfo() {
        return new LinkedHashMap<String, Object>() {{
            put("enable", getPageSize() > 0);
            put("pageSize", getPageSize());
            put("totalCount", getTotalCount());
            put("totalPage", getTotalPage());
            put("currentPage", getCurrentPage());
            put("recordPosition", getFirstRecordPosition());
        }};
    }
}
