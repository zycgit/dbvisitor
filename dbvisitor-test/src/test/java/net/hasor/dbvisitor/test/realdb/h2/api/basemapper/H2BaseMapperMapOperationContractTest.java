package net.hasor.dbvisitor.test.realdb.h2.api.basemapper;

import net.hasor.dbvisitor.test.contract.api.basemapper.AbstractBaseMapperMapOperationContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.H2Profile;

public class H2BaseMapperMapOperationContractTest extends AbstractBaseMapperMapOperationContractTest {
    @Override
    protected DataSourceProfile profile() {
        return H2Profile.INSTANCE;
    }
}
