package net.hasor.dbvisitor.test.realdb.pg.api.vector_query;

import net.hasor.dbvisitor.test.contract.api.vector_query.VectorTypeMappingContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlVectorTypeMappingContractTest extends VectorTypeMappingContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
