package net.hasor.dbvisitor.test.realdb.oracle.feature.vector;

import net.hasor.dbvisitor.test.contract.feature.vector.AbstractVectorContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleVectorContractTest extends AbstractVectorContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }
}
