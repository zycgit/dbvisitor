package net.hasor.dbvisitor.test.realdb.clickhouse.feature.schema;

import net.hasor.dbvisitor.test.contract.feature.schema.StandardSchemaContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseStandardSchemaTest extends StandardSchemaContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
