package net.hasor.dbvisitor.test.realdb.mysql.api.vector_query;

import net.hasor.dbvisitor.test.contract.api.vector_query.VectorKnnOrderingContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlVectorKnnOrderingContractTest extends VectorKnnOrderingContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
