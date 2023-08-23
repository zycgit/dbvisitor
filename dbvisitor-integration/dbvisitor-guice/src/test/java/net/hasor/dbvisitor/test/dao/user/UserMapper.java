package net.hasor.dbvisitor.test.dao.user;
import net.hasor.dbvisitor.dal.mapper.BaseMapper;
import net.hasor.dbvisitor.dal.repository.Param;
import net.hasor.dbvisitor.dal.repository.SimpleMapper;
import net.hasor.dbvisitor.test.dto.UserDTO;

import java.util.List;

@SimpleMapper
public interface UserMapper extends BaseMapper<UserDTO> {

    int insertUser(@Param("name") String name, @Param("age") int age);

    List<UserDTO> queryAll();
}
