package net.hasor.dbvisitor.test.realdb.oracle.api.vector_query;

import net.hasor.dbvisitor.test.contract.api.vector_query.VectorKnnOrderingContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleVectorKnnOrderingContractTest extends VectorKnnOrderingContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }
}
