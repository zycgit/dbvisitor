package net.hasor.dbvisitor.test.realdb.oracle.api.lambda;

import net.hasor.dbvisitor.test.contract.api.lambda.LambdaResultHandlingContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleLambdaResultHandlingContractTest extends LambdaResultHandlingContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }
}
