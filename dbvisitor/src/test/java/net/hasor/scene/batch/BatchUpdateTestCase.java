package net.hasor.scene.batch;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.jdbc.BatchPreparedStatementSetter;
import net.hasor.dbvisitor.jdbc.SqlParameterSource;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.jdbc.paramer.MapSqlParameterSource;
import net.hasor.scene.UserDTO;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BatchUpdateTestCase {
    @Test
    public void batchUpdateArrays_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Object[][] args = new Object[][] {//
                    { "person 1", 1 },  //
                    { "person 2", 2 },  //
                    { "person 3", 3 },  //
            };

            int[] ints = jdbcTemplate.executeBatch("update user set name = ? where id = ?;", args);
            assert ints.length == 3;
            assert Arrays.stream(ints).sum() == 3;

            List<UserDTO> users = jdbcTemplate.queryForList("select * from user where id in (1,2,3);", UserDTO.class);
            Set<String> ids = users.stream().map(UserDTO::getName).collect(Collectors.toSet());

            assert ids.size() == 3;
            assert ids.contains("person 1");
            assert ids.contains("person 2");
            assert ids.contains("person 3");
        }
    }

    @Test
    public void batchUpdateSource_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            SqlParameterSource[] args = new SqlParameterSource[] { //
                    new MapSqlParameterSource(CollectionUtils.asMap("id", 1, "name", "person 1")),//
                    new MapSqlParameterSource(CollectionUtils.asMap("id", 2, "name", "person 2")),//
                    new MapSqlParameterSource(CollectionUtils.asMap("id", 3, "name", "person 3")),//
            };

            int[] ints = jdbcTemplate.executeBatch("update user set name =:name where id = :id;", args);
            assert ints.length == 3;
            assert Arrays.stream(ints).sum() == 3;

            List<UserDTO> users = jdbcTemplate.queryForList("select * from user where id in (1,2,3);", UserDTO.class);
            Set<String> ids = users.stream().map(UserDTO::getName).collect(Collectors.toSet());

            assert ids.size() == 3;
            assert ids.contains("person 1");
            assert ids.contains("person 2");
            assert ids.contains("person 3");
        }
    }

    @Test
    public void batchUpdateMap_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Map<String, Object>[] args = new Map[] { //
                    CollectionUtils.asMap("id", 1, "name", "person 1"),//
                    CollectionUtils.asMap("id", 2, "name", "person 2"),//
                    CollectionUtils.asMap("id", 3, "name", "person 3"),//
            };

            int[] ints = jdbcTemplate.executeBatch("update user set name =:name where id = :id;", args);
            assert ints.length == 3;
            assert Arrays.stream(ints).sum() == 3;

            List<UserDTO> users = jdbcTemplate.queryForList("select * from user where id in (1,2,3);", UserDTO.class);
            Set<String> ids = users.stream().map(UserDTO::getName).collect(Collectors.toSet());

            assert ids.size() == 3;
            assert ids.contains("person 1");
            assert ids.contains("person 2");
            assert ids.contains("person 3");
        }
    }

    @Test
    public void batchUpdateSetter_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            final Object[][] args = new Object[][] {//
                    { "person 1", 1 }, //
                    { "person 2", 2 }, //
                    { "person 3", 3 }, //
            };

            BatchPreparedStatementSetter setter = new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setString(1, (String) args[i][0]);
                    ps.setInt(2, (Integer) args[i][1]);
                }

                @Override
                public int getBatchSize() {
                    return args.length;
                }
            };

            int[] ints = jdbcTemplate.executeBatch("update user set name = ? where id = ?;", setter);
            assert ints.length == 3;
            assert Arrays.stream(ints).sum() == 3;

            List<UserDTO> users = jdbcTemplate.queryForList("select * from user where id in (1,2,3);", UserDTO.class);
            Set<String> ids = users.stream().map(UserDTO::getName).collect(Collectors.toSet());

            assert ids.size() == 3;
            assert ids.contains("person 1");
            assert ids.contains("person 2");
            assert ids.contains("person 3");
        }
    }
}
