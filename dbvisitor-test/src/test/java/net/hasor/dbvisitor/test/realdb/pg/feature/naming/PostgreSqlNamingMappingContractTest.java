package net.hasor.dbvisitor.test.realdb.pg.feature.naming;

import net.hasor.dbvisitor.test.contract.feature.naming.NamingMappingContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlNamingMappingContractTest extends NamingMappingContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
