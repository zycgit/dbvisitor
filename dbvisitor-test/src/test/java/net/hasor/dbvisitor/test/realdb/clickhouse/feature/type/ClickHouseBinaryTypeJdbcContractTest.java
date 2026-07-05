package net.hasor.dbvisitor.test.realdb.clickhouse.feature.type;

import net.hasor.dbvisitor.test.contract.feature.type.BinaryTypeJdbcContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseBinaryTypeJdbcContractTest extends BinaryTypeJdbcContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
