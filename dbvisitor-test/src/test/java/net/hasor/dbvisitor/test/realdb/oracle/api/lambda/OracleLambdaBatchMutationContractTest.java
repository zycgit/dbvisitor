package net.hasor.dbvisitor.test.realdb.oracle.api.lambda;

import net.hasor.dbvisitor.test.contract.api.lambda.LambdaBatchMutationContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleLambdaBatchMutationContractTest extends LambdaBatchMutationContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }
}
