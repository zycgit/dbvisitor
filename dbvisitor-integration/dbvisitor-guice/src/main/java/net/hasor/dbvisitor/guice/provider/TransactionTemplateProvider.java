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
package net.hasor.dbvisitor.guice.provider;
import com.google.inject.Provider;
import net.hasor.dbvisitor.transaction.TransactionManager;
import net.hasor.dbvisitor.transaction.TransactionTemplate;
import net.hasor.dbvisitor.transaction.TransactionTemplateManager;
import net.hasor.dbvisitor.transaction.support.LocalTransactionManager;

import javax.sql.DataSource;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2015年11月10日
 */
public class TransactionTemplateProvider implements Provider<TransactionTemplate> {
    private final TransactionTemplate transactionTemplate;

    public TransactionTemplateProvider(DataSource dataSource) {
        TransactionManager tm = new LocalTransactionManager(dataSource);
        this.transactionTemplate = new TransactionTemplateManager(tm);
    }

    public TransactionTemplateProvider(TransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplateManager(transactionManager);
    }

    public TransactionTemplate get() {
        return this.transactionTemplate;
    }
}