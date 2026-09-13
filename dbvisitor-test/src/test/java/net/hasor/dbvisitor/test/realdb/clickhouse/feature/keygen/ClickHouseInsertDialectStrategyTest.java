/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.clickhouse.feature.keygen;

import net.hasor.dbvisitor.test.contract.feature.keygen.InsertDialectStrategyCase;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;

public class ClickHouseInsertDialectStrategyTest extends InsertDialectStrategyCase {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }

    @Override
    protected boolean expectedIgnoreSupport() {
        return false;
    }

    @Override
    protected boolean expectedUpdateSupport() {
        return false;
    }

    @Override
    protected boolean expectedUpdateWithKeyOnlyColumnsSupport() {
        return false;
    }
}
