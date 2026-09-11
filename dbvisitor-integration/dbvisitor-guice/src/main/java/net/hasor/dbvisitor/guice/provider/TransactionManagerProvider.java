/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.guice.provider;
import javax.sql.DataSource;
import com.google.inject.Provider;
import net.hasor.dbvisitor.transaction.TransactionManager;
import net.hasor.dbvisitor.transaction.support.TransactionHelper;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-07-18
 */
public class TransactionManagerProvider implements Provider<TransactionManager> {
    private final Provider<DataSource> dataSource;

    public TransactionManagerProvider(Provider<DataSource> dataSource) {
        this.dataSource = new InnerSingleProvider<>(dataSource);
    }

    public TransactionManager get() {
        return TransactionHelper.txManager(this.dataSource.get());
    }
}
