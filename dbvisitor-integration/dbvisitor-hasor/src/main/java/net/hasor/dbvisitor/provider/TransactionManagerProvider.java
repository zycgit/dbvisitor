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
package net.hasor.dbvisitor.provider;
import net.hasor.cobble.provider.SingleProvider;
import net.hasor.dbvisitor.transaction.TransactionManager;
import net.hasor.dbvisitor.transaction.support.LocalTransactionManager;

import javax.sql.DataSource;
import java.util.function.Supplier;

/**
 *
 * @version : 2015年11月10日
 * @author 赵永春 (zyc@hasor.net)
 */
public class TransactionManagerProvider implements Supplier<TransactionManager> {
    private final    Supplier<DataSource> dataSource;
    private volatile TransactionManager   transactionManager;

    public TransactionManagerProvider(DataSource dataSource) {
        this(() -> dataSource);
    }

    public TransactionManagerProvider(Supplier<DataSource> dataSource) {
        this.dataSource = new SingleProvider<>(dataSource);
    }

    public TransactionManager get() {
        if (this.transactionManager == null) {
            synchronized (this) {
                if (this.transactionManager == null) {
                    this.transactionManager = new LocalTransactionManager(this.dataSource.get());
                }
            }
        }
        return this.transactionManager;
    }
}