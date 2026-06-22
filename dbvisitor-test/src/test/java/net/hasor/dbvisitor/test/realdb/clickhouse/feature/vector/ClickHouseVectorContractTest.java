package net.hasor.dbvisitor.test.realdb.clickhouse.feature.vector;

import net.hasor.dbvisitor.test.contract.feature.vector.AbstractVectorContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseVectorContractTest extends AbstractVectorContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
