package net.hasor.dbvisitor.test.realdb.pg.feature.keygen;

import net.hasor.dbvisitor.lambda.GeneratedKeyStrategy;
import net.hasor.dbvisitor.test.contract.feature.keygen.InsertDialectStrategyContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlInsertDialectStrategyContractTest extends InsertDialectStrategyContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }

    @Override
    protected GeneratedKeyStrategy expectedReturnColumnsIntoStrategy() {
        return GeneratedKeyStrategy.MultiValuesResultSet;
    }

    @Override
    protected GeneratedKeyStrategy expectedReturnColumnsUpdateStrategy() {
        return GeneratedKeyStrategy.MultiValuesResultSet;
    }

    @Override
    protected boolean expectedUpdateWithKeyOnlyColumnsSupport() {
        return false;
    }

    @Override
    protected boolean expectedIgnoreWithoutPrimaryKeySupport() {
        return true;
    }
}
