package net.hasor.dbvisitor.test.realdb.mysql.feature.naming;

import net.hasor.dbvisitor.test.contract.feature.naming.AbstractNamingMappingContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlNamingMappingContractTest extends AbstractNamingMappingContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
