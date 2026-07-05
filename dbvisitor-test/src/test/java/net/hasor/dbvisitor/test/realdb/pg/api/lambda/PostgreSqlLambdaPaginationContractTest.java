package net.hasor.dbvisitor.test.realdb.pg.api.lambda;

import net.hasor.dbvisitor.test.contract.api.lambda.LambdaPaginationContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlLambdaPaginationContractTest extends LambdaPaginationContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
