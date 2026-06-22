package net.hasor.dbvisitor.test.realdb.mysql.api.session;

import net.hasor.dbvisitor.test.contract.api.session.AbstractSessionMapperContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlSessionMapperContractTest extends AbstractSessionMapperContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
