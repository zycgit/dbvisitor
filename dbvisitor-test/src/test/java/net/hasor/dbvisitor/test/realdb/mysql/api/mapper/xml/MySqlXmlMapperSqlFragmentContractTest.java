package net.hasor.dbvisitor.test.realdb.mysql.api.mapper.xml;

import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperSqlFragmentContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlXmlMapperSqlFragmentContractTest extends XmlMapperSqlFragmentContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
