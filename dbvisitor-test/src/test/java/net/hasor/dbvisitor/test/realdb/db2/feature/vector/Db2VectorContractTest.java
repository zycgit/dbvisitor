package net.hasor.dbvisitor.test.realdb.db2.feature.vector;

import net.hasor.dbvisitor.test.contract.feature.vector.AbstractVectorContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Db2Profile;

public class Db2VectorContractTest extends AbstractVectorContractTest {
    @Override
    protected DataSourceProfile profile() {
        return Db2Profile.INSTANCE;
    }
}
