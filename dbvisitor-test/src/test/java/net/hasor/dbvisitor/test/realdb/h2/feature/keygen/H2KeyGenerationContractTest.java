package net.hasor.dbvisitor.test.realdb.h2.feature.keygen;

import net.hasor.dbvisitor.test.contract.feature.keygen.AbstractKeyGenerationContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.H2Profile;

public class H2KeyGenerationContractTest extends AbstractKeyGenerationContractTest {
    @Override
    protected DataSourceProfile profile() {
        return H2Profile.INSTANCE;
    }
}
