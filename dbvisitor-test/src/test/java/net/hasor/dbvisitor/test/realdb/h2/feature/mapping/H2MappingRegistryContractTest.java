package net.hasor.dbvisitor.test.realdb.h2.feature.mapping;

import net.hasor.dbvisitor.test.contract.feature.mapping.MappingRegistryContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.H2Profile;

public class H2MappingRegistryContractTest extends MappingRegistryContractTest {
    @Override
    protected DataSourceProfile profile() {
        return H2Profile.INSTANCE;
    }
}
