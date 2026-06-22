package net.hasor.dbvisitor.test.realdb.pg.feature.keygen;

import net.hasor.dbvisitor.test.contract.feature.keygen.AbstractKeyGenerationContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlKeyGenerationContractTest extends AbstractKeyGenerationContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
