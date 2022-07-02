/*
 * Copyright 2008-2009 the original 赵永春(zyc@hasor.net).
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
package net.hasor.db.ar;
import java.util.ArrayList;
import java.util.List;
/**
 * 带有翻页信息的结果集
 * @version : 2014年10月25日
 * @author 赵永春(zyc@hasor.net)
 */
public class PageResult<T> extends Paginator {
    private List<T> resultList = new ArrayList<T>(0);
    //
    public PageResult(Paginator pageInfo) {
        this(pageInfo, null);
    }
    public PageResult(List<T> resultList) {
        this(null, resultList);
    }
    public PageResult(Paginator pageInfo, List<T> resultList) {
        if (resultList != null) {
            this.resultList = resultList;
        }
        if (pageInfo != null) {
            this.setPageSize(pageInfo.getPageSize());
            this.setTotalCount(pageInfo.getTotalCount());
            this.setCurrentPage(pageInfo.getCurrentPage());
        }
    }
    /**获取分页结果集。*/
    public List<T> getResultList() {
        return this.resultList;
    }
}