package net.hasor.dbvisitor.test.realdb.pg.api.lambda;

import net.hasor.dbvisitor.test.contract.api.lambda.LambdaLogicalConditionContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlLambdaLogicalConditionContractTest extends LambdaLogicalConditionContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
