package net.hasor.dbvisitor.test.realdb.clickhouse.api.lambda;

import net.hasor.dbvisitor.test.contract.api.lambda.LambdaCrudContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseLambdaCrudTest extends LambdaCrudContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
