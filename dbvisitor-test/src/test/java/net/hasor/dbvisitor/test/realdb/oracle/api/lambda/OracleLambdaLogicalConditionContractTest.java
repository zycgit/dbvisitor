package net.hasor.dbvisitor.test.realdb.oracle.api.lambda;

import net.hasor.dbvisitor.test.contract.api.lambda.LambdaLogicalConditionContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleLambdaLogicalConditionContractTest extends LambdaLogicalConditionContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }
}
