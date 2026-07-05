package net.hasor.dbvisitor.test.realdb.oracle.api.lambda;

import net.hasor.dbvisitor.test.contract.api.lambda.LambdaQueryContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleLambdaQueryTest extends LambdaQueryContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }
}
