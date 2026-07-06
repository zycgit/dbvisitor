package net.hasor.dbvisitor.test.realdb.mssql.material;

import net.hasor.dbvisitor.mapper.*;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.AnnotationAttributesMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;

@SimpleMapper
public interface MsSqlAnnotationAttributesMapper extends AnnotationAttributesMapper {
    @Override
    @Insert(value = "INSERT INTO user_info (name, age, email, create_time) " + //
            "OUTPUT INSERTED.id VALUES (#{name}, #{age}, #{email}, #{createTime})", //
            useGeneratedKeys = true, keyProperty = "id", keyColumn = "id", generatedKeySource = GeneratedKeySource.ResultSet)
    int insertWithKeyProperty(UserInfo user);

    @Override
    @Insert(value = "INSERT INTO user_info (name, age, email, create_time) " + //
            "OUTPUT INSERTED.id VALUES (#{name}, #{age}, #{email}, #{createTime})", //
            useGeneratedKeys = true, keyProperty = "id", generatedKeySource = GeneratedKeySource.ResultSet)
    int insertWithGeneratedKeyNoKeyColumn(UserInfo user);

    @Override
    @Insert(value = "INSERT INTO user_info (name, age, email, create_time) " + //
            "OUTPUT INSERTED.id VALUES (#{name}, #{age}, #{email}, #{createTime})", //
            useGeneratedKeys = true, keyProperty = "id", keyColumn = "id", generatedKeySource = GeneratedKeySource.ResultSet)
    int insertWithGeneratedKeyResultSet(UserInfo user);

    @Override
    @SelectKeySql(value = "SELECT NEXT VALUE FOR user_info_id_seq", keyProperty = "id", order = Order.Before)
    @Insert("INSERT INTO user_info (id, name, age, email, create_time) " + //
            "VALUES (#{id}, #{name}, #{age}, #{email}, #{createTime})")
    int insertWithSelectKeyBefore(UserInfo user);

    @Override
    @SelectKeySql(value = "SELECT CONVERT(INT, current_value) FROM sys.sequences WHERE name = 'user_info_id_seq'", keyProperty = "id", order = Order.After)
    @Insert("INSERT INTO user_info (id, name, age, email, create_time) " + //
            "VALUES (NEXT VALUE FOR user_info_id_seq, #{name}, #{age}, #{email}, #{createTime})")
    int insertWithSelectKeyAfter(UserInfo user);

    @Override
    @SelectKeySql(value = "SELECT NEXT VALUE FOR user_info_id_seq", //
            keyProperty = "id", order = Order.Before)
    @Insert("INSERT INTO user_info (id, name, age, email, create_time) " + //
            "VALUES (#{id}, #{name}, #{age}, #{email}, #{createTime})")
    int insertWithSelectKeyFullAttrs(UserInfo user);
}
