package net.hasor.scene.declarative.crud;

import net.hasor.dbvisitor.mapper.Insert;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.scene.declarative.crud.dto.UserTable;

@SimpleMapper
public interface UserMapper {
    @Insert({ "insert into user_info (user_uuid, user_name, login_name, login_password, email, seq, register_time)",//
            "values (#{userUuid}, #{name}, #{loginName}, #{loginPassword}, #{email}, #{seq}, #{registerTime})" })
    int createUser(UserTable tbUser);
}
