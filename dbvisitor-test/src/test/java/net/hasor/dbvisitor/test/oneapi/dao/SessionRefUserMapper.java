package net.hasor.dbvisitor.test.oneapi.dao;

import java.util.List;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.RefMapper;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import net.hasor.dbvisitor.test.oneapi.model.UserOrderDTO;

/**
 * 基于 @RefMapper 引用 XML 的用户 DAO，用于 Session.createMapper 测试
 */
@RefMapper("/oneapi/session/SessionRefUserMapper.xml")
public interface SessionRefUserMapper {

    UserInfo queryUserById(@Param("id") Integer id);

    List<UserInfo> queryAllUsers();

    int insertUser(UserInfo user);

    int updateUserEmail(@Param("id") Integer id, @Param("email") String email);

    int deleteUserById(@Param("id") Integer id);

    int countUsers();

    UserOrderDTO queryOrderWithUser(@Param("orderId") Integer orderId);
}
