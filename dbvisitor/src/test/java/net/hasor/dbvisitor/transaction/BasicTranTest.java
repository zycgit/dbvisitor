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
import java.sql.Connection;
import java.sql.SQLException;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.transaction.support.LocalTransactionManager;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.utils.DefaultDs;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;
import static net.hasor.test.utils.TestUtils.INSERT_ARRAY;
import static net.hasor.test.utils.TestUtils.arrayForData4;

/**
 * 基础 事务控制
 * @author 赵永春 (zyc@hasor.net)
 * @version 2015-11-10
 */
public class BasicTranTest extends AbstractDbTest {
    @Test
    public void tran_basic_1() throws Throwable {
        try (DefaultDs dataSource = DsUtils.h2Ds()) {
            TransactionManager transManager = new LocalTransactionManager(dataSource);
            assert !transManager.hasTransaction();

            TransactionStatus tran = transManager.begin(Propagation.REQUIRED);
            assert transManager.hasTransaction();

            transManager.commit(tran);
            assert !transManager.hasTransaction();
        }
    }

    @Test
    public void tran_basic_2() throws Throwable {
        try (DefaultDs dataSource = DsUtils.h2Ds()) {
            ConnectionHolder holder = DataSourceUtils.getHolder(dataSource);

            TransactionManager transManager = new LocalTransactionManager(dataSource);
            assert holder.getRefCount() == 0;

            TransactionStatus tran1 = transManager.begin(Propagation.REQUIRED);
            assert holder.getRefCount() == 1;

            TransactionStatus tran2 = transManager.begin(Propagation.REQUIRED);
            assert holder.getRefCount() == 2;

            transManager.commit(tran2);
            assert holder.getRefCount() == 1;

            transManager.commit(tran1);
            assert holder.getRefCount() == 0;
        }
    }

    @Test
    public void tran_basic_3() throws Throwable {
        try (DefaultDs dataSource = DsUtils.h2Ds()) {

            TransactionManager transManager = new LocalTransactionManager(dataSource);
            ConnectionHolder holder1 = DataSourceUtils.getHolder(dataSource);
            ConnectionHolder holderTmp = null;
            assert holder1.getRefCount() == 0;

            TransactionStatus tran1 = transManager.begin(Propagation.REQUIRED);
            holderTmp = DataSourceUtils.getHolder(dataSource);
            assert holder1.getRefCount() == 1;
            assert holder1 == holderTmp;

            TransactionStatus tran2 = transManager.begin(Propagation.REQUIRES_NEW);
            holderTmp = DataSourceUtils.getHolder(dataSource);
            assert holder1.getRefCount() == 2;
            assert holder1 != holderTmp;
            assert holderTmp.getRefCount() == 1;

            transManager.commit(tran2);

            holderTmp = DataSourceUtils.getHolder(dataSource);
            assert holder1.getRefCount() == 1;
            assert holder1 == holderTmp;

            transManager.commit(tran1);
            assert holder1.getRefCount() == 0;
        }
    }

    @Test
    public void tran_basic_4() throws Throwable {
        try (DefaultDs dataSource = DsUtils.mysqlDs()) {
            TransactionManager transManager = new LocalTransactionManager(dataSource);

            TransactionStatus tran1 = transManager.begin(Propagation.REQUIRED);
            ConnectionHolder holder1 = DataSourceUtils.getHolder(dataSource);
            assert holder1.getRefCount() == 1;
            String conn1_0_id = new JdbcTemplate(dataSource).queryForString("select connection_id();");
            String conn1_1_id = new JdbcTemplate(dataSource).queryForString("select connection_id();");

            assert conn1_0_id.equals(conn1_1_id);

            TransactionStatus tran2 = transManager.begin(Propagation.REQUIRES_NEW);
            ConnectionHolder holder2 = DataSourceUtils.getHolder(dataSource);
            assert holder2.getRefCount() == 1;
            String conn2_0_id = new JdbcTemplate(dataSource).queryForString("select connection_id();");
            String conn2_1_id = new JdbcTemplate(dataSource).queryForString("select connection_id();");

            assert conn2_0_id.equals(conn2_1_id);

            assert !conn1_0_id.equals(conn2_0_id);

            transManager.commit(tran2);
            transManager.commit(tran1);

            ConnectionHolder holder = DataSourceUtils.getHolder(dataSource);
            assert holder.getRefCount() == 0;
            holder.released();
        }
    }

