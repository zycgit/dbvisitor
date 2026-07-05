package net.hasor.dbvisitor.test.realdb.mysql.api.lambda;

import net.hasor.dbvisitor.test.contract.api.lambda.LambdaEmptyResultContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlLambdaEmptyResultContractTest extends LambdaEmptyResultContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
