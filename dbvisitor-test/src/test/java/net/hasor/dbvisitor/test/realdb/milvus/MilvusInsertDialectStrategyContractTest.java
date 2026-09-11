/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import net.hasor.dbvisitor.lambda.GeneratedKeyStrategy;
import net.hasor.dbvisitor.test.contract.feature.keygen.InsertDialectStrategyContractTest;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.Before;

/** Local dialect strategy checks; no database SQL is executed by this contract. */
public class MilvusInsertDialectStrategyContractTest extends InsertDialectStrategyContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() {
        OneApiDataSourceManager.assumeCurrentDataSource(profile().env());
    }

    @Override
    protected GeneratedKeyStrategy expectedNoReturnColumnsStrategy() {
        return GeneratedKeyStrategy.OneByOne;
    }

    @Override
    protected boolean expectedIgnoreSupport() {
        return false;
    }
}
