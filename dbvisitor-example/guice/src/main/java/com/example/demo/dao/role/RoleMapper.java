package com.example.demo.dao.role;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.mapper.Param;
import com.example.demo.dto.RoleDTO;

public interface RoleMapper extends BaseMapper<RoleDTO> {

    int insertUser(@Param("name") String name, @Param("age") int age);

}
