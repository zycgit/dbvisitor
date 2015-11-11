/*
 * Copyright 2008-2009 the original 赵永春(zyc@hasor.net).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.test.hasor.db._06_transaction.direct.REQUIRED;
import java.sql.Connection;
import org.junit.Test;
import org.junit.runner.RunWith;
import net.hasor.db.datasource.DSManager;
import net.hasor.db.jdbc.core.JdbcTemplate;
import net.hasor.db.transaction.Propagation;
import net.test.hasor.db._06_transaction.direct.AbstractNativesJDBCTest;
import net.test.hasor.db._07_datasource.warp.OneDataSourceWarp;
import net.test.hasor.test.junit.ContextConfiguration;
import net.test.hasor.test.runner.HasorUnitRunner;
/**
 * 
 * @version : 2015年11月10日
 * @author 赵永春(zyc@hasor.net)
 */
@RunWith(HasorUnitRunner.class)
@ContextConfiguration(value = "jdbc-config.xml", loadModules = OneDataSourceWarp.class)
public class RequiredTranTest extends AbstractNativesJDBCTest {
    protected Propagation testPropagation() {
        return Propagation.REQUIRED;
    }
    //
    /* PROPAGATION_REQUIRED：加入已有的事务
     *   -条件：环境中没有事务，事务管理器会创建一个事务。 */
    @Test
    public void requiredTranTest() throws Throwable {
        System.out.println("--->>haveTarn_REQUIRED_Test<<--");
        /* 执行步骤：
         *   T1   ，新建‘默罕默德’用户   (打印：默罕默德).
         *      T2，开启事务           (不打印).
         *      T2，新建‘安妮.贝隆’用户  (不打印).
         *      T2，新建‘吴广’用户      (不打印).
         *      T2，递交事务           (打印：默罕默德、安妮.贝隆、吴广).
         *   T1   ，新建‘赵飞燕’用户    (打印：默罕默德、安妮.贝隆、吴广、赵飞燕).
         */
        Connection conn = DSManager.getConnection(dataSource);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        doTransactionalA(jdbcTemplate);
        DSManager.releaseConnection(conn, dataSource);
    }
    //
    /* PROPAGATION_REQUIRED：加入已有的事务
     * -条件：环境中有事务，事务管理器会将当前事务加入已有的事务中(commit,rollback均不生效)。*/
    @Test
    public void requiredTest() throws Throwable {
        System.out.println("--->>haveTarn_REQUIRED_Test<<--");
        /* 执行步骤：
         *   T1   ，开启事务           (不打印).
         *   T1   ，新建‘默罕默德’用户   (不打印).
         *      T2，开启事务           (不打印).
         *      T2，新建‘安妮.贝隆’用户  (不打印).
         *      T2，新建‘吴广’用户      (不打印).
         *      T2，递交事务           (不打印).
         *   T1   ，新建‘赵飞燕’用户    (不打印).
         *   T1   ，递交事务           (打印：默罕默德、安妮.贝隆、吴广、赵飞燕).
         */
        Connection conn = DSManager.getConnection(dataSource);
        conn.setAutoCommit(false);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        doTransactionalA(jdbcTemplate);
        conn.commit();
        DSManager.releaseConnection(conn, dataSource);
    }
}