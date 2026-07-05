package net.hasor.dbvisitor.test.realdb.clickhouse.api.lambda;

import net.hasor.dbvisitor.test.contract.api.lambda.LambdaDuplicateStrategyContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseLambdaDuplicateStrategyContractTest extends LambdaDuplicateStrategyContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
