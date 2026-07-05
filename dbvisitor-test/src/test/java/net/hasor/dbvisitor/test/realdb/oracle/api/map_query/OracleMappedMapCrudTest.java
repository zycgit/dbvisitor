package net.hasor.dbvisitor.test.realdb.oracle.api.map_query;

import net.hasor.dbvisitor.test.contract.api.map_query.MappedMapCrudContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleMappedMapCrudTest extends MappedMapCrudContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }
}
