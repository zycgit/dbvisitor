package net.hasor.dbvisitor.test.realdb.mysql.api.lambda;

import net.hasor.dbvisitor.test.contract.api.lambda.LambdaDuplicateStrategyContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlLambdaDuplicateStrategyContractTest extends LambdaDuplicateStrategyContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
