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
package net.test.hasor.db._03_update;
import java.sql.SQLException;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.db.jdbc.core.JdbcTemplate;
import net.test.hasor.db._07_datasource.warp.OneDataSourceWarp;
import org.junit.Test;
/***
 * 基本的update操作语句执行
 * @version : 2014-1-13
 * @author 赵永春(zyc@hasor.net)
 */
public class Simple_UpdateJDBCTest {
    @Test
    public void simple_UpdateJDBCTest() throws SQLException {
        System.out.println("--->>simple_UpdateJDBCTest<<--");
        //
        AppContext app = Hasor.createAppContext("net/test/simple/db/jdbc-config.xml", new OneDataSourceWarp());
        JdbcTemplate jdbc = app.getInstance(JdbcTemplate.class);
        //
    }
}