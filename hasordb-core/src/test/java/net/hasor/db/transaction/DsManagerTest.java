package net.hasor.db.transaction;
import com.alibaba.druid.pool.DruidDataSource;
import net.hasor.db.jdbc.ConnectionCallback;
import net.hasor.db.jdbc.core.JdbcTemplate;
import net.hasor.test.db.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.Statement;

public class DsManagerTest {
    @Test
    public void manager_test_1() throws Throwable {
        try (DruidDataSource dataSource = DsUtils.createDs()) {
            Connection conn1 = DataSourceManager.getConnection(dataSource);
            Connection conn2 = DataSourceManager.getConnection(dataSource);

            assert conn1 != conn2;
            assert ((ConnectionProxy) conn1).getTargetConnection() == ((ConnectionProxy) conn2).getTargetConnection();
            assert ((ConnectionProxy) conn1).getTargetSource() == ((ConnectionProxy) conn2).getTargetSource();
            assert ((ConnectionProxy) conn1).getTargetSource() == dataSource;
        }
    }

    @Test
    public void manager_test_2() throws Throwable {
        try (DruidDataSource dataSource = DsUtils.createDs()) {
            try (Connection conn = DataSourceManager.getConnection(dataSource)) {
                int result = new JdbcTemplate(conn).queryForInt("select 123");
                assert result == 123;
            }

            try (Connection conn = DataSourceManager.getConnection(dataSource)) {
                int result = new JdbcTemplate(conn).queryForInt("select 123");
                assert result == 123;
            }
        }
    }

    @Test
    public void manager_test_3() throws Throwable {
        try (DruidDataSource dataSource = DsUtils.mysqlDataSource()) {
            Connection conn1 = DataSourceManager.getConnection(dataSource);
            Connection conn2 = DataSourceManager.getConnection(dataSource);

            String conn1_id = new JdbcTemplate(conn1).queryForString("select connection_id();");
            String conn2_id = new JdbcTemplate(conn2).queryForString("select connection_id()");

            assert conn1_id.equals(conn2_id);
        }
    }

    @Test
    public void manager_test_4() throws Throwable {
        try (DruidDataSource dataSource = DsUtils.createDs()) {
            Connection conn1 = DataSourceManager.getConnection(dataSource);
            Connection conn2 = DataSourceManager.getConnection(dataSource);

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
        try (DruidDataSource dataSource = DsUtils.createDs()) {
            Connection conn = DataSourceManager.getConnection(dataSource);

            assert conn instanceof ConnectionProxy;
            assert ((ConnectionProxy) conn).getTargetSource() == dataSource;

            assert ((ConnectionProxy) conn).getTargetConnection() != null;
            assert conn.hashCode() == System.identityHashCode(conn);
            assert conn.equals(conn);

            conn.close();
        }
    }

    @Test
    public void manager_test_6() throws Throwable {
        try (DruidDataSource dataSource = DsUtils.createDs(false)) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            jdbcTemplate.execute((ConnectionCallback<Object>) conn1 -> {
                Connection conn2 = DataSourceManager.getConnection(dataSource);
                assert ((ConnectionProxy) conn1).getTargetConnection() == ((ConnectionProxy) conn2).getTargetConnection();

                conn2.close();
                return null;
            });

            ConnectionHolder holder = DataSourceManager.getHolder(dataSource);
            assert !holder.isOpen();
        }
    }
}
