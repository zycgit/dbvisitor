package net.hasor.dbvisitor.test.realdb.db2.material;

import net.hasor.dbvisitor.mapper.*;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.AnnotationAttributesMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;

@SimpleMapper
public interface Db2AnnotationAttributesMapper extends AnnotationAttributesMapper {

    @Override
    @Insert(value = "SELECT id FROM FINAL TABLE (INSERT INTO user_info (name, age, email, create_time) " + //
            "VALUES (#{name}, #{age}, #{email}, #{createTime}))", //
            useGeneratedKeys = true, keyProperty = "id", keyColumn = "id", generatedKeySource = GeneratedKeySource.ResultSet)
    int insertWithGeneratedKeyResultSet(UserInfo user);

    @Override
    @SelectKeySql(value = "VALUES NEXT VALUE FOR user_info_id_seq", keyProperty = "id", order = Order.Before)
    @Insert("INSERT INTO user_info (id, name, age, email, create_time) " + //
            "VALUES (#{id}, #{name}, #{age}, #{email}, #{createTime})")
    int insertWithSelectKeyBefore(UserInfo user);

    @Override
    @SelectKeySql(value = "VALUES PREVVAL FOR user_info_id_seq", keyProperty = "id", order = Order.After)
    @Insert("""
        INSERT INTO user_info (id, name, age, email, create_time)
        VALUES (NEXT VALUE FOR user_info_id_seq, #{name}, #{age}, #{email}, #{createTime})
        """)
    int insertWithSelectKeyAfter(UserInfo user);

    @Override
    @SelectKeySql(value = "VALUES NEXT VALUE FOR user_info_id_seq", //
            keyProperty = "id", order = Order.Before)
    @Insert("INSERT INTO user_info (id, name, age, email, create_time) " + //
            "VALUES (#{id}, #{name}, #{age}, #{email}, #{createTime})")
    int insertWithSelectKeyFullAttrs(UserInfo user);
}
