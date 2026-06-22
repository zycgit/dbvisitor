package net.hasor.dbvisitor.test.realdb.mysql.feature.vector;

import net.hasor.dbvisitor.test.contract.feature.vector.AbstractVectorContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlVectorContractTest extends AbstractVectorContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
