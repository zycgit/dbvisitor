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
package net.hasor.dbvisitor.transaction;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.transaction.support.LocalTransactionManager;
import net.hasor.test.utils.DefaultDs;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;

import static net.hasor.test.utils.TestUtils.*;

/**
 * NESTED
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2015年11月10日
 */
public class PropagationNestedTest extends AbstractPropagationTest {
    @Test
    public void tran_rollback_test_1() throws Throwable {
        try (DefaultDs dataSource = DsUtils.mysqlDs();//
             LocalTransactionManager tranManager = new LocalTransactionManager(dataSource);//
             Connection conn = DsUtils.mysqlConn()) {
            initTable(conn);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            /* T1 */
            TransactionStatus tran1 = tranManager.begin(Propagation.NESTED, Isolation.REPEATABLE_READ);
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData1());
            assert selectCount(dataSource) == 1;
            assert selectCount(conn) == 0;

            /* T2 */
            TransactionStatus tran2 = tranManager.begin(Propagation.NESTED, Isolation.REPEATABLE_READ);
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData2());
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData3());
            assert selectCount(dataSource) == 3;
            assert selectCount(conn) == 0;
            assert !tran1.isCompleted() && !tran2.isCompleted();

            tranManager.rollBack(tran2);
            assert selectCount(dataSource) == 1;
            assert selectCount(conn) == 0;
            assert !tran1.isCompleted() && tran2.isCompleted();

            tranManager.rollBack(tran1);
            assert selectCount(dataSource) == 0;
            assert selectCount(conn) == 0;
            assert tran1.isCompleted() && tran2.isCompleted();
        }
    }

    @Test
    public void tran_rollback_test_2() throws Throwable {
        try (DefaultDs dataSource = DsUtils.mysqlDs();//
             LocalTransactionManager tranManager = new LocalTransactionManager(dataSource);//
             Connection conn = DsUtils.mysqlConn()) {
            initTable(conn);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            /* T1 */
            TransactionStatus tran1 = tranManager.begin(Propagation.NESTED, Isolation.REPEATABLE_READ);
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData1());
            assert selectCount(dataSource) == 1;
            assert selectCount(conn) == 0;

            /* T2 */
            TransactionStatus tran2 = tranManager.begin(Propagation.NESTED, Isolation.REPEATABLE_READ);
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData2());
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData3());
            assert selectCount(dataSource) == 3;
            assert selectCount(conn) == 0;
            assert !tran1.isCompleted();
            assert !tran2.isCompleted();

            tranManager.rollBack(tran1);
            assert selectCount(dataSource) == 0;
            assert selectCount(conn) == 0;
            assert tran1.isCompleted();
            assert tran2.isCompleted();
        }
    }

    @Test
    public void tran_commit_test_1() throws Throwable {
        try (DefaultDs dataSource = DsUtils.mysqlDs();//
             LocalTransactionManager tranManager = new LocalTransactionManager(dataSource);//
             Connection conn = DsUtils.mysqlConn()) {
            initTable(conn);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            /* T1 */
            TransactionStatus tran1 = tranManager.begin(Propagation.NESTED, Isolation.REPEATABLE_READ);
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData1());
            assert selectCount(dataSource) == 1;
            assert selectCount(conn) == 0;

            /* T2 */
            TransactionStatus tran2 = tranManager.begin(Propagation.NESTED, Isolation.REPEATABLE_READ);
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData2());
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData3());
            assert selectCount(dataSource) == 3;
            assert selectCount(conn) == 0;
            assert !tran1.isCompleted();
            assert !tran2.isCompleted();

            tranManager.commit(tran2);
            assert selectCount(dataSource) == 3;
            assert selectCount(conn) == 0;
            assert !tran1.isCompleted();
            assert tran2.isCompleted();

            tranManager.commit(tran1);
            assert selectCount(dataSource) == 3;
            assert selectCount(conn) == 3;
            assert tran1.isCompleted();
            assert tran2.isCompleted();
        }
    }

    @Test
    public void tran_commit_test_2() throws Throwable {
        try (DefaultDs dataSource = DsUtils.mysqlDs();//
             LocalTransactionManager tranManager = new LocalTransactionManager(dataSource);//
             Connection conn = DsUtils.mysqlConn()) {
            initTable(conn);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            /* T1 */
            TransactionStatus tran1 = tranManager.begin(Propagation.NESTED, Isolation.REPEATABLE_READ);
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData1());
            assert selectCount(dataSource) == 1;
            assert selectCount(conn) == 0;

            /* T2 */
            TransactionStatus tran2 = tranManager.begin(Propagation.NESTED, Isolation.REPEATABLE_READ);
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData2());
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData3());
            assert selectCount(dataSource) == 3;
            assert selectCount(conn) == 0;
            assert !tran1.isCompleted();
            assert !tran2.isCompleted();

            tranManager.commit(tran1);
            assert selectCount(dataSource) == 3;
            assert selectCount(conn) == 3;
            assert tran1.isCompleted();
            assert tran2.isCompleted();
        }
    }

    @Test
    public void tran_commit_and_rollback_test_1() throws Throwable {
        try (DefaultDs dataSource = DsUtils.mysqlDs();//
             LocalTransactionManager tranManager = new LocalTransactionManager(dataSource);//
             Connection conn = DsUtils.mysqlConn()) {
            initTable(conn);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            /* T1 */
            TransactionStatus tran1 = tranManager.begin(Propagation.NESTED, Isolation.REPEATABLE_READ);
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData1());
            assert selectCount(dataSource) == 1;
            assert selectCount(conn) == 0;

            /* T2 */
            TransactionStatus tran2 = tranManager.begin(Propagation.NESTED, Isolation.REPEATABLE_READ);
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData2());
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData3());
            assert selectCount(dataSource) == 3;
            assert selectCount(conn) == 0;
            assert !tran1.isCompleted() && !tran2.isCompleted();

            tranManager.commit(tran2);
            assert selectCount(dataSource) == 3;
            assert selectCount(conn) == 0;
            assert !tran1.isCompleted() && tran2.isCompleted();

            tranManager.rollBack(tran1);
            assert selectCount(dataSource) == 0;
            assert selectCount(conn) == 0;
            assert tran1.isCompleted() && tran2.isCompleted();
        }
    }

    @Test
    public void tran_rollback_and_commit_test_1() throws Throwable {
        try (DefaultDs dataSource = DsUtils.mysqlDs();//
             LocalTransactionManager tranManager = new LocalTransactionManager(dataSource);//
             Connection conn = DsUtils.mysqlConn()) {
            initTable(conn);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            /* T1 */
            TransactionStatus tran1 = tranManager.begin(Propagation.NESTED, Isolation.REPEATABLE_READ);
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData1());
            assert selectCount(dataSource) == 1;
            assert selectCount(conn) == 0;

            /* T2 */
            TransactionStatus tran2 = tranManager.begin(Propagation.NESTED, Isolation.REPEATABLE_READ);
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData2());
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData3());
            assert selectCount(dataSource) == 3;
            assert selectCount(conn) == 0;
            assert !tran1.isCompleted() && !tran2.isCompleted();

            tranManager.rollBack(tran2);
            assert selectCount(dataSource) == 1;
            assert selectCount(conn) == 0;
            assert !tran1.isCompleted() && tran2.isCompleted();

            tranManager.commit(tran1);
            assert selectCount(dataSource) == 1;
            assert selectCount(conn) == 1;
            assert tran1.isCompleted() && tran2.isCompleted();
        }
    }
}