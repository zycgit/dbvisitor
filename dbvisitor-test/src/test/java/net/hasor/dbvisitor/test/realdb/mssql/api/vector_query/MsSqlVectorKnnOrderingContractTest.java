package net.hasor.dbvisitor.test.realdb.mssql.api.vector_query;

import net.hasor.dbvisitor.test.contract.api.vector_query.VectorKnnOrderingContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;

public class MsSqlVectorKnnOrderingContractTest extends VectorKnnOrderingContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }
}
