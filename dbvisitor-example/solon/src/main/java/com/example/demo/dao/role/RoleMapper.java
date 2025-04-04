package com.example.demo.dao.role;
import com.example.demo.dto.RoleDTO;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.mapper.Param;

public interface RoleMapper extends BaseMapper<RoleDTO> {

    int insertUser(@Param("name") String name, @Param("age") int age);

}
