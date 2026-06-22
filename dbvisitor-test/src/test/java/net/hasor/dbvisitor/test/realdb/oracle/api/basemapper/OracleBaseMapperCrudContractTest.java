package net.hasor.dbvisitor.test.realdb.oracle.api.basemapper;

import net.hasor.dbvisitor.test.contract.api.basemapper.AbstractBaseMapperCrudContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleBaseMapperCrudContractTest extends AbstractBaseMapperCrudContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }
}
