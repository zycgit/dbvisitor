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
import net.hasor.dbvisitor.transaction.TransactionTemplate;
import net.hasor.dbvisitor.transaction.TransactionTemplateManager;
import net.hasor.dbvisitor.transaction.support.TransactionHelper;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2017-07-12
 */
public class TransactionTemplateProvider implements Supplier<TransactionTemplate> {
    private final Supplier<DataSource> dataSource;

    public TransactionTemplateProvider(Supplier<DataSource> dataSource) {
        this.dataSource = new SingleProvider<>(dataSource);
    }

    public TransactionTemplate get() {
        return new TransactionTemplateManager(TransactionHelper.txManager(this.dataSource.get()));
    }
}
