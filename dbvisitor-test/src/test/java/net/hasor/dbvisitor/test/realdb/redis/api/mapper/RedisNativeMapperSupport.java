/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.util.*;
import java.sql.*;
import net.hasor.dbvisitor.jdbc.*;
import net.hasor.dbvisitor.mapper.*;
import net.hasor.dbvisitor.test.realdb.redis.scenario.RedisScenarioSupport;

public abstract class RedisNativeMapperSupport extends RedisScenarioSupport {

    protected NativeMapper mapper() throws Exception {
        return session.createMapper(NativeMapper.class);
    }

    protected void loadXml() throws Exception {
        session.getConfiguration().loadMapper("/mapper/redis/NativeMapper.xml");
    }

    protected Map<String, Object> params(String key, String value) {
        Map<String, Object> args = new HashMap<>();
        args.put("key", key);
        args.put("value", value);
        return args;
    }

    public static class Entry {

        private String field;

        private String value;

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class Generated {

        private String key;

        private String counter;

        private String value;

        private Long id;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getCounter() {
            return counter;
        }

        public void setCounter(String counter) {
            this.counter = counter;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }

    public static class ValuesMapper implements RowMapper<String> {

        public String mapRow(ResultSet rs, int row) throws SQLException {
            return row + ":" + rs.getString("ELEMENT");
        }
    }

    public static class ValuesExtractor implements ResultSetExtractor<List<String>> {

        public List<String> extractData(ResultSet rs) throws SQLException {
            List<String> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rs.getString("ELEMENT"));
            }
            return result;
        }
    }

    public static class ValuesCallback implements RowCallbackHandler {

        public static final ThreadLocal<List<String>> VALUES = ThreadLocal.withInitial(ArrayList::new);

        public void processRow(ResultSet rs, int rowNum) throws SQLException {
            VALUES.get().add(rs.getString("ELEMENT"));
        }
    }

    @SimpleMapper
    public interface SimpleNativeMapper extends NativeMapper {
    }

    @RefMapper("/mapper/redis/RefNativeMapper.xml")
    public interface RefNativeMapper {

        int put(@Param("key") String key, @Param("value") String value);

        String get(@Param("key") String key);

        int replace(@Param("key") String key, @Param("value") String value);

        int remove(@Param("key") String key);

        List<String> list(@Param("key") String key);

    }

    @SimpleMapper
    public interface NativeMapper {

        @Insert("SET #{key} #{value}")
        int put(@Param("key") String key, @Param("value") String value);

        @Insert("SET ? ?")
        int positional(String key, String value);

        @Update("SET #{key} #{value} XX")
        int replace(@Param("key") String key, @Param("value") String value);

        @Delete("DEL #{key}")
        int remove(@Param("key") String key);

        @Execute("SET #{key} #{value}")
        int execute(@Param("key") String key, @Param("value") String value);

        @Query("GET #{key}")
        String get(@Param("key") String key);

        @Query(value = "GET #{key}", timeout = 5, fetchSize = 1)
        String options(@Param("key") String key);

        @Query(value = "PING", statementType = StatementType.Statement)
        String statement(@Param("key") String key);

        @Insert({ "SET", "#{key}", "#{value}" })
        Object multiline(@Param("key") String key, @Param("value") String value);

        @Query("LRANGE #{key} 0 -1")
        List<String> list(@Param("key") String key);

        @Query("LRANGE #{key} 0 -1")
        List<Integer> integers(@Param("key") String key);

        @Query("ZRANGEBYSCORE #{key} #{min} #{max}")
        List<String> range(@Param("key") String key, @Param("min") int min, @Param("max") int max);

        @Query("HGETALL #{key}")
        List<Entry> entries(@Param("key") String key);

        @Query("HKEYS #{key}")
        List<Entry> partialEntries(@Param("key") String key);

        @Query("HGETALL #{key}")
        List<Map<String, Object>> maps(@Param("key") String key);

        @Query("HGETALL #{key}")
        Map<String, Object> map(@Param("key") String key);

        @Query(value = "LRANGE #{key} 0 -1", resultRowMapper = ValuesMapper.class)
        List<String> mapped(@Param("key") String key);

        @Query(value = "LRANGE #{key} 0 -1", resultRowMapper = ValuesMapper.class, timeout = 5, fetchSize = 1)
        List<String> mappedOptions(@Param("key") String key);

        @Query(value = "LINDEX #{key} 0", resultRowMapper = ValuesMapper.class)
        String first(@Param("key") String key);

        @Query(value = "LRANGE #{key} 0 0", resultRowMapper = ValuesMapper.class)
        String firstRow(@Param("key") String key);

        @Query(value = "LRANGE #{key} 0 -1", resultSetExtractor = ValuesExtractor.class)
        List<String> extracted(@Param("key") String key);

        @Query(value = "LRANGE #{key} 0 -1", resultSetExtractor = ValuesExtractor.class, timeout = 5, fetchSize = 1)
        List<String> extractedOptions(@Param("key") String key);

        @Query(value = "LRANGE #{key} 0 -1", resultRowCallback = ValuesCallback.class)
        void callback(@Param("key") String key);

        @Insert("HSET #{key} #{id} #{value}")
        @SelectKeySql(value = "INCR #{counter}", keyProperty = "id", order = Order.Before)
        int before(Generated value);

        @Insert("RPUSH #{key} #{value}")
        @SelectKeySql(value = "LLEN #{key}", keyProperty = "id", order = Order.After)
        int after(Generated value);

        @Insert(value = "INCR #{counter}", useGeneratedKeys = true, generatedKeySource = GeneratedKeySource.ResultSet, keyProperty = "id", keyColumn = "VALUE")
        int resultKey(Generated value);
    }
}
