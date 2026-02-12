package net.hasor.dbvisitor.test.oneapi.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.RefMapper;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;

/**
 * @RefMapper 接口，通过命名空间引用 XmlRefMapper.xml
 */
@RefMapper("/oneapi/mapper/XmlRefMapper.xml")
public interface XmlRefMapperDao {

    int insertUser(@Param("id") int id, @Param("name") String name, @Param("age") int age, @Param("email") String email) throws SQLException;

    UserInfo selectById(@Param("id") int id) throws SQLException;

    List<UserInfo> selectAll() throws SQLException;

    int updateEmail(@Param("id") int id, @Param("email") String email) throws SQLException;

    int deleteById(@Param("id") int id) throws SQLException;

    List<UserInfo> selectByCondition(@Param("name") String name, @Param("minAge") Integer minAge) throws SQLException;

    List<UserInfo> selectByIds(@Param("ids") List<Integer> ids) throws SQLException;

    List<UserInfo> selectByAgeRange(@Param("minAge") int minAge, @Param("maxAge") int maxAge) throws SQLException;

    List<UserInfo> selectByBean(@Param("name") String name, @Param("age") int age) throws SQLException;

    List<UserInfo> selectWithOrderBy(@Param("orderColumn") String orderColumn) throws SQLException;

    List<Map<String, Object>> selectAgeStats() throws SQLException;
}
