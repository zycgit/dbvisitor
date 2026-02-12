package net.hasor.dbvisitor.test.oneapi.dao.declarative;

import java.util.Date;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.mapper.Insert;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;

/**
 * Mapper for result mapping tests.
 * Covers: entity, Map, scalars (Integer/String/Long/Date), lists, pagination, aggregates.
 */
@SimpleMapper
public interface ResultMappingMapper {

    // ========== Data setup ==========

    @Insert("INSERT INTO user_info (id, name, age, email, create_time) " +//
            "VALUES (#{id}, #{name}, #{age}, #{email}, #{createTime})")
    int insertUser(UserInfo user);

    // ========== Map to entity ==========

    @Query("SELECT * FROM user_info WHERE id = #{id}")
    UserInfo selectUserById(@Param("id") Integer id);

    /** Query only id and name — other fields should be null */
    @Query("SELECT id, name FROM user_info WHERE id = #{id}")
    UserInfo selectUserPartial(@Param("id") Integer id);

    // ========== Map to Map ==========

    @Query("SELECT * FROM user_info WHERE id = #{id}")
    Map<String, Object> selectUserAsMap(@Param("id") Integer id);

    @Query("SELECT * FROM user_info")
    List<Map<String, Object>> selectUsersAsMapList();

    // ========== Map to scalar types ==========

    @Query("SELECT age FROM user_info WHERE id = #{id}")
    Integer selectAgeById(@Param("id") Integer id);

    @Query("SELECT name FROM user_info WHERE id = #{id}")
    String selectNameById(@Param("id") Integer id);

    @Query("SELECT COUNT(*) FROM user_info")
    Long selectCount();

    @Query("SELECT create_time FROM user_info WHERE id = #{id}")
    Date selectCreateTimeById(@Param("id") Integer id);

    // ========== Map to lists ==========

    @Query("SELECT * FROM user_info WHERE age BETWEEN #{minAge} AND #{maxAge}")
    List<UserInfo> selectUsersByAgeRange(@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge);

    @Query("SELECT name FROM user_info WHERE name LIKE #{pattern}")
    List<String> selectAllNames(@Param("pattern") String pattern);

    @Query("SELECT id FROM user_info WHERE id >= #{minId} AND id <= #{maxId}")
    List<Integer> selectIdRange(@Param("minId") Integer minId, @Param("maxId") Integer maxId);

    @Query("SELECT DISTINCT age FROM user_info WHERE name LIKE #{pattern}")
    List<Integer> selectDistinctAges(@Param("pattern") String pattern);

    // ========== Aggregates ==========

    @Query("SELECT MAX(age) FROM user_info")
    Integer selectMaxAge();

    @Query("SELECT MIN(age) as minAge, MAX(age) as maxAge, AVG(age) as avgAge " +//
            "FROM user_info WHERE name LIKE #{pattern}")
    Map<String, Object> selectAgeStats(@Param("pattern") String pattern);

    @Query("SELECT age, COUNT(*) as cnt FROM user_info " +//
            "WHERE name LIKE #{pattern} GROUP BY age ORDER BY age")
    List<Map<String, Object>> selectCountByAge(@Param("pattern") String pattern);

    // ========== Pagination ==========

    @Query("SELECT * FROM user_info WHERE name LIKE #{pattern} ORDER BY id")
    List<UserInfo> selectUsersWithPagination(@Param("pattern") String pattern, PageObject page);
}
