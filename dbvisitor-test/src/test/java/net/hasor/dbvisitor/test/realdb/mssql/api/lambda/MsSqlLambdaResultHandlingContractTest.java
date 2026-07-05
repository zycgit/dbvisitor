package net.hasor.dbvisitor.test.realdb.mssql.api.lambda;

import net.hasor.dbvisitor.test.contract.api.lambda.LambdaResultHandlingContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;

public class MsSqlLambdaResultHandlingContractTest extends LambdaResultHandlingContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }
}
