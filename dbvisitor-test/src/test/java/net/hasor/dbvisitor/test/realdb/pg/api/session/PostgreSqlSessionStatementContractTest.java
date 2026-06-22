package net.hasor.dbvisitor.test.realdb.pg.api.session;

import net.hasor.dbvisitor.test.contract.api.session.AbstractSessionStatementContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlSessionStatementContractTest extends AbstractSessionStatementContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
