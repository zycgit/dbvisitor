package net.hasor.dbvisitor.test.realdb.mssql.feature.mapping;

import net.hasor.dbvisitor.test.contract.feature.mapping.MappingRegistryContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;

public class MsSqlMappingRegistryContractTest extends MappingRegistryContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }
}
