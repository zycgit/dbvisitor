package net.hasor.db.example.curd;
import net.hasor.db.dialect.BoundSql;
import net.hasor.db.example.DsUtils;
import net.hasor.db.lambda.LambdaOperations.LambdaQuery;
import net.hasor.db.lambda.core.LambdaTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class SqlBuildMain {
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();
        LambdaTemplate lambdaTemplate = new LambdaTemplate(dataSource);
        lambdaTemplate.loadSQL("CreateDB.sql");

        LambdaQuery<TestUser> query1 = lambdaTemplate.lambdaQuery(TestUser.class);
        BoundSql boundSql1 = query1.eq(TestUser::getAge, 32).getBoundSql();
        System.out.println(boundSql1);

        LambdaQuery<TestUser> query2 = lambdaTemplate.lambdaQuery(TestUser.class);
        BoundSql boundSql2 = query2.ne(TestUser::getAge, 32).getBoundSql();
        System.out.println(boundSql2);

        LambdaQuery<TestUser> query3 = lambdaTemplate.lambdaQuery(TestUser.class);
        BoundSql boundSql3 = query3.gt(TestUser::getAge, 32).getBoundSql();
        System.out.println(boundSql3);

        LambdaQuery<TestUser> query4 = lambdaTemplate.lambdaQuery(TestUser.class);
        BoundSql boundSql4 = query4.ge(TestUser::getAge, 32).getBoundSql();
        System.out.println(boundSql4);

        LambdaQuery<TestUser> query5 = lambdaTemplate.lambdaQuery(TestUser.class);
        BoundSql boundSql5 = query5.lt(TestUser::getAge, 32).getBoundSql();
        System.out.println(boundSql5);

        LambdaQuery<TestUser> query6 = lambdaTemplate.lambdaQuery(TestUser.class);
        BoundSql boundSql6 = query6.le(TestUser::getAge, 32).getBoundSql();
        System.out.println(boundSql6);

        LambdaQuery<TestUser> query7 = lambdaTemplate.lambdaQuery(TestUser.class);
        BoundSql boundSql7 = query7.likeLeft(TestUser::getName, "001").getBoundSql();
        System.out.println(boundSql7);

        LambdaQuery<TestUser> query8 = lambdaTemplate.lambdaQuery(TestUser.class);
        BoundSql boundSql8 = query8.isNull(TestUser::getAge).getBoundSql();
        System.out.println(boundSql8);

        LambdaQuery<TestUser> query9 = lambdaTemplate.lambdaQuery(TestUser.class);
        BoundSql boundSql9 = query9.isNotNull(TestUser::getAge).getBoundSql();
        System.out.println(boundSql9);

        List<Integer> argsIn = Arrays.asList(22, 32);
        LambdaQuery<TestUser> query10 = lambdaTemplate.lambdaQuery(TestUser.class);
        BoundSql boundSql10 = query10.in(TestUser::getAge, argsIn).getBoundSql();
        System.out.println(boundSql10);

        List<Integer> argsNotIn = Arrays.asList(22, 32);
        LambdaQuery<TestUser> query11 = lambdaTemplate.lambdaQuery(TestUser.class);
        BoundSql boundSql11 = query11.notIn(TestUser::getAge, argsNotIn).getBoundSql();
        System.out.println(boundSql11);

        LambdaQuery<TestUser> query12 = lambdaTemplate.lambdaQuery(TestUser.class);
        BoundSql boundSql12 = query12.between(TestUser::getAge, 20, 30).getBoundSql();
        System.out.println(boundSql12);

        LambdaQuery<TestUser> query13 = lambdaTemplate.lambdaQuery(TestUser.class);
        BoundSql boundSql13 = query13.notBetween(TestUser::getAge, 20, 30).getBoundSql();
        System.out.println(boundSql13);

        LambdaQuery<TestUser> query14 = lambdaTemplate.lambdaQuery(TestUser.class);
        BoundSql boundSql14 = query14.eq(TestUser::getName, "123").eq(TestUser::getAge, 12).getBoundSql();
        System.out.println(boundSql14);

        LambdaQuery<TestUser> query15 = lambdaTemplate.lambdaQuery(TestUser.class);
        BoundSql boundSql15 = query15.eq(TestUser::getName, "123").or().eq(TestUser::getAge, 12).getBoundSql();
        System.out.println(boundSql15);

        LambdaQuery<TestUser> query16 = lambdaTemplate.lambdaQuery(TestUser.class);
        BoundSql boundSql16 = query16.and(qc -> {
            qc.like(TestUser::getName, "123").eq(TestUser::getAge, 12);
        }).or(qc -> {
            qc.eq(TestUser::getId, 1);
        }).getBoundSql();
        System.out.println(boundSql16);
    }
}
