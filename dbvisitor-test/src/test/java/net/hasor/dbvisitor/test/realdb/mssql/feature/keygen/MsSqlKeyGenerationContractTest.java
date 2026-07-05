package net.hasor.dbvisitor.test.realdb.mssql.feature.keygen;

import net.hasor.dbvisitor.test.contract.feature.keygen.KeyGenerationContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;

public class MsSqlKeyGenerationContractTest extends KeyGenerationContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }
}
