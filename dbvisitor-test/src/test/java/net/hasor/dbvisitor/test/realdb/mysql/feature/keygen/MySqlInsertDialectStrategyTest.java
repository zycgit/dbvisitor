/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mysql.feature.keygen;

import net.hasor.dbvisitor.lambda.GeneratedKeyStrategy;
import net.hasor.dbvisitor.test.contract.feature.keygen.InsertDialectStrategyCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlInsertDialectStrategyTest extends InsertDialectStrategyCase {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }

    @Override
    protected GeneratedKeyStrategy expectedReturnColumnsIntoStrategy() {
        return GeneratedKeyStrategy.JdbcBatchGeneratedKeys;
    }

    @Override
    protected boolean expectedUpdateWithKeyOnlyColumnsSupport() {
        return true;
    }

    @Override
    protected boolean expectedIgnoreWithoutPrimaryKeySupport() {
        return true;
    }

    @Override
    protected boolean expectedUpdateWithoutPrimaryKeySupport() {
        return true;
    }
}
