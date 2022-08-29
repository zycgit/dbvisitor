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
package net.hasor.dbvisitor.faker.seed;

import net.hasor.cobble.codec.HexUtils;
import net.hasor.dbvisitor.faker.seed.bytes.BytesSeedConfig;
import net.hasor.dbvisitor.faker.seed.bytes.BytesSeedFactory;
import org.junit.Test;

import java.io.Serializable;
import java.util.function.Supplier;

public class BytesSeedFactoryTest {

    @Test
    public void buildBytes_1() {
        BytesSeedFactory factory = new BytesSeedFactory();
        BytesSeedConfig genConfig = new BytesSeedConfig();
        genConfig.setMinLength(2);
        genConfig.setMaxLength(64);
        genConfig.setAllowNullable(false);

        Supplier<Serializable> bytesSupplier = factory.createSeed(genConfig);

        for (int i = 0; i < 10; i++) {
            System.out.println(HexUtils.bytes2hex((byte[]) bytesSupplier.get()));
        }
    }

    @Test
    public void buildBytes_2() {
        BytesSeedFactory factory = new BytesSeedFactory();
        BytesSeedConfig genConfig = new BytesSeedConfig();
        genConfig.setMinLength(2);
        genConfig.setMaxLength(64);
        genConfig.setAllowNullable(true);
        genConfig.setNullableRatio(10.0f);

        Supplier<Serializable> bytesSupplier = factory.createSeed(genConfig);

        for (int i = 0; i < 10; i++) {
            byte[] bytes = (byte[]) bytesSupplier.get();
            if (bytes == null) {
                System.out.println("@@NULL@@");
            } else {
                System.out.println(HexUtils.bytes2hex(bytes));
            }
        }
    }

}
