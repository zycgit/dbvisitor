package net.hasor.dbvisitor.test.realdb.mysql.feature.keygen;

import net.hasor.dbvisitor.test.contract.feature.keygen.KeyGenerationContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlKeyGenerationContractTest extends KeyGenerationContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
