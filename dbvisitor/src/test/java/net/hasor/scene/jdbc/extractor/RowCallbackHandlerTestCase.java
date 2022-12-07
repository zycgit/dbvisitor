package net.hasor.scene.jdbc.extractor;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.jdbc.PreparedStatementSetter;
import net.hasor.dbvisitor.jdbc.SqlParameterSource;
import net.hasor.dbvisitor.jdbc.SqlParameterUtils;
import net.hasor.dbvisitor.jdbc.core.ArgPreparedStatementSetter;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.jdbc.paramer.MapSqlParameterSource;
import net.hasor.scene.UserNameRowCallback;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

/** 使用 RowCallbackHandler 接口来接收结果集 */
public class RowCallbackHandlerTestCase {
    @Test
    public void noArgsExtractor_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserNameRowCallback callback = new UserNameRowCallback();
            Object[] args = new Object[] { SqlParameterUtils.withInput(40) };
            jdbcTemplate.query("select * from user where age > ? order by id", args, callback);

            assert callback.size() == 2;
            assert callback.getName(0).equals("jon wes");
            assert callback.getName(1).equals("mary");
        }
    }

    @Test
    public void arrayArgsRowCallback_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserNameRowCallback callback = new UserNameRowCallback();
            jdbcTemplate.query("select * from user where age > 40 order by id", callback);

            assert callback.size() == 2;
            assert callback.getName(0).equals("jon wes");
            assert callback.getName(1).equals("mary");
        }
    }

    @Test
    public void mapArgsRowCallback_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserNameRowCallback callback = new UserNameRowCallback();
            Map<String, Object> args = CollectionUtils.asMap("age", 40);
            jdbcTemplate.query("select * from user where age > :age order by id", args, callback);

            assert callback.size() == 2;
            assert callback.getName(0).equals("jon wes");
            assert callback.getName(1).equals("mary");
        }
    }

    @Test
    public void setterArgsRowCallback_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserNameRowCallback callback = new UserNameRowCallback();
            PreparedStatementSetter setter = new ArgPreparedStatementSetter(new Object[] { 40 });
            jdbcTemplate.query("select * from user where age > ? order by id", setter, callback);

            assert callback.size() == 2;
            assert callback.getName(0).equals("jon wes");
            assert callback.getName(1).equals("mary");
        }
    }

    @Test
    public void sourceArgsRowCallback_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserNameRowCallback callback = new UserNameRowCallback();
            SqlParameterSource argSource = new MapSqlParameterSource(CollectionUtils.asMap("arg", 40));
            jdbcTemplate.query("select * from user where age > :arg order by id", argSource, callback);

            assert callback.size() == 2;
            assert callback.getName(0).equals("jon wes");
            assert callback.getName(1).equals("mary");
        }
    }

    @Test
    public void paramArgsRowCallback_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserNameRowCallback callback = new UserNameRowCallback();
            Object[] args = new Object[] { SqlParameterUtils.withInOut(40, Types.INTEGER) };
            jdbcTemplate.query("select * from user where age > ? order by id", args, callback);

            assert callback.size() == 2;
            assert callback.getName(0).equals("jon wes");
            assert callback.getName(1).equals("mary");
        }
    }
}