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
package net.test.hasor.db._06_transaction.test;
import org.junit.Test;
import org.junit.runner.RunWith;
import net.hasor.db.Transactional;
import net.hasor.db.transaction.Propagation;
import net.hasor.db.transaction.TransactionCallback;
import net.hasor.db.transaction.TransactionStatus;
import net.hasor.db.transaction.TransactionTemplate;
import net.test.hasor.db._06_transaction.AbstractNativesJDBCTest;
import net.test.hasor.db._07_datasource.warp.OneDataSourceWarp;
import net.test.hasor.junit.ContextConfiguration;
import net.test.hasor.junit.HasorUnitRunner;
/**
 * NEVER：如果当前没有事务存在，就以非事务方式执行；如果有，就抛出异常。
 * @version : 2015年11月15日
 * @author 赵永春(zyc@hasor.net)
 */
@RunWith(HasorUnitRunner.class)
@ContextConfiguration(value = "jdbc-config.xml", loadModules = OneDataSourceWarp.class)
public class NEVER_TranTest extends AbstractNativesJDBCTest {
    @Test
    public void testHasTransactional() throws Throwable {
        System.out.println("--->>NEVER －> 测试条件，环境中存在事物。");
        System.out.println("--->>NEVER －>     数据库应存在：“默罕默德”、“赵飞燕”");
        System.out.println("--->>NEVER －>     共计 2 条记录。");
        System.out.println();
        //
        TransactionTemplate temp = appContext.getInstance(TransactionTemplate.class);
        temp.execute(new TransactionCallback<Void>() {
            public Void doTransaction(TransactionStatus tranStatus) throws Throwable {
                System.out.println("begin T1!");
                /*T1 - 默罕默德*/
                insertUser_MHMD();
                /*T2 - 安妮.贝隆、吴广*/
                try {
                    doTransactional();
                } catch (Exception e) {
                    System.out.println("T2 error = " + e.getMessage());
                } finally {
                    Thread.sleep(500);
                }
                /*T1 - 赵飞燕*/
                insertUser_ZFY();
                System.out.println("commit T1!");
                return null;
            }
        });
        //
        Thread.sleep(1000);
        printData();
    }
    @Test
    public void testNoneTransactional() throws Throwable {
        System.out.println("--->>NEVER －> 测试条件，环境不存在事物。");
        System.out.println("--->>NEVER －>     数据库应存在：“默罕默德”、“安妮.贝隆”、“吴广”、“赵飞燕”");
        System.out.println("--->>NEVER －>     共计 4 条记录。");
        System.out.println();
        //
        System.out.println("begin T1!");
        /*T1 - 默罕默德*/
        insertUser_MHMD();
        /*T2 - 安妮.贝隆、吴广*/
        doTransactional();
        /*T1 - 赵飞燕*/
        insertUser_ZFY();
        System.out.println("commit T1!");
        //
        Thread.sleep(1000);
        printData();
    }
    //
    //
    //
    //
    @Transactional(propagation = Propagation.NEVER)
    public void doTransactional() throws Throwable {
        System.out.println("begin T2!");
        /*安妮.贝隆*/
        insertUser_ANBL();
        /*吴广*/
        insertUser_WG();
        System.out.println("commit T2!");
    }
}