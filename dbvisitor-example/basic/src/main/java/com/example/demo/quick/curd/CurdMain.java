package com.example.demo.quick.curd;
import com.example.demo.DsUtils;
import com.example.demo.PrintUtils;
import net.hasor.dbvisitor.lambda.LambdaTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class CurdMain {
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();

        LambdaTemplate wrapper = new LambdaTemplate(dataSource);
        wrapper.jdbc().loadSQL("CreateDB.sql");

        // 查询，所有数据
        List<TestUser> dtoList = wrapper.query(TestUser.class)//
                .queryForList();
        PrintUtils.printObjectList(dtoList);

        // 插入新数据
        TestUser newUser = new TestUser();
        newUser.setName("new User");
        newUser.setAge(33);
        newUser.setCreateTime(new Date());
        int result1 = wrapper.insert(TestUser.class)//
                .applyEntity(newUser).executeSumResult();

        dtoList = wrapper.query(TestUser.class).queryForList();
        PrintUtils.printObjectList(dtoList);

        // 更新，将name 从 mali 更新为 mala
        TestUser sample = new TestUser();
        sample.setName("mala");
        int result2 = wrapper.update(TestUser.class)//
                .eq(TestUser::getId, 1).updateToSample(sample).doUpdate();

        dtoList = wrapper.query(TestUser.class).queryForList();
        PrintUtils.printObjectList(dtoList);

        // 删除，ID 为 2 的数据删掉
        int result3 = wrapper.delete(TestUser.class)//
                .eq(TestUser::getId, 2).doDelete();

        dtoList = wrapper.query(TestUser.class).queryForList();
        PrintUtils.printObjectList(dtoList);
    }
}
