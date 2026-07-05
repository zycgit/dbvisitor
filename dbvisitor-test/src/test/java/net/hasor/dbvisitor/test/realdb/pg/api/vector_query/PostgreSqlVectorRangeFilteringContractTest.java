package net.hasor.dbvisitor.test.realdb.pg.api.vector_query;

import net.hasor.dbvisitor.test.contract.api.vector_query.VectorRangeFilteringContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlVectorRangeFilteringContractTest extends VectorRangeFilteringContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
