package net.hasor.dbvisitor.test.realdb.db2.api.vector_query;

import net.hasor.dbvisitor.test.contract.api.vector_query.VectorRangeFilteringContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Db2Profile;

public class Db2VectorRangeFilteringContractTest extends VectorRangeFilteringContractTest {
    @Override
    protected DataSourceProfile profile() {
        return Db2Profile.INSTANCE;
    }
}
