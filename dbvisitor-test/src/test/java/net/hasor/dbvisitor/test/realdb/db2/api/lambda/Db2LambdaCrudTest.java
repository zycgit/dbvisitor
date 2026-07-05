package net.hasor.dbvisitor.test.realdb.db2.api.lambda;

import net.hasor.dbvisitor.test.contract.api.lambda.LambdaCrudContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Db2Profile;

public class Db2LambdaCrudTest extends LambdaCrudContractTest {
    @Override
    protected DataSourceProfile profile() {
        return Db2Profile.INSTANCE;
    }
}
