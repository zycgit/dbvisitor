package net.hasor.dbvisitor.test.dao.role;
import net.hasor.dbvisitor.dal.mapper.BaseMapper;
import net.hasor.dbvisitor.dal.repository.Param;
import net.hasor.dbvisitor.dal.repository.SimpleMapper;
import net.hasor.dbvisitor.test.dto.RoleDTO;

@SimpleMapper
public interface RoleMapper extends BaseMapper<RoleDTO> {

    int insertUser(@Param("name") String name, @Param("age") int age);

}
