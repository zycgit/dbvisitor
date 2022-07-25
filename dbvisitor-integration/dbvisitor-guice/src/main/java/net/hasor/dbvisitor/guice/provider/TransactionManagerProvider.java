/*
 * Copyright 2008-2009 the original author or authors.
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
package net.hasor.dbvisitor.guice.provider;
import com.google.inject.Provider;
import net.hasor.dbvisitor.transaction.DataSourceUtils;
import net.hasor.dbvisitor.transaction.TransactionManager;

import javax.sql.DataSource;

/**
 *
 * @version : 2015年11月10日
 * @author 赵永春 (zyc@hasor.net)
 */
public class TransactionManagerProvider implements Provider<TransactionManager> {
    private final Provider<DataSource> dataSource;

    public TransactionManagerProvider(DataSource dataSource) {
        this(() -> dataSource);
    }

    public TransactionManagerProvider(Provider<DataSource> dataSource) {
        this.dataSource = dataSource;
    }

    public TransactionManager get() {
        return DataSourceUtils.getManager(this.dataSource.get());
    }
}