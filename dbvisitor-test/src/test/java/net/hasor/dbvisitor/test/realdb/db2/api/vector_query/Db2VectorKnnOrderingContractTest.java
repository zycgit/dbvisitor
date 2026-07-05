package net.hasor.dbvisitor.test.realdb.db2.api.vector_query;

import net.hasor.dbvisitor.test.contract.api.vector_query.VectorKnnOrderingContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Db2Profile;

public class Db2VectorKnnOrderingContractTest extends VectorKnnOrderingContractTest {
    @Override
    protected DataSourceProfile profile() {
        return Db2Profile.INSTANCE;
    }
}
