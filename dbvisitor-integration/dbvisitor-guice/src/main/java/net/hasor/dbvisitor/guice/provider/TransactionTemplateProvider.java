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
import net.hasor.dbvisitor.transaction.TransactionTemplate;
import net.hasor.dbvisitor.transaction.TransactionTemplateManager;
import net.hasor.dbvisitor.transaction.support.TransactionHelper;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-07-18
 */
public class TransactionTemplateProvider implements Provider<TransactionTemplate> {
    private final Provider<DataSource> dataSource;

    public TransactionTemplateProvider(Provider<DataSource> dataSource) {
        this.dataSource = dataSource;
    }

    public TransactionTemplate get() {
        return new TransactionTemplateManager(TransactionHelper.txManager(dataSource.get()));
    }
}
