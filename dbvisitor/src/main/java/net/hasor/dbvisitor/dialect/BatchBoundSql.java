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
 * 批量 SQL 绑定接口，扩展 {@link BoundSql} 以支持批量操作
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public interface BatchBoundSql extends BoundSql {
    /** 获取 SQL 字符串 */
    String getSqlString();

    /** 获取批量参数数组，二维参数数组，每行代表一组参数 */
    Object[][] getArgs();

    /** {@link BatchBoundSql} 的默认实现类 */
    class BatchBoundSqlObj extends BoundSqlObj implements BatchBoundSql {
        /**
         * 构造函数
         * @param sqlString SQL 字符串
         * @param paramArray 批量参数数组
         */
        public BatchBoundSqlObj(String sqlString, Object[][] paramArray) {
            super(sqlString, paramArray);
        }

        @Override
        public Object[][] getArgs() {
            return (Object[][]) super.getArgs();
        }

        @Override
        public String toString() {
            return "BoundSqlObj{'" + getSqlString() + '\'' + ", args=" + Arrays.toString(getArgs()) + '}';
        }
    }
}
