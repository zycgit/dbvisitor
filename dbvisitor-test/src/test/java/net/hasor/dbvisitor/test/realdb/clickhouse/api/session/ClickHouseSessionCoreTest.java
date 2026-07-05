package net.hasor.dbvisitor.test.realdb.clickhouse.api.session;

import net.hasor.dbvisitor.test.contract.api.session.SessionCoreContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseSessionCoreTest extends SessionCoreContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
