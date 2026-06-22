package net.hasor.dbvisitor.test.realdb.pg.api.session;

import net.hasor.dbvisitor.test.contract.api.session.AbstractSessionMapperContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlSessionMapperContractTest extends AbstractSessionMapperContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
