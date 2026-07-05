package net.hasor.dbvisitor.test.realdb.mssql.api.vector_query;

import net.hasor.dbvisitor.test.contract.api.vector_query.VectorCombinedQueryContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;

public class MsSqlVectorCombinedQueryContractTest extends VectorCombinedQueryContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }
}
