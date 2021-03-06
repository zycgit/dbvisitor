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
package net.hasor.dbvisitor.page;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 分页
 * @version : 2021-02-04
 * @author 赵永春 (zyc@hasor.net)
 */
public interface Page {
    /** 获取页大小，默认是 -1 表示无穷大。 */
    int getPageSize();

    /** 设置分页的页大小，-1 表示无穷大 */
    void setPageSize(int pageSize);

    /**取当前页号 */
    int getCurrentPage();

    /** 设置前页号 */
    void setCurrentPage(int currentPage);

    /** 页码偏移量（例如：从1页作为起始页，可以设置为 1。否则第一页的页码是 0） */
    int getPageNumberOffset();

    /** 设置页码偏移量（例如：从1页作为起始页，可以设置为 1。否则第一页的页码是 0） */
    void setPageNumberOffset(int pageNumberOffset);

    /** 获取本页第一个记录的索引位置 */
    int getFirstRecordPosition();

    /** 获取总页数 */
    int getTotalPage() throws SQLException;

    /** 获取记录总数 */
    int getTotalCount() throws SQLException;

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
    default void lastPage() throws SQLException {
        setCurrentPage(getTotalPage() - 1);
    }

    /** 获取分页信息 */
    default Map<String, Object> toPageInfo() throws SQLException {
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
