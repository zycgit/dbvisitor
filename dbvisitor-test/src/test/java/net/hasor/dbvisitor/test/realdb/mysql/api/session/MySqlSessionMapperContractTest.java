package net.hasor.dbvisitor.test.realdb.mysql.api.session;

import net.hasor.dbvisitor.test.contract.api.session.SessionMapperContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlSessionMapperContractTest extends SessionMapperContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
