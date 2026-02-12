package net.hasor.dbvisitor.test.oneapi.dao.declarative;

import java.util.List;
import net.hasor.dbvisitor.mapper.*;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;

/**
 * Mapper for annotation attribute tests.
 * Covers: statementType, timeout, fetchSize, resultSetType,
 * useGeneratedKeys, keyProperty, keyColumn, @SelectKeySql, combined attributes.
 */
@SimpleMapper
public interface AnnotationAttributesMapper {

    // ========== Data setup ==========

    @Insert("INSERT INTO user_info (id, name, age, email, create_time) " +//
            "VALUES (#{id}, #{name}, #{age}, #{email}, #{createTime})")
    int insertUserBasic(UserInfo user);

    // ========== statementType ==========

    @Query(value = "SELECT * FROM user_info WHERE id = #{id}",//
            statementType = StatementType.Prepared)
    UserInfo selectByIdPrepared(@Param("id") Integer id);

    @Query(value = "SELECT * FROM user_info WHERE id = ${id}",//
            statementType = StatementType.Statement)
    UserInfo selectByIdStatement(@Param("id") Integer id);

    // ========== timeout ==========

    @Query(value = "SELECT * FROM user_info WHERE id = #{id}", timeout = -1)
    UserInfo selectByIdDefaultTimeout(@Param("id") Integer id);

    @Query(value = "SELECT * FROM user_info WHERE id = #{id}", timeout = 30)
    UserInfo selectByIdWithTimeout(@Param("id") Integer id);

    @Query(value = "SELECT * FROM user_info WHERE id = #{id}", timeout = 3600)
    UserInfo selectWithMaxTimeout(@Param("id") Integer id);

    @Update(value = "UPDATE user_info SET age = #{age} WHERE id = #{id}", timeout = 30)
    int updateWithTimeout(@Param("id") Integer id, @Param("age") Integer age);

    @Delete(value = "DELETE FROM user_info WHERE id = #{id}", timeout = 30)
    int deleteWithTimeout(@Param("id") Integer id);

    @Insert(value = "INSERT INTO user_info (id, name, age, email, create_time) " +//
            "VALUES (#{id}, #{name}, #{age}, #{email}, #{createTime})", timeout = 30)
    int insertWithTimeout(UserInfo user);

    // ========== fetchSize ==========

    @Query(value = "SELECT * FROM user_info WHERE name LIKE #{pattern}", fetchSize = 256)
    List<UserInfo> selectWithDefaultFetchSize(@Param("pattern") String pattern);

    @Query(value = "SELECT * FROM user_info WHERE name LIKE #{pattern}", fetchSize = 10)
    List<UserInfo> selectWithSmallFetchSize(@Param("pattern") String pattern);

    @Query(value = "SELECT * FROM user_info WHERE name LIKE #{pattern}", fetchSize = 1000)
    List<UserInfo> selectWithLargeFetchSize(@Param("pattern") String pattern);

    @Query(value = "SELECT * FROM user_info WHERE name LIKE #{pattern}", fetchSize = 1)
    List<UserInfo> selectWithFetchSizeOne(@Param("pattern") String pattern);

    // ========== resultSetType ==========

    @Query(value = "SELECT * FROM user_info WHERE name LIKE #{pattern}", resultSetType = ResultSetType.DEFAULT)
    List<UserInfo> selectWithDefaultResultSetType(@Param("pattern") String pattern);

    @Query(value = "SELECT * FROM user_info WHERE name LIKE #{pattern}", resultSetType = ResultSetType.FORWARD_ONLY)
    List<UserInfo> selectWithForwardOnly(@Param("pattern") String pattern);

    @Query(value = "SELECT * FROM user_info WHERE name LIKE #{pattern}", resultSetType = ResultSetType.SCROLL_INSENSITIVE)
    List<UserInfo> selectWithScrollInsensitive(@Param("pattern") String pattern);

    @Query(value = "SELECT * FROM user_info WHERE name LIKE #{pattern}", resultSetType = ResultSetType.SCROLL_SENSITIVE)
    List<UserInfo> selectWithScrollSensitive(@Param("pattern") String pattern);

    // ========== useGeneratedKeys / keyProperty / keyColumn ==========

    @Insert(value = "INSERT INTO user_info (name, age, email, create_time) " +//
            "VALUES (#{name}, #{age}, #{email}, #{createTime})",//
            useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertWithGeneratedKey(UserInfo user);

    @Insert(value = "INSERT INTO user_info (id, name, age, email, create_time) " +//
            "VALUES (#{id}, #{name}, #{age}, #{email}, #{createTime})", useGeneratedKeys = false)
    int insertWithoutGeneratedKey(UserInfo user);

    @Insert(value = "INSERT INTO user_info (name, age, email, create_time) " +//
            "VALUES (#{name}, #{age}, #{email}, #{createTime})",//
            useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertWithKeyProperty(UserInfo user);

    // ========== @SelectKeySql (PostgreSQL compatible) ==========

    @SelectKeySql(value = "SELECT nextval('user_info_id_seq')", keyProperty = "id", order = Order.Before)
    @Insert("INSERT INTO user_info (id, name, age, email, create_time) " +//
            "VALUES (#{id}, #{name}, #{age}, #{email}, #{createTime})")
    int insertWithSelectKeyBefore(UserInfo user);

    @SelectKeySql(value = "SELECT lastval()", keyProperty = "id", order = Order.After)
    @Insert("INSERT INTO user_info (name, age, email, create_time) " +//
            "VALUES (#{name}, #{age}, #{email}, #{createTime})")
    int insertWithSelectKeyAfter(UserInfo user);

    @SelectKeySql(value = "SELECT nextval('user_info_id_seq')",//
            keyProperty = "id", order = Order.Before,//
            statementType = StatementType.Prepared, timeout = 30, fetchSize = 1, resultSetType = ResultSetType.DEFAULT)
    @Insert("INSERT INTO user_info (id, name, age, email, create_time) " +//
            "VALUES (#{id}, #{name}, #{age}, #{email}, #{createTime})")
    int insertWithSelectKeyFullAttrs(UserInfo user);

    // ========== Multi-line SQL ==========

    @Insert({ "INSERT INTO user_info",//
            "(id, name, age, email, create_time)",//
            "VALUES",//
            "(#{id}, #{name}, #{age}, #{email}, #{createTime})" })
    int insertMultiLine(UserInfo user);

    // ========== Combined attributes ==========

    @Query(value = "SELECT * FROM user_info WHERE name LIKE #{pattern}",//
            statementType = StatementType.Prepared,//
            timeout = 60, fetchSize = 100, resultSetType = ResultSetType.SCROLL_INSENSITIVE)
    List<UserInfo> selectWithCombinedAttributes(@Param("pattern") String pattern);

    @Query(value = "SELECT * FROM user_info WHERE id = #{id}")
    UserInfo selectWithAllDefaults(@Param("id") Integer id);
}
