package net.hasor.dbvisitor.test.dao;
import net.hasor.dbvisitor.dal.repository.Param;
import net.hasor.dbvisitor.dal.session.BaseMapper;
import net.hasor.dbvisitor.test.dto.RoleDTO;
import net.hasor.dbvisitor.test.dto.UserDTO;

import java.util.List;

public interface RoleMapper extends BaseMapper<RoleDTO> {

    int insertUser(@Param("name") String name, @Param("age") int age);

    int updateAge(@Param("id") int userId, @Param("age") int newAge);

    int deleteByAge(@Param("age") int age);

    List<UserDTO> queryByAge(@Param("beginAge") int beginAge, @Param("endAge") int endAge);

    List<UserDTO> queryAll();
}
