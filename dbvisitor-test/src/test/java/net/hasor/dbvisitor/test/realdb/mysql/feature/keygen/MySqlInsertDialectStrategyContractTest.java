package net.hasor.dbvisitor.test.realdb.mysql.feature.keygen;

import net.hasor.dbvisitor.lambda.GeneratedKeyStrategy;
import net.hasor.dbvisitor.test.contract.feature.keygen.InsertDialectStrategyContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlInsertDialectStrategyContractTest extends InsertDialectStrategyContractTest {
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
