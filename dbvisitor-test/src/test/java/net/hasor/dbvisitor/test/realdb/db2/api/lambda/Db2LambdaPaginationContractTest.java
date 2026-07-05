package net.hasor.dbvisitor.test.realdb.db2.api.lambda;

import net.hasor.dbvisitor.test.contract.api.lambda.LambdaPaginationContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Db2Profile;

public class Db2LambdaPaginationContractTest extends LambdaPaginationContractTest {
    @Override
    protected DataSourceProfile profile() {
        return Db2Profile.INSTANCE;
    }
}
