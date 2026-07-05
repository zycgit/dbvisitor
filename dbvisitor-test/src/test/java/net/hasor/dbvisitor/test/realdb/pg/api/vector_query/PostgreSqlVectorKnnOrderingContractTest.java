package net.hasor.dbvisitor.test.realdb.pg.api.vector_query;

import net.hasor.dbvisitor.test.contract.api.vector_query.VectorKnnOrderingContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlVectorKnnOrderingContractTest extends VectorKnnOrderingContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
