package net.hasor.dbvisitor.test.realdb.db2.feature.keygen;

import net.hasor.dbvisitor.test.contract.feature.keygen.AbstractKeyGenerationContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Db2Profile;

public class Db2KeyGenerationContractTest extends AbstractKeyGenerationContractTest {
    @Override
    protected DataSourceProfile profile() {
        return Db2Profile.INSTANCE;
    }
}
