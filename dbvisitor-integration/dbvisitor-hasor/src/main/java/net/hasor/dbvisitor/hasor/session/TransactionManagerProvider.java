/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.hasor.session;
import java.util.function.Supplier;
import javax.sql.DataSource;
import net.hasor.cobble.provider.SingleProvider;
import net.hasor.dbvisitor.transaction.TransactionManager;
import net.hasor.dbvisitor.transaction.support.TransactionHelper;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2017-07-12
 */
public class TransactionManagerProvider implements Supplier<TransactionManager> {
    private final Supplier<DataSource> dataSource;

    public TransactionManagerProvider(Supplier<DataSource> dataSource) {
        this.dataSource = new SingleProvider<>(dataSource);
    }

    public TransactionManager get() {
        return TransactionHelper.txManager(this.dataSource.get());
    }
}
