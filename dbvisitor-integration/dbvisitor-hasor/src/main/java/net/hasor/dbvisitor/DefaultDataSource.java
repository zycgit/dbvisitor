package net.hasor.dbvisitor;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DefaultDataSource extends AbstractDataSource {

    private String jdbcUrl;
    private String driverClassName;
    private String username;
    private String password;

    @Override
    public Connection getConnection() throws SQLException {
        return this.getConnection(this.username, this.password);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        try {
            Class.forName(this.driverClassName);
        } catch (ClassNotFoundException e) {
            throw new SQLException(e);
        }

        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getDriverClassName() {
        return this.driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}