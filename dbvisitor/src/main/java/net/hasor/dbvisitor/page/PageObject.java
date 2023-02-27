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
package net.hasor.dbvisitor.page;
/**
 * 分页接口 Page 的实现类
 * @version : 2021-02-04
 * @author 赵永春 (zyc@hasor.net)
 */
public class PageObject implements Page {
    /** 满足条件的总记录数 */
    private long    totalCount        = 0;
    /** 每页记录数（-1表示无限大）*/
    private long    pageSize          = 0;
    /** 当前页号 */
    private long    currentPage       = 0;
    /** 起始页码的偏移量 */
    private long    pageNumberOffset  = 0;
    /** 是否刷新总记录数 */
    private boolean refreshTotalCount = false;

    public PageObject() {
    }

    public PageObject(long pageSize, long totalCount) {
        this.pageSize = pageSize;
        this.totalCount = totalCount;
    }

    /** 获取分页的页大小 */
    public long getPageSize() {
        return this.pageSize;
    }

    /** 设置分页的页大小 */
    public void setPageSize(long pageSize) {
        this.pageSize = Math.max(pageSize, 0);
    }

    /**取当前页号 */
    public long getCurrentPage() {
        if (this.pageSize > 0) {
            return this.currentPage + this.pageNumberOffset;
        } else {
            return this.pageNumberOffset;
        }
    }

    /** 设置前页号 */
    public void setCurrentPage(long currentPage) {
        if (currentPage <= this.pageNumberOffset) {
            this.currentPage = 0;
        } else {
            this.currentPage = currentPage - this.pageNumberOffset;
        }
    }

    /** 获得分页的第一页的页码 */
    public long getPageNumberOffset() {
        return this.pageNumberOffset;
    }

    /** 设置分页的第一页的页码，必须大于等于 0  */
    public void setPageNumberOffset(long pageNumberOffset) {
        this.pageNumberOffset = Math.max(pageNumberOffset, 0);
    }

    /** 获取是否刷新总记录数 */
    public boolean isRefreshTotalCount() {
        return this.refreshTotalCount;
    }

    /** 设置是否刷新总记录数 */
    public void setRefreshTotalCount(boolean refreshTotalCount) {
        this.refreshTotalCount = refreshTotalCount;
    }

    @Override
    public void refreshTotalCount() {
        this.setRefreshTotalCount(true);
    }

    /** 获取本页第一个记录的索引位置 */
    public long getFirstRecordPosition() {
        long pgSize = getPageSize();
        if (pgSize <= 0) {
            return 0;
        }
        return pgSize * this.currentPage;
    }

    /** 获取总页数 */
    public long getTotalPage() {
        long pgSize = getPageSize();
        if (pgSize > 0) {
            long totalCount = getTotalCount();
            if (totalCount == 0) {
                return this.pageNumberOffset;
            }
            long result = totalCount / pgSize;
            if ((totalCount % pgSize) != 0) {
                result++;
            }
            return result + this.pageNumberOffset;
        } else {
            long totalCount = getTotalCount();
            if (totalCount > 0) {
                return this.pageNumberOffset + 1;
            } else {
                return this.pageNumberOffset;
            }
        }
    }

    /** 获取记录总数 */
    public long getTotalCount() {
        return this.totalCount;
    }

    /** 设置记录总数 */
    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }
}
