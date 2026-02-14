package net.hasor.dbvisitor.test.dao;

import java.util.List;
import net.hasor.dbvisitor.mapper.*;
import net.hasor.dbvisitor.test.model.UserInfo;

/**
 * 基于 @SimpleMapper 的用户 DAO，用于 Session.createMapper 测试
 */
@SimpleMapper
public interface SessionUserMapper {

    @Insert("INSERT INTO user_info (id, name, age, email, create_time) " + "VALUES (#{id}, #{name}, #{age}, #{email}, CURRENT_TIMESTAMP)")
    int insertUser(UserInfo user);

    @Query("SELECT * FROM user_info WHERE id = #{id}")
    UserInfo selectById(@Param("id") Integer id);

    @Query("SELECT * FROM user_info ORDER BY id ASC")
    List<UserInfo> selectAll();

    @Update("UPDATE user_info SET name = #{name}, age = #{age} WHERE id = #{id}")
    int updateUser(UserInfo user);

    @Delete("DELETE FROM user_info WHERE id = #{id}")
    int deleteById(@Param("id") Integer id);

    @Query("SELECT COUNT(*) FROM user_info")
    int countAll();
}
