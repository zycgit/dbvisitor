package net.hasor.dbvisitor.test.realdb.h2.api.vector_query;

import net.hasor.dbvisitor.test.contract.api.vector_query.VectorCombinedQueryContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.H2Profile;

public class H2VectorCombinedQueryContractTest extends VectorCombinedQueryContractTest {
    @Override
    protected DataSourceProfile profile() {
        return H2Profile.INSTANCE;
    }
}
