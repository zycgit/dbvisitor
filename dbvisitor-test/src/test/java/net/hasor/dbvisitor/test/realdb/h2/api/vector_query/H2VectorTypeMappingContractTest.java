package net.hasor.dbvisitor.test.realdb.h2.api.vector_query;

import net.hasor.dbvisitor.test.contract.api.vector_query.VectorTypeMappingContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.H2Profile;

public class H2VectorTypeMappingContractTest extends VectorTypeMappingContractTest {
    @Override
    protected DataSourceProfile profile() {
        return H2Profile.INSTANCE;
    }
}
