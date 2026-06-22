package net.hasor.dbvisitor.test.realdb.h2.feature.vector;

import net.hasor.dbvisitor.test.contract.feature.vector.AbstractVectorContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.H2Profile;

public class H2VectorContractTest extends AbstractVectorContractTest {
    @Override
    protected DataSourceProfile profile() {
        return H2Profile.INSTANCE;
    }
}
