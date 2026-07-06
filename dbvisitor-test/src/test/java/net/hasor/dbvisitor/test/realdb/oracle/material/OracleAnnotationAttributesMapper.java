package net.hasor.dbvisitor.test.realdb.oracle.material;

import net.hasor.dbvisitor.mapper.Insert;
import net.hasor.dbvisitor.mapper.Order;
import net.hasor.dbvisitor.mapper.SelectKeySql;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.AnnotationAttributesMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;

@SimpleMapper
public interface OracleAnnotationAttributesMapper extends AnnotationAttributesMapper {

    @Override
    @Insert(value = "INSERT INTO user_info (name, age, email, create_time) " + //
            "VALUES (#{name}, #{age}, #{email}, #{createTime})", //
            useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertWithGeneratedKeyNoKeyColumn(UserInfo user);

    @Override
    @SelectKeySql(value = "SELECT user_info_id_seq.NEXTVAL FROM dual", keyProperty = "id", order = Order.Before)
    @Insert("INSERT INTO user_info (id, name, age, email, create_time) " + //
            "VALUES (#{id}, #{name}, #{age}, #{email}, #{createTime})")
    int insertWithSelectKeyBefore(UserInfo user);

    @Override
    @SelectKeySql(value = "SELECT user_info_id_seq.CURRVAL FROM dual", keyProperty = "id", order = Order.After)
    @Insert("INSERT INTO user_info (id, name, age, email, create_time) " + //
            "VALUES (user_info_id_seq.NEXTVAL, #{name}, #{age}, #{email}, #{createTime})")
    int insertWithSelectKeyAfter(UserInfo user);

    @Override
    @SelectKeySql(value = "SELECT user_info_id_seq.NEXTVAL FROM dual", //
            keyProperty = "id", order = Order.Before)
    @Insert("INSERT INTO user_info (id, name, age, email, create_time) " + //
            "VALUES (#{id}, #{name}, #{age}, #{email}, #{createTime})")
    int insertWithSelectKeyFullAttrs(UserInfo user);
}
