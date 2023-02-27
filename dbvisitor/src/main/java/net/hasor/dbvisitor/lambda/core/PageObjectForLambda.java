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
package net.hasor.dbvisitor.lambda.core;
import net.hasor.cobble.ExceptionUtils;
import net.hasor.cobble.function.ESupplier;
import net.hasor.dbvisitor.page.PageObject;

import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 分页接口 Page 的实现类
 * @version : 2021-02-04
 * @author 赵永春 (zyc@hasor.net)
 */
class PageObjectForLambda extends PageObject {
    /** 满足条件的总记录数 */
    private final ESupplier<Long, SQLException> totalCountSupplier;
    private final AtomicBoolean                 totalCountInited = new AtomicBoolean(false);

    public PageObjectForLambda(long pageSize, ESupplier<Long, SQLException> totalCountSupplier) {
        super(pageSize, 0);
        Objects.requireNonNull(totalCountSupplier, "totalCountSupplier is null.");
        this.totalCountSupplier = totalCountSupplier;
    }

    @Override
    public void refreshTotalCount() {
        super.refreshTotalCount();
        this.getTotalCount();
    }

    /** 获取记录总数 */
    public long getTotalCount() {
        if (this.isRefreshTotalCount() || this.totalCountInited.compareAndSet(false, true)) {
            try {
                super.setTotalCount(this.totalCountSupplier.eGet());
            } catch (SQLException e) {
                throw ExceptionUtils.toRuntime(e);
            }
        }
        return super.getTotalCount();
    }

    public void setTotalCount(long totalCount) {
        super.setTotalCount(totalCount);
        this.totalCountInited.set(true);
    }
}
