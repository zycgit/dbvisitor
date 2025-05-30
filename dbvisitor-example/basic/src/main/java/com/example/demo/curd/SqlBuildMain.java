package com.example.demo.curd;
import com.example.demo.DsUtils;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.lambda.EntityQuery;
import net.hasor.dbvisitor.lambda.LambdaTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class SqlBuildMain {
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();
        LambdaTemplate wrapper = new LambdaTemplate(dataSource);
        wrapper.jdbc().loadSQL("CreateDB.sql");

        EntityQuery<TestUser> query1 = wrapper.query(TestUser.class);
        BoundSql boundSql1 = query1.eq(TestUser::getAge, 32).getBoundSql();
        System.out.println(boundSql1);

        EntityQuery<TestUser> query2 = wrapper.query(TestUser.class);
        BoundSql boundSql2 = query2.ne(TestUser::getAge, 32).getBoundSql();
        System.out.println(boundSql2);

        EntityQuery<TestUser> query3 = wrapper.query(TestUser.class);
        BoundSql boundSql3 = query3.gt(TestUser::getAge, 32).getBoundSql();
        System.out.println(boundSql3);

        EntityQuery<TestUser> query4 = wrapper.query(TestUser.class);
        BoundSql boundSql4 = query4.ge(TestUser::getAge, 32).getBoundSql();
        System.out.println(boundSql4);

        EntityQuery<TestUser> query5 = wrapper.query(TestUser.class);
        BoundSql boundSql5 = query5.lt(TestUser::getAge, 32).getBoundSql();
        System.out.println(boundSql5);

        EntityQuery<TestUser> query6 = wrapper.query(TestUser.class);
        BoundSql boundSql6 = query6.le(TestUser::getAge, 32).getBoundSql();
        System.out.println(boundSql6);

        EntityQuery<TestUser> query7 = wrapper.query(TestUser.class);
        BoundSql boundSql7 = query7.likeLeft(TestUser::getName, "001").getBoundSql();
        System.out.println(boundSql7);

        EntityQuery<TestUser> query8 = wrapper.query(TestUser.class);
        BoundSql boundSql8 = query8.isNull(TestUser::getAge).getBoundSql();
        System.out.println(boundSql8);

        EntityQuery<TestUser> query9 = wrapper.query(TestUser.class);
        BoundSql boundSql9 = query9.isNotNull(TestUser::getAge).getBoundSql();
        System.out.println(boundSql9);

        List<Integer> argsIn = Arrays.asList(22, 32);
        EntityQuery<TestUser> query10 = wrapper.query(TestUser.class);
        BoundSql boundSql10 = query10.in(TestUser::getAge, argsIn).getBoundSql();
        System.out.println(boundSql10);

        List<Integer> argsNotIn = Arrays.asList(22, 32);
        EntityQuery<TestUser> query11 = wrapper.query(TestUser.class);
        BoundSql boundSql11 = query11.notIn(TestUser::getAge, argsNotIn).getBoundSql();
        System.out.println(boundSql11);

        EntityQuery<TestUser> query12 = wrapper.query(TestUser.class);
        BoundSql boundSql12 = query12.rangeBetween(TestUser::getAge, 20, 30).getBoundSql();
        System.out.println(boundSql12);

        EntityQuery<TestUser> query13 = wrapper.query(TestUser.class);
        BoundSql boundSql13 = query13.rangeNotBetween(TestUser::getAge, 20, 30).getBoundSql();
        System.out.println(boundSql13);

        EntityQuery<TestUser> query14 = wrapper.query(TestUser.class);
        BoundSql boundSql14 = query14.eq(TestUser::getName, "123").eq(TestUser::getAge, 12).getBoundSql();
        System.out.println(boundSql14);

        EntityQuery<TestUser> query15 = wrapper.query(TestUser.class);
        BoundSql boundSql15 = query15.eq(TestUser::getName, "123").or().eq(TestUser::getAge, 12).getBoundSql();
        System.out.println(boundSql15);

        EntityQuery<TestUser> query16 = wrapper.query(TestUser.class);
        BoundSql boundSql16 = query16.and(qc -> {
            qc.like(TestUser::getName, "123").eq(TestUser::getAge, 12);
        }).or(qc -> {
            qc.eq(TestUser::getId, 1);
        }).getBoundSql();
        System.out.println(boundSql16);
    }
}
