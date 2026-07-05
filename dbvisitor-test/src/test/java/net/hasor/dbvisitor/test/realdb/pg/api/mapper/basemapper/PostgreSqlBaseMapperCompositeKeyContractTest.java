package net.hasor.dbvisitor.test.realdb.pg.api.mapper.basemapper;

import net.hasor.dbvisitor.test.contract.api.mapper.basemapper.BaseMapperCompositeKeyContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlBaseMapperCompositeKeyContractTest extends BaseMapperCompositeKeyContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
