/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.transaction;
import java.sql.Connection;
import java.sql.SQLException;
import net.hasor.cobble.function.EConsumer;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.transaction.support.TransactionHelper;
import net.hasor.test.utils.DefaultDs;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;
import static net.hasor.test.utils.TestUtils.INSERT_ARRAY;
import static net.hasor.test.utils.TestUtils.arrayForData2;

/**
 * 事务传播属性
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2015年11月10日
 */
public class AnnoPropagationMandatoryTest extends AbstractPropagationTest {

    @Transactional(propagation = Propagation.MANDATORY)
    public void commitTranTest(JdbcTemplate jdbc, EConsumer<JdbcTemplate, SQLException> callBack) throws SQLException {
        jdbc.executeUpdate(INSERT_ARRAY, arrayForData2());
        callBack.eAccept(jdbc);
    }

    @Test
    public void hasTx() throws Throwable {
        try (Connection conn = DsUtils.mysqlConn()) {
            initTable(conn);
        }

        try (DefaultDs ds = DsUtils.mysqlDs()) {
            TransactionManager txManager = TransactionHelper.txManager(ds);//
            JdbcTemplate jdbc = new JdbcTemplate(ds);

            TransactionStatus t1 = txManager.begin(Propagation.REQUIRED, Isolation.READ_COMMITTED);
            try {
                TransactionHelper.support(this, ds).commitTranTest(jdbc, p -> {
                    //
                });
                assert true;
            } catch (Exception e) {
                assert false;
            }
            txManager.commit(t1);
        }
    }

    @Test
    public void noTx() throws Throwable {
        try (Connection conn = DsUtils.mysqlConn()) {
            initTable(conn);
        }

        try (DefaultDs ds = DsUtils.mysqlDs()) {
            TransactionManager txManager = TransactionHelper.txManager(ds);//
            JdbcTemplate jdbc = new JdbcTemplate(ds);

            TransactionStatus t1 = txManager.begin(Propagation.NOT_SUPPORTED, Isolation.READ_COMMITTED);
            try {
                TransactionHelper.support(this, ds).commitTranTest(jdbc, p -> {
                    //
                });
                assert false;
            } catch (Exception e) {
                assert true;
            }
            txManager.commit(t1);
        }
    }
}
