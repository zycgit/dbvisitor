package net.hasor.dbvisitor.test.realdb.oracle.feature.keygen;

import net.hasor.dbvisitor.lambda.GeneratedKeyStrategy;
import net.hasor.dbvisitor.test.contract.feature.keygen.InsertDialectStrategyContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleInsertDialectStrategyContractTest extends InsertDialectStrategyContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }

    @Override
    protected GeneratedKeyStrategy expectedNoReturnColumnsStrategy() {
        return GeneratedKeyStrategy.OneByOne;
    }
}
