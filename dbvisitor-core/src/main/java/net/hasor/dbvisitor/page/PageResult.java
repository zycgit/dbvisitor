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

import java.util.List;

/**
 * 分页查询的结果，含有分页信息。
 * @version : 2021-07-20
 * @author 赵永春 (zyc@hasor.net)
 */
public class PageResult<T> extends PageObject {
    private List<T> data;

    public PageResult(Page pageInfo, int totalCount) {
        super(pageInfo.getPageSize(), totalCount);
        this.setCurrentPage(pageInfo.getCurrentPage());
        this.setPageNumberOffset(pageInfo.getPageNumberOffset());
    }

    public PageResult(Page pageInfo, int totalCount, List<T> data) {
        this(pageInfo, totalCount);
        this.data = data;
    }

    public List<T> getData() {
        return this.data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
