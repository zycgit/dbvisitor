///*
// * Copyright 2015-2022 the original author or authors.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http:www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package net.hasor.dbvisitor.dialect;
//import net.hasor.dbvisitor.lambda.InsertOperation;
//import net.hasor.dbvisitor.lambda.LambdaTemplate;
//import net.hasor.test.AbstractDbTest;
//import net.hasor.test.dto.TB_User;
//import org.junit.Test;
//
//import static net.hasor.test.utils.TestUtils.beanForData1;
//
///***
// * 方言
// * @version 2014-1-13
// * @author 赵永春 (zyc@hasor.net)
// */
//public class InsertSqlDialectTest extends AbstractDbTest {
//    @Test
//    public void dialect_mysql_1() {
//        InsertOperation<TB_User> lambdaInsert = new LambdaTemplate().lambdaInsert(TB_User.class);
//        lambdaInsert.applyEntity(beanForData1());
//        lambdaInsert.applyMap(mapForData2());
//
//        SqlDialect dialect = new MySqlDialect();
//        BoundSql boundSql = lambdaInsert.getBoundSql(dialect);
//
//        assert boundSql instanceof BatchBoundSql;
//        assert boundSql.getSqlString().equals("INSERT INTO TB_User (userUUID, name, loginName, loginPassword, email, `index`, registerTime) VALUES (?, ?, ?, ?, ?, ?, ?)");
//        assert boundSql.getArgs().length == 2;
//        assert ((BatchBoundSql) boundSql).getArgs()[0].length == 7;
//        assert ((BatchBoundSql) boundSql).getArgs()[1].length == 7;
//
//        assert ((BatchBoundSql) boundSql).getArgs()[0][0].equals(beanForData1().getUserUUID());
//        assert ((BatchBoundSql) boundSql).getArgs()[0][1].equals(beanForData1().getName());
//        assert ((BatchBoundSql) boundSql).getArgs()[0][2].equals(beanForData1().getLoginName());
//        assert ((BatchBoundSql) boundSql).getArgs()[0][3].equals(beanForData1().getLoginPassword());
//        assert ((BatchBoundSql) boundSql).getArgs()[0][4].equals(beanForData1().getEmail());
//        assert ((BatchBoundSql) boundSql).getArgs()[0][5].equals(beanForData1().getIndex());
//        assert ((BatchBoundSql) boundSql).getArgs()[0][6].equals(beanForData1().getRegisterTime());
//
//        assert ((BatchBoundSql) boundSql).getArgs()[1][0].equals(beanForData2().getUserUUID());
//        assert ((BatchBoundSql) boundSql).getArgs()[1][1].equals(beanForData2().getName());
//        assert ((BatchBoundSql) boundSql).getArgs()[1][2].equals(beanForData2().getLoginName());
//        assert ((BatchBoundSql) boundSql).getArgs()[1][3].equals(beanForData2().getLoginPassword());
//        assert ((BatchBoundSql) boundSql).getArgs()[1][4].equals(beanForData2().getEmail());
//        assert ((BatchBoundSql) boundSql).getArgs()[1][5].equals(beanForData2().getIndex());
//        assert ((BatchBoundSql) boundSql).getArgs()[1][6].equals(beanForData2().getRegisterTime());
//    }
//
//    //    @Test
//    //    public void dialect_mysql_2() {
//    //        LambdaInsert<TB_User> lambdaInsert = new LambdaTemplate().lambdaInsert(TB_User.class);
//    //        lambdaInsert.applyEntity(beanForData1());
//    //
//    //        SqlDialect dialect = new MySqlDialect();
//    //        BoundSql boundSql1 = lambdaInsert.onDuplicateStrategy(DuplicateKeyStrategy.Into).getBoundSql(dialect);
//    //        assert boundSql1.getSqlString().equals("INSERT INTO TB_User (userUUID, name, loginName, loginPassword, email, `index`, registerTime) VALUES (?, ?, ?, ?, ?, ?, ?)");
//    //
//    //        BoundSql boundSql2 = lambdaInsert.onDuplicateStrategy(DuplicateKeyStrategy.Ignore).getBoundSql(dialect);
//    //        assert boundSql2.getSqlString().equals("INSERT IGNORE TB_User (userUUID, name, loginName, loginPassword, email, `index`, registerTime) VALUES (?, ?, ?, ?, ?, ?, ?)");
//    //
//    //        BoundSql boundSql3 = lambdaInsert.onDuplicateStrategy(DuplicateKeyStrategy.Update).getBoundSql(dialect);
//    //        assert boundSql3.getSqlString().equals("INSERT INTO TB_User (userUUID, name, loginName, loginPassword, email, `index`, registerTime) VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE");
//    //    }
//    //
//    //    @Test
//    //    public void dialect_oracle_1() {
//    //        LambdaInsert<TbUser> lambdaInsert = new LambdaTemplate().lambdaInsert(TbUser.class);
//    //        lambdaInsert.applyEntity(mappingBeanForData1());
//    //        lambdaInsert.applyMap(mapForData2());
//    //
//    //        SqlDialect dialect = new OracleDialect();
//    //        BoundSql boundSql1 = lambdaInsert.onDuplicateStrategy(DuplicateKeyStrategy.Into).getBoundSql(dialect);
//    //        assert boundSql1.getSqlString().equals("INSERT INTO tb_user (userUUID, name, loginName, loginPassword, email, \"index\", registerTime) VALUES (?, ?, ?, ?, ?, ?, ?)");
//    //
//    //        BoundSql boundSql2 = lambdaInsert.onDuplicateStrategy(DuplicateKeyStrategy.Ignore).getBoundSql(dialect);
//    //        assert boundSql2.getSqlString().equals("MERGE INTO tb_user TMP USING (SELECT ? userUUID, ? name, ? loginName, ? loginPassword, ? email, ? \"index\", ? registerTime FROM dual ) SRC ON (TMP.userUUID = SRC.userUUID) "//
//    //                + "WHEN NOT MATCHED THEN INSERT (userUUID, name, loginName, loginPassword, email, \"index\", registerTime) VALUES( SRC.userUUID, SRC.name, SRC.loginName, SRC.loginPassword, SRC.email, SRC.\"index\", SRC.registerTime) ");
//    //
//    //        BoundSql boundSql3 = lambdaInsert.onDuplicateStrategy(DuplicateKeyStrategy.Update).getBoundSql(dialect);
//    //        assert boundSql3.getSqlString().equals("MERGE INTO tb_user TMP USING (SELECT ? userUUID, ? name, ? loginName, ? loginPassword, ? email, ? \"index\", ? registerTime FROM dual ) SRC ON (TMP.userUUID = SRC.userUUID) " //
//    //                + "WHEN MATCHED THEN UPDATE SET userUUID = SRC.userUUID, name = SRC.name, loginName = SRC.loginName, loginPassword = SRC.loginPassword, email = SRC.email, \"index\" = SRC.\"index\", registerTime = SRC.registerTime " //
//    //                + "WHEN NOT MATCHED THEN INSERT (userUUID, name, loginName, loginPassword, email, \"index\", registerTime) VALUES( SRC.userUUID, SRC.name, SRC.loginName, SRC.loginPassword, SRC.email, SRC.\"index\", SRC.registerTime) ");
//    //    }
//}
