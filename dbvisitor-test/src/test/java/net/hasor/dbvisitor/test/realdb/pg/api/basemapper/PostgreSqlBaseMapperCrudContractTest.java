package net.hasor.dbvisitor.test.realdb.pg.api.basemapper;

import net.hasor.dbvisitor.test.contract.api.basemapper.AbstractBaseMapperCrudContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlBaseMapperCrudContractTest extends AbstractBaseMapperCrudContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
