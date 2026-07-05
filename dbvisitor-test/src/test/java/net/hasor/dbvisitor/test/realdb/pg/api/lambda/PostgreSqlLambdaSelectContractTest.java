package net.hasor.dbvisitor.test.realdb.pg.api.lambda;

import net.hasor.dbvisitor.test.contract.api.lambda.LambdaSelectContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlLambdaSelectContractTest extends LambdaSelectContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
