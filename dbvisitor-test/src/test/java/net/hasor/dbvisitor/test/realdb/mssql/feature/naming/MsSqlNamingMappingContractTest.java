package net.hasor.dbvisitor.test.realdb.mssql.feature.naming;

import net.hasor.dbvisitor.test.contract.feature.naming.AbstractNamingMappingContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;

public class MsSqlNamingMappingContractTest extends AbstractNamingMappingContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }
}
