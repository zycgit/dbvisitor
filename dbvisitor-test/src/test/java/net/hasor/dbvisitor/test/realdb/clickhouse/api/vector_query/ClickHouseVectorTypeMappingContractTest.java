package net.hasor.dbvisitor.test.realdb.clickhouse.api.vector_query;

import net.hasor.dbvisitor.test.contract.api.vector_query.VectorTypeMappingContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseVectorTypeMappingContractTest extends VectorTypeMappingContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
