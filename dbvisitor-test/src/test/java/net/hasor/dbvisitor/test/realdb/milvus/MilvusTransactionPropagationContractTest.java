/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import net.hasor.dbvisitor.test.contract.feature.transaction.TransactionPropagationContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;

/** Binds the common scenarios to the explicitly declared Milvus boundary. */
public class MilvusTransactionPropagationContractTest extends TransactionPropagationContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }
}
