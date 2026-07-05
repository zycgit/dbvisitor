package net.hasor.dbvisitor.test.realdb.clickhouse.api.lambda;

import net.hasor.dbvisitor.test.contract.api.lambda.LambdaPredicateContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseLambdaPredicateContractTest extends LambdaPredicateContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
