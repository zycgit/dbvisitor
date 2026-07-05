package net.hasor.dbvisitor.test.realdb.h2.api.lambda;

import net.hasor.dbvisitor.test.contract.api.lambda.LambdaIteratorContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.H2Profile;

public class H2LambdaIteratorContractTest extends LambdaIteratorContractTest {
    @Override
    protected DataSourceProfile profile() {
        return H2Profile.INSTANCE;
    }
}
