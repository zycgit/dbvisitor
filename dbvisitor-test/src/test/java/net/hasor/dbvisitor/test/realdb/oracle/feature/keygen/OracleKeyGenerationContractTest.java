package net.hasor.dbvisitor.test.realdb.oracle.feature.keygen;

import net.hasor.dbvisitor.test.contract.feature.keygen.AbstractKeyGenerationContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleKeyGenerationContractTest extends AbstractKeyGenerationContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }
}
