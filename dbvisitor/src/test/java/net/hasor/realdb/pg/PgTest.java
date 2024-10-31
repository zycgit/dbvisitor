/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.realdb.pg;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Timestamp;

public class PgTest {

    @Test
    public void testOracleClob() throws SQLException {
        LambdaTemplate lambdaTemplate = new LambdaTemplate();

        ArchiveValidStatisticsResult result = new ArchiveValidStatisticsResult();
        result.setArchiveType("01");
        result.setHasName(1L);
        result.setHasNotName(1L);
        result.setUniqueId10(1L);
        result.setUniqueId15(1L);
        result.setUniqueId18(1L);
        result.setIllegalRowKey(1L);
        result.setUpdateTime(new Timestamp(System.currentTimeMillis()));

        BoundSql boundSql = lambdaTemplate.insertByEntity(ArchiveValidStatisticsResult.class).applyEntity(result).getBoundSql();
        System.out.println(boundSql.getSqlString());
    }

}
