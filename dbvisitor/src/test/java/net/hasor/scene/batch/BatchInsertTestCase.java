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
import java.util.*;
import java.util.stream.Collectors;

/** 批量插入的不同传参方式 */
public class BatchInsertTestCase {
    @Test
    public void batchInsertArrays_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Object[][] args = new Object[][] {//
                    { 6, "person 1", 33, new Date() },  //
                    { 7, "person 2", 44, new Date() },  //
                    { 8, "person 3", 55, new Date() },  //
            };

            int[] ints = jdbcTemplate.executeBatch("insert into user values (?, ?, ?, ?);", args);
            assert ints.length == 3;
            assert Arrays.stream(ints).sum() == 3;

            List<UserDTO> users = jdbcTemplate.queryForList("select * from user where id in (6,7,8);", UserDTO.class);
            Set<String> ids = users.stream().map(UserDTO::getName).collect(Collectors.toSet());

            assert ids.size() == 3;
            assert ids.contains("person 1");
            assert ids.contains("person 2");
            assert ids.contains("person 3");
        }
    }

    @Test
    public void batchInsertSource_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            SqlParameterSource[] args = new SqlParameterSource[] { //
                    new MapSqlParameterSource(CollectionUtils.asMap("id", 6, "name", "person 1", "age", 33, "date", new Date())),//
                    new MapSqlParameterSource(CollectionUtils.asMap("id", 7, "name", "person 2", "age", 44, "date", new Date())),//
                    new MapSqlParameterSource(CollectionUtils.asMap("id", 8, "name", "person 3", "age", 55, "date", new Date())),//
            };

            int[] ints = jdbcTemplate.executeBatch("insert into user values (:id, :name, :age, :date);", args);
            assert ints.length == 3;
            assert Arrays.stream(ints).sum() == 3;

            List<UserDTO> users = jdbcTemplate.queryForList("select * from user where id in (6,7,8);", UserDTO.class);
            Set<String> ids = users.stream().map(UserDTO::getName).collect(Collectors.toSet());

            assert ids.size() == 3;
            assert ids.contains("person 1");
            assert ids.contains("person 2");
            assert ids.contains("person 3");
        }
    }

    @Test
    public void batchInsertMap_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Map<String, Object>[] args = new Map[] { //
                    CollectionUtils.asMap("id", 6, "name", "person 1", "age", 33, "date", new Date()),//
                    CollectionUtils.asMap("id", 7, "name", "person 2", "age", 44, "date", new Date()),//
                    CollectionUtils.asMap("id", 8, "name", "person 3", "age", 55, "date", new Date()),//
            };

            int[] ints = jdbcTemplate.executeBatch("insert into user values (:id, :name, :age, :date);", args);
            assert ints.length == 3;
            assert Arrays.stream(ints).sum() == 3;

            List<UserDTO> users = jdbcTemplate.queryForList("select * from user where id in (6,7,8);", UserDTO.class);
            Set<String> ids = users.stream().map(UserDTO::getName).collect(Collectors.toSet());

            assert ids.size() == 3;
            assert ids.contains("person 1");
            assert ids.contains("person 2");
            assert ids.contains("person 3");
        }
    }

    @Test
    public void batchInsertSetter_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            final Object[][] args = new Object[][] {          //
                    { 6, "person 1", 33, new java.sql.Date(System.currentTimeMillis()) },  //
                    { 7, "person 2", 44, new java.sql.Date(System.currentTimeMillis()) },  //
                    { 8, "person 3", 55, new java.sql.Date(System.currentTimeMillis()) },  //
            };

            BatchPreparedStatementSetter setter = new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setInt(1, (Integer) args[i][0]);
                    ps.setString(2, (String) args[i][1]);
                    ps.setInt(3, (Integer) args[i][2]);
                    ps.setDate(4, (java.sql.Date) args[i][3]);
                }

                @Override
                public int getBatchSize() {
                    return args.length;
                }
            };

            int[] ints = jdbcTemplate.executeBatch("insert into user values (?, ?, ?, ?);", setter);
            assert ints.length == 3;
            assert Arrays.stream(ints).sum() == 3;

            List<UserDTO> users = jdbcTemplate.queryForList("select * from user where id in (6,7,8);", UserDTO.class);
            Set<String> ids = users.stream().map(UserDTO::getName).collect(Collectors.toSet());

            assert ids.size() == 3;
            assert ids.contains("person 1");
            assert ids.contains("person 2");
            assert ids.contains("person 3");
        }
    }
}