package net.hasor.dbvisitor.test.realdb.oracle.api.mapper.basemapper;

import net.hasor.dbvisitor.test.contract.api.mapper.basemapper.BaseMapperCrudContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleBaseMapperCrudContractTest extends BaseMapperCrudContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }
}
