package net.hasor.dbvisitor.test.dao;
import net.hasor.dbvisitor.dal.mapper.BaseMapper;
import net.hasor.dbvisitor.dal.repository.Param;
import net.hasor.dbvisitor.dal.repository.SimpleMapper;
import net.hasor.dbvisitor.dal.repository.Update;
import net.hasor.dbvisitor.test.dto.UserDTO;

import java.util.List;

@SimpleMapper
public interface UserMapper extends BaseMapper<UserDTO> {
    @Update({"update users",          //
             "set name = #{newName}", //
             "where id = #{userId}"}) //
    int updateUserName(               // 2. userId 和 newName 参数
            @Param("userId") int userId,
            @Param("newName") int newName
    );


    int insertUser(@Param("name") String name, @Param("age") int age);

    int updateAge(@Param("id") int userId, @Param("age") int newAge);

    int deleteByAge(@Param("age") int age);

    List<UserDTO> queryByAge(@Param("beginAge") int beginAge, @Param("endAge") int endAge);

    List<UserDTO> queryAll();
}
