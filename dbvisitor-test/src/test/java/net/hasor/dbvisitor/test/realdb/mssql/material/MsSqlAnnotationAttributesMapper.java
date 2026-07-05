package net.hasor.dbvisitor.test.realdb.mssql.material;

import net.hasor.dbvisitor.mapper.GeneratedKeySource;
import net.hasor.dbvisitor.mapper.Insert;
import net.hasor.dbvisitor.mapper.SimpleMapper;
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
            useGeneratedKeys = true, keyProperty = "id", keyColumn = "id", generatedKeySource = GeneratedKeySource.ResultSet)
    int insertWithGeneratedKeyResultSet(UserInfo user);
}
