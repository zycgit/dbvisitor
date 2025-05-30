/*
 * Copyright 2015-2022 the original author or authors.
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
