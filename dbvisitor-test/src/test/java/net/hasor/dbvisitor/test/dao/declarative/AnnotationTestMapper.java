package net.hasor.dbvisitor.test.dao.declarative;

import java.util.List;
import net.hasor.dbvisitor.mapper.*;
import net.hasor.dbvisitor.test.model.UserInfo;

/**
 * Mapper for annotation CRUD type tests.
 * Covers: @Insert, @Update, @Delete, @Query, @Execute, multi-line SQL, error scenarios.
 */
@SimpleMapper
public interface AnnotationTestMapper {

    // ========== @Insert ==========

    /** Bean parameter binding */
    @Insert("INSERT INTO user_info (id, name, age, email, create_time) " +//
            "VALUES (#{id}, #{name}, #{age}, #{email}, #{createTime})")
    int insertUser(UserInfo user);

    /** Multiple @Param parameters */
    @Insert("INSERT INTO user_info (id, name, age, email, create_time) " +//
            "VALUES (#{id}, #{name}, #{age}, #{email}, CURRENT_TIMESTAMP)")
    int insertUserWithParams(@Param("id") Integer id, @Param("name") String name, @Param("age") Integer age, @Param("email") String email);

    /** Multi-line SQL via value[] array */
    @Insert({ "INSERT INTO user_info",//
            "(id, name, age, email, create_time)",//
            "VALUES",//
            "(#{id}, #{name}, #{age}, #{email}, #{createTime})" })
    int insertUserMultiLine(UserInfo user);

    // ========== @Update ==========

    /** Update single field */
    @Update("UPDATE user_info SET age = #{age} WHERE id = #{id}")
    int updateUserAge(@Param("id") Integer id, @Param("age") Integer age);

    /** Update multiple fields */
    @Update("UPDATE user_info SET name = #{name}, age = #{age} WHERE id = #{id}")
    int updateUserInfo(@Param("id") Integer id, @Param("name") String name, @Param("age") Integer age);

    /** Batch update returning affected rows */
    @Update("UPDATE user_info SET age = #{newAge} WHERE age = #{oldAge}")
    int updateAgeByRange(@Param("oldAge") Integer oldAge, @Param("newAge") Integer newAge);

    // ========== @Delete ==========

    /** Delete by id */
    @Delete("DELETE FROM user_info WHERE id = #{id}")
    int deleteById(@Param("id") Integer id);

    /** Delete by condition */
    @Delete("DELETE FROM user_info WHERE age = #{age}")
    int deleteByAge(@Param("age") Integer age);

    // ========== @Query ==========

    /** Query single object */
    @Query("SELECT * FROM user_info WHERE id = #{id}")
    UserInfo selectById(@Param("id") Integer id);

    /** Query list */
    @Query("SELECT * FROM user_info WHERE age = #{age}")
    List<UserInfo> selectByAge(@Param("age") Integer age);

    /** Query count */
    @Query("SELECT COUNT(*) FROM user_info WHERE age = #{age}")
    int countByAge(@Param("age") Integer age);

    /** Query with LIKE */
    @Query("SELECT * FROM user_info WHERE name LIKE #{pattern}")
    List<UserInfo> selectByNameLike(@Param("pattern") String pattern);

    /** Query with IN clause using @{in} rule — auto-expands array to (?, ?, ...) */
    @Query("SELECT * FROM user_info WHERE age IN @{in, :ages}")
    List<UserInfo> selectByAgeIn(@Param("ages") Integer[] ages);

    // ========== @Execute ==========

    /** DDL: create temporary table */
    @Execute("CREATE TEMPORARY TABLE IF NOT EXISTS temp_anno_test (id INT, name VARCHAR(50))")
    void createTempTable();

    /** DDL: drop temporary table */
    @Execute("DROP TABLE IF EXISTS temp_anno_test")
    void dropTempTable();

    /** DML via @Execute with parameters */
    @Execute("INSERT INTO temp_anno_test (id, name) VALUES (#{id}, #{name})")
    void insertTempData(@Param("id") Integer id, @Param("name") String name);

    /** Query temp table for verification */
    @Query("SELECT name FROM temp_anno_test WHERE id = #{id}")
    String selectTempData(@Param("id") Integer id);

    // ========== Error scenarios ==========

    /** SQL with syntax error (FORM instead of FROM) */
    @Query("SELECT * FORM user_info WHERE id = 1")
    UserInfo selectWithSyntaxError();

    /** Query non-existent table */
    @Query("SELECT * FROM non_existent_table WHERE id = 1")
    UserInfo selectFromNonExistentTable();

    /** Query non-existent column */
    @Query("SELECT non_existent_column FROM user_info WHERE id = 1")
    String selectNonExistentColumn();
}
