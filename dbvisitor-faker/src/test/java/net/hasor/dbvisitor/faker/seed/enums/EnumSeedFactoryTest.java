package net.hasor.dbvisitor.faker.seed.enums;

import org.junit.Test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Supplier;

public class EnumSeedFactoryTest {

    @Test
    public void buildEnums_1() {
        EnumSeedFactory factory = new EnumSeedFactory();
        EnumSeedConfig genConfig = new EnumSeedConfig();
        genConfig.setDict(new HashSet<>(Arrays.asList("One", "Two", "Three", "Four", "Five")));
        genConfig.setAllowNullable(false);

        Supplier<Serializable> enumSupplier = factory.createSeed(genConfig);

        for (int i = 0; i < 10; i++) {
            System.out.println(enumSupplier.get());
        }
    }

    @Test
    public void buildEnums_2() {
        EnumSeedFactory factory = new EnumSeedFactory();
        EnumSeedConfig genConfig = new EnumSeedConfig();
        genConfig.setDict(new HashSet<>(Arrays.asList("One", "Two", "Three", "Four", "Five")));
        genConfig.setAllowNullable(true);

        Supplier<Serializable> enumSupplier = factory.createSeed(genConfig);

        for (int i = 0; i < 10; i++) {
            System.out.println("val :" + enumSupplier.get());
        }
    }

    @Test
    public void buildEnums_3() {
        EnumSeedFactory factory = new EnumSeedFactory();
        EnumSeedConfig genConfig = new EnumSeedConfig();
        genConfig.setDict(new HashSet<>(Arrays.asList("One", "Two", "Three", "Four", "Five")));
        genConfig.setAllowNullable(true);
        genConfig.setNullableRatio(50.0f);

        Supplier<Serializable> enumSupplier = factory.createSeed(genConfig);

        for (int i = 0; i < 10; i++) {
            System.out.println("val :" + enumSupplier.get());
        }
    }
}
