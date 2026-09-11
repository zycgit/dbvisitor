/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dialect;
import net.hasor.dbvisitor.dialect.dto.TestDialect;
import net.hasor.dbvisitor.dialect.provider.DefaultSqlDialect;
import net.hasor.test.AbstractDbTest;
import org.junit.Test;

/***
 * 方言注册器
 * @version 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class RegisterTest extends AbstractDbTest {
    @Test
    public void dialectRegisterTest_1() {
        assert SqlDialectRegister.findOrCreate("") == DefaultSqlDialect.DEFAULT;
        assert SqlDialectRegister.findOrCreate(null) == DefaultSqlDialect.DEFAULT;
    }

    @Test
    public void dialectRegisterTest_2() {
        try {
            SqlDialectRegister.clearDialectCache();
            SqlDialectRegister.findOrCreate("abc");
        } catch (IllegalStateException e) {
            assert e.getMessage().equals("load dialect 'abc' class not found");
        }
    }

    @Test
    public void dialectRegisterTest_3() {
        SqlDialectRegister.clearDialectCache();
        SqlDialect dialect = SqlDialectRegister.findOrCreate("net.hasor.dbvisitor.dialect.dto.TestDialect");
        assert dialect != null;
        assert dialect instanceof TestDialect;
    }
}
