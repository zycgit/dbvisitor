package net.hasor.dbvisitor.test.realdb.clickhouse.api.session;

import net.hasor.dbvisitor.test.contract.api.session.AbstractSessionMapperContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseSessionMapperContractTest extends AbstractSessionMapperContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
