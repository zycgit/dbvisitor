package net.hasor.db.test.dao;
import net.hasor.db.dal.repository.Param;
import net.hasor.db.dal.session.BaseMapper;
import net.hasor.db.test.dto.UserDTO;

import java.util.List;

public interface UserMapper extends BaseMapper<UserDTO> {

    int insertUser(@Param("name") String name, @Param("age") int age);

    int updateAge(@Param("id") int userId, @Param("age") int newAge);

    int deleteByAge(@Param("age") int age);

    List<UserDTO> queryByAge(@Param("beginAge") int beginAge, @Param("endAge") int endAge);

    List<UserDTO> queryAll();
}
