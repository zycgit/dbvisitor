package net.hasor.dbvisitor.test.dao.user;

import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.test.dto.UserDTO;

import java.util.List;

public interface UserMapper extends BaseMapper<UserDTO> {

    int insertUser(@Param("name") String name, @Param("age") int age);

    List<UserDTO> queryAll();
}
