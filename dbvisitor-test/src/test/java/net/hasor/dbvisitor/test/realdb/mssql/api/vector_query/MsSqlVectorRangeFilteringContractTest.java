package net.hasor.dbvisitor.test.realdb.mssql.api.vector_query;

import net.hasor.dbvisitor.test.contract.api.vector_query.VectorRangeFilteringContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;

public class MsSqlVectorRangeFilteringContractTest extends VectorRangeFilteringContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }
}
