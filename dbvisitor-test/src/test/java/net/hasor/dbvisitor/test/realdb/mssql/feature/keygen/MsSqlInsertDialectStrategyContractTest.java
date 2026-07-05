package net.hasor.dbvisitor.test.realdb.mssql.feature.keygen;

import net.hasor.dbvisitor.lambda.GeneratedKeyStrategy;
import net.hasor.dbvisitor.test.contract.feature.keygen.InsertDialectStrategyContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;

public class MsSqlInsertDialectStrategyContractTest extends InsertDialectStrategyContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }

    @Override
    protected GeneratedKeyStrategy expectedReturnColumnsIntoStrategy() {
        return GeneratedKeyStrategy.MultiValuesResultSet;
    }
}
