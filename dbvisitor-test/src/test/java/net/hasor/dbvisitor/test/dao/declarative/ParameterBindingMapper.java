package net.hasor.dbvisitor.test.dao.declarative;

import java.util.Date;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.mapper.*;
import net.hasor.dbvisitor.test.model.UserInfo;

/**
 * Mapper for parameter binding tests.
 * Covers: positional (?), @Param named, Bean, Map, mixed, reuse.
 */
@SimpleMapper
public interface ParameterBindingMapper {

    // ========== Positional parameters (?) ==========

    @Insert("INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, CURRENT_TIMESTAMP)")
    int insertByPosition(Integer id, String name, Integer age);

    @Update("UPDATE user_info SET age = ? WHERE id = ?")
    int updateByPosition(Integer age, Integer id);

    // ========== @Param named parameters ==========

    @Insert("INSERT INTO user_info (id, name, age, email, create_time) " +//
            "VALUES (#{id}, #{name}, #{age}, #{email}, CURRENT_TIMESTAMP)")
    int insertWithParam(@Param("id") Integer id, @Param("name") String name, @Param("age") Integer age, @Param("email") String email);

    // ========== Bean property binding ==========

    @Insert("INSERT INTO user_info (id, name, age, email, create_time) " +//
            "VALUES (#{id}, #{name}, #{age}, #{email}, #{createTime})")
    int insertBean(UserInfo user);

    // ========== Map parameter ==========

    @Insert("INSERT INTO user_info (id, name, age, email, create_time) " +//
            "VALUES (#{id}, #{name}, #{age}, #{email}, CURRENT_TIMESTAMP)")
    int insertByMap(Map<String, Object> params);

    // ========== Queries for verification ==========

    @Query("SELECT * FROM user_info WHERE id = #{id}")
    UserInfo selectById(@Param("id") Integer id);

    @Query("SELECT * FROM user_info WHERE age BETWEEN #{minAge} AND #{maxAge}")
    List<UserInfo> selectByAgeRange(@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge);

    // ========== Array / List IN clause via @{in} rule ==========

    /** Array parameter — @{in} rule auto-expands Integer[] to (?, ?, ...) */
    @Query("SELECT * FROM user_info WHERE age IN @{in, :ages}")
    List<UserInfo> selectByAgesArray(@Param("ages") Integer[] ages);

    /** List parameter — @{in} rule auto-expands List<Integer> to (?, ?, ...) */
    @Query("SELECT * FROM user_info WHERE age IN @{in, :ages}")
    List<UserInfo> selectByAgesList(@Param("ages") List<Integer> ages);

    // ========== Mixed: @Param + Bean ==========

    @Insert("INSERT INTO user_info (id, name, age, email, create_time) " +//
            "VALUES (#{user.id}, #{user.name}, #{user.age}, #{email}, CURRENT_TIMESTAMP)")
    int insertMixed(@Param("user") UserInfo user, @Param("email") String email);

    // ========== Parameter reuse (same param used twice) ==========

    @Insert("INSERT INTO user_info (id, name, age, email, create_time) " +//
            "VALUES (#{id}, #{name}, 25, #{name}, CURRENT_TIMESTAMP)")
    int insertWithReuse(@Param("id") Integer id, @Param("name") String name);

    // ========== Many parameters ==========

    @Insert("INSERT INTO user_info (id, name, age, email, create_time) " +//
            "VALUES (#{id}, #{name}, #{age}, #{email}, #{createTime})")
    int insertWithManyParams(@Param("id") Integer id, @Param("name") String name, @Param("age") Integer age, @Param("email") String email, @Param("createTime") Date createTime, @Param("extra1") String extra1, @Param("extra2") String extra2);

}
