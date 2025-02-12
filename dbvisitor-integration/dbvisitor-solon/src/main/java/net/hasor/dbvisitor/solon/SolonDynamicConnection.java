package net.hasor.dbvisitor.solon;

import net.hasor.dbvisitor.jdbc.DynamicConnection;
import org.noear.solon.data.tran.TranUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author noear
 * @see 1.8
 */
public class SolonDynamicConnection implements DynamicConnection {
    private final DataSource dataSource;

    public SolonDynamicConnection(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return TranUtils.getConnectionProxy(dataSource);
    }

    @Override
    public void releaseConnection(Connection conn) throws SQLException {
        conn.close();
    }
}
