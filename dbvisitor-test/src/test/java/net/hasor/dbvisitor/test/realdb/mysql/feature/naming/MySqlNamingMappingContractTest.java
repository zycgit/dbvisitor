package net.hasor.dbvisitor.test.realdb.mysql.feature.naming;

import net.hasor.dbvisitor.test.contract.feature.naming.NamingMappingContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlNamingMappingContractTest extends NamingMappingContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
