package net.hasor.dbvisitor.test.realdb.clickhouse.api.session;

import net.hasor.dbvisitor.test.contract.api.session.SessionStatementContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseSessionStatementContractTest extends SessionStatementContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
