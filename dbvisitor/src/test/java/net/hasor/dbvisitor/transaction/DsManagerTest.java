package net.hasor.dbvisitor.transaction;
import net.hasor.dbvisitor.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.transaction.support.LocalTransactionManager;
import net.hasor.test.utils.DefaultDs;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.Statement;

public class DsManagerTest {
    @Test
    public void manager_test_1() throws Throwable {
        try (DefaultDs dataSource = DsUtils.h2Ds()) {
            Connection conn1 = DataSourceUtils.getConnection(dataSource);
            Connection conn2 = DataSourceUtils.getConnection(dataSource);

            assert conn1 != conn2;
            assert ((ConnectionProxy) conn1).getTargetConnection() == ((ConnectionProxy) conn2).getTargetConnection();
        }
    }

    @Test
    public void manager_test_2() throws Throwable {
        try (DefaultDs dataSource = DsUtils.h2Ds()) {
            try (Connection conn = DataSourceUtils.getConnection(dataSource)) {
                int result = new JdbcTemplate(conn).queryForInt("select 123");
                assert result == 123;
            }

            try (Connection conn = DataSourceUtils.getConnection(dataSource)) {
                int result = new JdbcTemplate(conn).queryForInt("select 123");
                assert result == 123;
            }
        }
    }

    @Test
    public void manager_test_3() throws Throwable {
        try (DefaultDs dataSource = DsUtils.mysqlDs()) {
            Connection conn1 = DataSourceUtils.getConnection(dataSource);
            Connection conn2 = DataSourceUtils.getConnection(dataSource);

            String conn1_id = new JdbcTemplate(conn1).queryForString("select connection_id();");
            String conn2_id = new JdbcTemplate(conn2).queryForString("select connection_id()");

            assert conn1_id.equals(conn2_id);
        }
    }

    @Test
    public void manager_test_4() throws Throwable {
        try (DefaultDs dataSource = DsUtils.h2Ds()) {
            Connection conn1 = DataSourceUtils.getConnection(dataSource);
            Connection conn2 = DataSourceUtils.getConnection(dataSource);

            conn1.close();

            try {
                Statement statement = conn1.createStatement();
                assert false;
            } catch (Exception e) {
                assert e.getMessage().equals("connection is close.");
            }
            try {
                conn1.close();
            } catch (Exception e) {
                assert e.getMessage().equals("connection is close.");
            }

            int result = new JdbcTemplate(conn2).queryForInt("select 123");
            assert result == 123;
        }
    }

    @Test
    public void manager_test_5() throws Throwable {
        try (DefaultDs dataSource = DsUtils.h2Ds()) {
            Connection conn = DataSourceUtils.getConnection(dataSource);

            assert conn instanceof ConnectionProxy;

            assert ((ConnectionProxy) conn).getTargetConnection() != null;
            assert conn.hashCode() == System.identityHashCode(conn);
            assert conn.equals(conn);

            conn.close();
        }
    }

    @Test
    public void manager_test_6() throws Throwable {
        try (DefaultDs dataSource = DsUtils.h2Ds()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            jdbcTemplate.execute((ConnectionCallback<Object>) conn1 -> {
                Connection conn2 = DataSourceUtils.getConnection(dataSource);
                assert ((ConnectionProxy) conn1).getTargetConnection() == ((ConnectionProxy) conn2).getTargetConnection();

                conn2.close();
                return null;
            });

            ConnectionHolder holder = DataSourceUtils.getHolder(dataSource);
            assert !holder.isOpen();

            holder.released();
        }
    }

    @Test
    public void tooManyDs_test_1() throws Throwable {
        DefaultDs dataSource = DsUtils.h2Ds();
        TransactionManager manager1 = new LocalTransactionManager(dataSource);
        TransactionManager manager2 = new LocalTransactionManager(dataSource);

        assert DataSourceUtils.holderMap.get().size() == 0;

        TransactionStatus tran1 = manager1.begin(Propagation.REQUIRED, Isolation.READ_COMMITTED);
        TransactionStatus tran2 = manager2.begin(Propagation.REQUIRED, Isolation.READ_COMMITTED);
        assert DataSourceUtils.holderMap.get().size() == 1;

        manager2.commit(tran2);
        assert DataSourceUtils.holderMap.get().size() == 1;

        manager1.commit(tran1);
        assert DataSourceUtils.holderMap.get().size() == 0;
    }

    @Test
    public void tooManyDs_test_2() throws Throwable {
        for (int i = 0; i < 10; i++) {
            DefaultDs dataSource = DsUtils.h2Ds();
            new JdbcTemplate(dataSource).queryForString("select 1");
        }

        assert DataSourceUtils.holderMap.get().size() == 0;
        assert DataSourceUtils.holderMap.get().size() == 0;
    }
}
