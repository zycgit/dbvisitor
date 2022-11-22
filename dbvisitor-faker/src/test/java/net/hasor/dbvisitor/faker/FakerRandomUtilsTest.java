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
package net.hasor.dbvisitor.faker;
import org.junit.Test;

import java.math.BigInteger;

/***
 * 创建 JDBC
 * @version : 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
@Deprecated
public class FakerRandomUtilsTest {
    @Test
    public void nextLongTest_01() {
        for (int i = 0; i < 1000000; i++) {
            long ranLong = FakerRandomUtils.nextLong(BigInteger.valueOf(-100), BigInteger.valueOf(100)).longValue();
            if (-100 <= ranLong && ranLong <= 100) {
                assert true;
            } else {
                System.out.println(ranLong);
                assert false;
            }
        }
    }

    @Test
    public void nextLongTest_02() {
        for (int i = 0; i < 1000000; i++) {
            long ranLong = FakerRandomUtils.nextLong(BigInteger.valueOf(100), BigInteger.valueOf(200)).longValue();
            if (100 <= ranLong && ranLong <= 200) {
                assert true;
            } else {
                System.out.println(ranLong);
                assert false;
            }
        }
    }
}