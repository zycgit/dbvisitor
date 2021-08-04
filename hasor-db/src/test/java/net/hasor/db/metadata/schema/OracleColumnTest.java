/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.db.metadata.schema;
import net.hasor.db.jdbc.core.JdbcTemplate;
import net.hasor.db.metadata.AbstractMetadataServiceSupplierTest;
import net.hasor.db.metadata.domain.oracle.OracleColumn;
import net.hasor.db.metadata.provider.OracleMetadataProvider;
import net.hasor.test.db.utils.DsUtils;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/***
 *
 * @version : 2021-3-22
 * @author 赵永春 (zyc@hasor.net)
 */
public class OracleColumnTest extends AbstractMetadataServiceSupplierTest<OracleMetadataProvider> {
    @Override
    protected Connection initConnection() throws SQLException {
        return DsUtils.localOracle();
    }

    @Override
    protected OracleMetadataProvider initRepository(Connection con) {
        return new OracleMetadataProvider(con);
    }

    @Override
    protected void beforeTest(JdbcTemplate jdbcTemplate, OracleMetadataProvider repository) throws SQLException, IOException {
        applySql("drop table oracle_column_null");
        applySql("create table oracle_column_null ( c_1 int primary key, c_2_null varchar2(24) null , c_2_notnull varchar2(24) not null)");
    }

    @Test
    public void getColumns_1() throws SQLException {
        List<OracleColumn> columnList = this.repository.getColumns("TESTER", "ORACLE_COLUMN_NULL");
        Map<String, OracleColumn> columnMap = columnList.stream().collect(Collectors.toMap(OracleColumn::getName, c -> c));
        //
        assert columnMap.containsKey("C_1");
        assert columnMap.containsKey("C_2_NULL");
        assert columnMap.containsKey("C_2_NOTNULL");
        //
        assert !columnMap.get("C_1").isNullable();
        assert columnMap.get("C_2_NULL").isNullable();
        assert !columnMap.get("C_2_NOTNULL").isNullable();
    }
}
