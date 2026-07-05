package net.hasor.dbvisitor.test.realdb.mssql.api.lambda;

import net.hasor.dbvisitor.test.contract.api.lambda.LambdaBatchMutationContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;

public class MsSqlLambdaBatchMutationContractTest extends LambdaBatchMutationContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }
}
