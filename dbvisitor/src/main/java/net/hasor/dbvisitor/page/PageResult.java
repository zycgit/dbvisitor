/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.page;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页查询的结果，含有分页信息。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-07-20
 */
public class PageResult<T> extends PageObject {
    private List<T> data;

    /** 多用于首次分页结果构建 */
    public PageResult() {
        super(0, 0, 0);
        this.data = new ArrayList<>();
    }

    /** 多用于首次分页结果构建 */
    public PageResult(long pageNumber, long pageSize) {
        super(pageNumber, pageSize, 0);
        this.data = new ArrayList<>();
    }

    /** 多用于首次分页结果构建 */
    public PageResult(long pageNumber, long pageSize, long totalCount, List<T> data) {
        super(pageNumber, pageSize, totalCount);
        this.data = data == null ? new ArrayList<>() : data;
    }

    /** 多用于二次分页结果构建 */
    public PageResult(Page pageInfo) {
        this(pageInfo, new ArrayList<>());
    }

    /** 多用于二次分页结果构建 */
    public PageResult(Page pageInfo, List<T> data) {
        super(pageInfo.getCurrentPage(), pageInfo.getPageSize(), pageInfo.getTotalCount());
        this.setPageNumberOffset(pageInfo.getPageNumberOffset());
        this.data = data == null ? new ArrayList<>() : data;
    }

    /** 获取分页数据 */
    public List<T> getData() {
        return this.data;
    }

    /** 设置分页数据 */
    public void setData(List<T> data) {
        this.data = data;
    }
}
