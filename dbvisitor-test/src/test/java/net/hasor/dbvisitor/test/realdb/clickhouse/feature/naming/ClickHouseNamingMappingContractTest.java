package net.hasor.dbvisitor.test.realdb.clickhouse.feature.naming;

import net.hasor.dbvisitor.test.contract.feature.naming.AbstractNamingMappingContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseNamingMappingContractTest extends AbstractNamingMappingContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
