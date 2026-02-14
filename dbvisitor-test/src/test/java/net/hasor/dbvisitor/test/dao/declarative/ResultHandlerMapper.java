package net.hasor.dbvisitor.test.dao.declarative;

import java.util.List;
import net.hasor.dbvisitor.mapper.*;
import net.hasor.dbvisitor.test.handler.CustomResultSetExtractor;
import net.hasor.dbvisitor.test.handler.CustomRowCallbackHandler;
import net.hasor.dbvisitor.test.handler.CustomRowMapper;
import net.hasor.dbvisitor.test.model.UserInfo;

/**
 * Mapper for @Query result handler attribute tests.
 * Covers: resultSetExtractor, resultRowMapper, resultRowCallback, resultTypeHandler,
 * and combinations with other @Query attributes.
 */
@SimpleMapper
public interface ResultHandlerMapper {

    // ========== Data setup ==========

    @Insert("INSERT INTO user_info (id, name, age, email, create_time) " +//
            "VALUES (#{id}, #{name}, #{age}, #{email}, #{createTime})")
    int insertUser(UserInfo user);

    /** Default query (no custom handler) for comparison */
    @Query("SELECT * FROM user_info WHERE name LIKE #{pattern}")
    List<UserInfo> selectDefault(@Param("pattern") String pattern);

    // ========== resultSetExtractor ==========

    /** Custom ResultSetExtractor processes the entire ResultSet */
    @Query(value = "SELECT * FROM user_info WHERE name LIKE #{pattern}",//
            resultSetExtractor = CustomResultSetExtractor.class)
    List<UserInfo> selectWithExtractor(@Param("pattern") String pattern);

    /** ResultSetExtractor combined with fetchSize and resultSetType */
    @Query(value = "SELECT * FROM user_info WHERE name LIKE #{pattern}",//
            resultSetExtractor = CustomResultSetExtractor.class, fetchSize = 50, resultSetType = ResultSetType.SCROLL_INSENSITIVE)
    List<UserInfo> selectWithExtractorAndOptions(@Param("pattern") String pattern);

    // ========== resultRowMapper ==========

    /** Custom RowMapper adds [RowN] prefix to name */
    @Query(value = "SELECT * FROM user_info WHERE name LIKE #{pattern}",//
            resultRowMapper = CustomRowMapper.class)
    List<UserInfo> selectWithRowMapper(@Param("pattern") String pattern);

    /** Custom RowMapper for single result */
    @Query(value = "SELECT * FROM user_info WHERE id = #{id}",//
            resultRowMapper = CustomRowMapper.class)
    UserInfo selectSingleWithRowMapper(@Param("id") Integer id);

    /** RowMapper combined with timeout and fetchSize */
    @Query(value = "SELECT * FROM user_info WHERE name LIKE #{pattern}",//
            resultRowMapper = CustomRowMapper.class, timeout = 30, fetchSize = 100)
    List<UserInfo> selectWithRowMapperAndOptions(@Param("pattern") String pattern);

    // ========== resultRowCallback ==========

    /** RowCallbackHandler — void return, processes rows via callback */
    @Query(value = "SELECT * FROM user_info WHERE name LIKE #{pattern}",//
            resultRowCallback = CustomRowCallbackHandler.class)
    void selectWithRowCallback(@Param("pattern") String pattern);

    /** RowCallbackHandler with timeout */
    @Query(value = "SELECT * FROM user_info WHERE name LIKE #{pattern}",//
            resultRowCallback = CustomRowCallbackHandler.class, timeout = 30)
    void selectWithRowCallbackAndTimeout(@Param("pattern") String pattern);

    // ========== Queries without custom handlers for comparison ==========

    @Query("SELECT * FROM user_info WHERE id = #{id}")
    UserInfo selectById(@Param("id") Integer id);
}
