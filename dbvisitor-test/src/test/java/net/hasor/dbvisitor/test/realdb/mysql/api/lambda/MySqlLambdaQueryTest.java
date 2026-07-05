package net.hasor.dbvisitor.test.realdb.mysql.api.lambda;

import net.hasor.dbvisitor.test.contract.api.lambda.LambdaQueryContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlLambdaQueryTest extends LambdaQueryContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
