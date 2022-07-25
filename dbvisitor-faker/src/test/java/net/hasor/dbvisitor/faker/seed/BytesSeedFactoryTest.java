package net.hasor.dbvisitor.faker.seed;

import net.hasor.cobble.codec.HexUtils;
import net.hasor.dbvisitor.faker.seed.bytes.BytesSeedConfig;
import net.hasor.dbvisitor.faker.seed.bytes.BytesSeedFactory;
import org.junit.Test;

import java.util.function.Supplier;

public class BytesSeedFactoryTest {

    @Test
    public void buildBytes_1() {
        BytesSeedFactory factory = new BytesSeedFactory();
        BytesSeedConfig genConfig = new BytesSeedConfig();
        genConfig.setMinLength(2);
        genConfig.setMaxLength(64);
        genConfig.setAllowNullable(false);

        Supplier<byte[]> bytesSupplier = factory.createSeed(genConfig);

        for (int i = 0; i < 10; i++) {
            System.out.println(HexUtils.bytes2hex(bytesSupplier.get()));
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

        Supplier<byte[]> bytesSupplier = factory.createSeed(genConfig);

        for (int i = 0; i < 10; i++) {
            byte[] bytes = bytesSupplier.get();
            if (bytes == null) {
                System.out.println("@@NULL@@");
            } else {
                System.out.println(HexUtils.bytes2hex(bytes));
            }
        }
    }

}