    @Test
    public void tran_basic_5() throws Throwable {
        try (DefaultDs dataSource = DsUtils.mysqlDs()) {
            TransactionManager transManager = new LocalTransactionManager(dataSource);
            ConnectionHolder holder = DataSourceUtils.getHolder(dataSource);

            TransactionStatus tran1 = transManager.begin(Propagation.REQUIRED);
            TransactionStatus tran2 = transManager.begin(Propagation.REQUIRED);
            TransactionStatus tran3 = transManager.begin(Propagation.REQUIRED);

            assert !tran1.isCompleted();
            assert !tran2.isCompleted();
            assert !tran3.isCompleted();
            assert holder.getRefCount() == 3;

            transManager.commit(tran1);

            assert tran1.isCompleted();
            assert tran2.isCompleted();
            assert tran3.isCompleted();
            assert holder.getRefCount() == 0;
        }
    }

    @Test
    public void tran_basic_6() throws Throwable {
        try (DefaultDs dataSource = DsUtils.mysqlDs();//
             TransactionManager transManager = new LocalTransactionManager(dataSource);) {

            TransactionStatus tran1 = transManager.begin(Propagation.REQUIRED);
            TransactionStatus tran2 = transManager.begin(Propagation.REQUIRED);
            TransactionStatus tran3 = transManager.begin(Propagation.REQUIRED);

            assert transManager.isTopTransaction(tran3);
            assert !transManager.isTopTransaction(tran2);
            assert !transManager.isTopTransaction(tran1);

            assert !transManager.isTopTransaction(null);
        }
    }

    @Test
    public void tran_basic_7() throws Throwable {
        try (DefaultDs dataSource = DsUtils.mysqlDs()) {
            LocalTransactionManager transManager = new LocalTransactionManager(dataSource);
            ConnectionHolder holder = DataSourceUtils.getHolder(dataSource);

            TransactionStatus tran1 = transManager.begin(Propagation.REQUIRED);
            TransactionStatus tran2 = transManager.begin(Propagation.REQUIRED);
            TransactionStatus tran3 = transManager.begin(Propagation.REQUIRED);

            assert transManager.hasTransaction();
            assert holder.getRefCount() == 3;

            transManager.close();
            assert !transManager.hasTransaction();
            assert tran1.isCompleted();
            assert tran2.isCompleted();
            assert tran3.isCompleted();
            assert holder.getRefCount() == 0;
        }
    }

    @Test
    public void tran_required_test_1() throws Throwable {
        try (DefaultDs dataSource = DsUtils.mysqlDs();//
             Connection conn = DsUtils.mysqlConn()) {

            conn.setTransactionIsolation(Isolation.REPEATABLE_READ.getValue());
            JdbcTemplate initJdbc = new JdbcTemplate(conn);
            initJdbc.execute("drop table if exists user_info;");
            initJdbc.loadSQL("dbvisitor_coverage/user_info_for_mysql.sql");

            LocalTransactionManager tranManager = new LocalTransactionManager(dataSource);
            TransactionStatus tran1 = tranManager.begin(Propagation.REQUIRED, Isolation.REPEATABLE_READ);

            assert new JdbcTemplate(dataSource).queryForInt("select count(*) from user_info") == 0;
            new JdbcTemplate(dataSource).executeUpdate(INSERT_ARRAY, arrayForData4());
            assert new JdbcTemplate(dataSource).queryForInt("select count(*) from user_info") == 1;

            assert new JdbcTemplate(conn).queryForInt("select count(*) from user_info") == 0;

            tranManager.rollBack(tran1);
            assert new JdbcTemplate(dataSource).queryForInt("select count(*) from user_info") == 0;
        }
    }

    @Test
    public void tran_never_test_1() throws Throwable {
        try (DefaultDs dataSource = DsUtils.mysqlDs();//
             LocalTransactionManager transManager = new LocalTransactionManager(dataSource)) {

            transManager.begin(Propagation.REQUIRED);
            assert DataSourceUtils.getHolder(dataSource).hasTransaction();

            try {
                transManager.begin(Propagation.NEVER);
                assert false;
            } catch (SQLException e) {
                assert e.getMessage().equals("existing transaction found for transaction marked with propagation 'never'");
            }

            ConnectionHolder holder = DataSourceUtils.getHolder(dataSource);
            assert holder.hasTransaction();
        }
    }

    @Test
    public void tran_never_test_2() throws Throwable {
        try (DefaultDs dataSource = DsUtils.mysqlDs();//
             LocalTransactionManager transManager = new LocalTransactionManager(dataSource)) {

            transManager.begin(Propagation.REQUIRED);
            assert DataSourceUtils.getHolder(dataSource).hasTransaction();

            transManager.begin(Propagation.NOT_SUPPORTED);
            assert !DataSourceUtils.getHolder(dataSource).hasTransaction();

            transManager.begin(Propagation.NEVER);
            assert !DataSourceUtils.getHolder(dataSource).hasTransaction();
        }
    }

