package net.hasor.dbvisitor.test.realdb.oracle.feature.naming;

import net.hasor.dbvisitor.test.contract.feature.naming.AbstractNamingMappingContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleNamingMappingContractTest extends AbstractNamingMappingContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }
}
