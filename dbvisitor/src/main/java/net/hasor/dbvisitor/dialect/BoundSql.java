/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dialect;
import java.util.Arrays;

/**
 * 封装 SQL 语句和参数
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public interface BoundSql {
    /** 获取 SQL 字符串 */
    String getSqlString();

    /** 获取参数数组 */
    Object[] getArgs();

    /** {@link BoundSql} 的默认实现类 */
    class BoundSqlObj implements BoundSql {
        private final String   sqlString;
        private final Object[] paramArray;

        /**
         * 构造函数
         * @param sqlString SQL 字符串
         */
        public BoundSqlObj(String sqlString) {
            this.sqlString = sqlString;
            this.paramArray = new Object[0];
        }

        /**
         * 构造函数
         * @param sqlString SQL 字符串
         * @param paramArray 参数数组
         */
        public BoundSqlObj(String sqlString, Object[] paramArray) {
            this.sqlString = sqlString;
            this.paramArray = paramArray;
        }

        public String getSqlString() {
            return this.sqlString;
        }

        @Override
        public Object[] getArgs() {
            return this.paramArray;
        }

        @Override
        public String toString() {
            return "BoundSqlObj{'" + sqlString + '\'' + ", args=" + Arrays.toString(paramArray) + '}';
        }
    }
}