    @Test
    public void tran_mandatory_test_1() throws Throwable {
        try (DefaultDs dataSource = DsUtils.mysqlDs();//
             LocalTransactionManager transManager = new LocalTransactionManager(dataSource)) {

            transManager.begin(Propagation.REQUIRED);
            transManager.begin(Propagation.MANDATORY);

            assert DataSourceUtils.getHolder(dataSource).hasTransaction();
        }
    }

    @Test
    public void tran_mandatory_test_2() throws Throwable {
        try (DefaultDs dataSource = DsUtils.mysqlDs();//
             LocalTransactionManager transManager = new LocalTransactionManager(dataSource)) {

            transManager.begin(Propagation.REQUIRED);
            assert DataSourceUtils.getHolder(dataSource).hasTransaction();

            transManager.begin(Propagation.NOT_SUPPORTED);
            assert !DataSourceUtils.getHolder(dataSource).hasTransaction();

            try {
                transManager.begin(Propagation.MANDATORY);
                assert false;
            } catch (SQLException e) {
                assert e.getMessage().equals("no existing transaction found for transaction marked with propagation 'mandatory'");
            }
        }
    }

    @Test
    public void tran_mandatory_test_3() throws Throwable {
        try (DefaultDs dataSource = DsUtils.mysqlDs();//
             LocalTransactionManager transManager = new LocalTransactionManager(dataSource)) {

            try {
                transManager.begin(Propagation.MANDATORY);
                assert false;
            } catch (SQLException e) {
                assert e.getMessage().equals("no existing transaction found for transaction marked with propagation 'mandatory'");
            }
        }
    }

    @Test
    public void tran_supports_test_1() throws Throwable {
        try (DefaultDs dataSource = DsUtils.mysqlDs();//
             LocalTransactionManager transManager = new LocalTransactionManager(dataSource)) {

            assert !DataSourceUtils.getHolder(dataSource).hasTransaction();

            transManager.begin(Propagation.SUPPORTS);
            assert !DataSourceUtils.getHolder(dataSource).hasTransaction();

            transManager.begin(Propagation.NESTED);
            assert DataSourceUtils.getHolder(dataSource).hasTransaction();

            transManager.commit(transManager.lastTransaction());
            assert !DataSourceUtils.getHolder(dataSource).hasTransaction();
        }
    }

    @Test
    public void tran_supports_test_2() throws Throwable {
        try (DefaultDs dataSource = DsUtils.mysqlDs();//
             LocalTransactionManager transManager = new LocalTransactionManager(dataSource)) {

            assert !DataSourceUtils.getHolder(dataSource).hasTransaction();

            transManager.begin(Propagation.NESTED);
            assert DataSourceUtils.getHolder(dataSource).hasTransaction();

            transManager.begin(Propagation.SUPPORTS);
            assert DataSourceUtils.getHolder(dataSource).hasTransaction();

            transManager.commit(transManager.lastTransaction());
            assert DataSourceUtils.getHolder(dataSource).hasTransaction();
        }
    }

    @Test
    public void tran_not_supported_test_1() throws Throwable {
        try (DefaultDs dataSource = DsUtils.mysqlDs();//
             LocalTransactionManager transManager = new LocalTransactionManager(dataSource)) {

            assert !DataSourceUtils.getHolder(dataSource).hasTransaction();

            transManager.begin(Propagation.NESTED);
            assert DataSourceUtils.getHolder(dataSource).hasTransaction();

            transManager.begin(Propagation.NOT_SUPPORTED);
            assert !DataSourceUtils.getHolder(dataSource).hasTransaction();

            transManager.commit(transManager.lastTransaction());
            assert DataSourceUtils.getHolder(dataSource).hasTransaction();
        }
    }

    @Test
    public void template() throws Throwable {
        try (DefaultDs dataSource = DsUtils.mysqlDs();//
             LocalTransactionManager transManager = new LocalTransactionManager(dataSource)) {
            TransactionTemplate templateProvider = new TransactionTemplateManager(transManager);

            ConnectionHolder holder = DataSourceUtils.getHolder(dataSource);
            assert !holder.hasTransaction();
            templateProvider.execute((TransactionCallbackWithoutResult) tran1 -> {
                assert DataSourceUtils.getHolder(dataSource).hasTransaction();

                templateProvider.execute((TransactionCallbackWithoutResult) tran2 -> {
                    assert !DataSourceUtils.getHolder(dataSource).hasTransaction();

                }, Propagation.NOT_SUPPORTED);
            });

            assert !holder.hasTransaction();
            holder.released();
        }
    }
}