package net.hasor.dbvisitor.test.realdb.oracle.api.vector_query;

import net.hasor.dbvisitor.test.contract.api.vector_query.VectorRangeFilteringContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleVectorRangeFilteringContractTest extends VectorRangeFilteringContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }
}
