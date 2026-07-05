package net.hasor.dbvisitor.test.realdb.mysql.api.mapper.basemapper;

import net.hasor.dbvisitor.test.contract.api.mapper.basemapper.BaseMapperStatementContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlBaseMapperStatementContractTest extends BaseMapperStatementContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
