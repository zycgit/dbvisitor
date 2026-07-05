package net.hasor.dbvisitor.test.realdb.oracle.api.mapper.xml;

import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperResultMapContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleXmlMapperResultMapContractTest extends XmlMapperResultMapContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }
}
