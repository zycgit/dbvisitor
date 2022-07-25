package net.hasor.dbvisitor.faker.seed;

import net.hasor.cobble.RandomUtils;
import net.hasor.dbvisitor.faker.seed.string.Characters;
import net.hasor.dbvisitor.faker.seed.string.StandardCharacterSet;
import net.hasor.dbvisitor.faker.seed.string.StringSeedConfig;
import net.hasor.dbvisitor.faker.seed.string.StringSeedFactory;
import net.hasor.dbvisitor.faker.seed.string.characters.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.function.Supplier;

public class StringSeedFactoryTest {

    @Test
    public void characters_1() {
        AsciiCharacters characters = new AsciiCharacters();

        for (int i = 0; i < 10; i++) {
            int nextInt = RandomUtils.nextInt(0, characters.getSize());
            System.out.println(nextInt + " - " + characters.getChar(nextInt));
        }
    }

    @Test
    public void characters_2() {
        ControlCharacters characters = new ControlCharacters();

        for (int i = 0; i < 10; i++) {
            int nextInt = RandomUtils.nextInt(0, characters.getSize());
            System.out.println(nextInt + " - " + characters.getChar(nextInt));
        }
    }

    @Test
    public void characters_3() {
        HexNumberCharacters characters = new HexNumberCharacters();

        for (int i = 0; i < 10; i++) {
            int nextInt = RandomUtils.nextInt(0, characters.getSize());
            System.out.println(nextInt + " - " + characters.getChar(nextInt));
        }
    }

    @Test
    public void characters_4() {
        SmallLetterCharacters characters = new SmallLetterCharacters();

        for (int i = 0; i < 10; i++) {
            int nextInt = RandomUtils.nextInt(0, characters.getSize());
            System.out.println(nextInt + " - " + characters.getChar(nextInt));
        }
    }

    @Test
    public void characters_5() {
        NumberCharacters characters = new NumberCharacters();

        for (int i = 0; i < 10; i++) {
            int nextInt = RandomUtils.nextInt(0, characters.getSize());
            System.out.println(nextInt + " - " + characters.getChar(nextInt));
        }
    }

    @Test
    public void characters_6() {
        CapitalLetterCharacters characters = new CapitalLetterCharacters();

        for (int i = 0; i < 10; i++) {
            int nextInt = RandomUtils.nextInt(0, characters.getSize());
            System.out.println(nextInt + " - " + characters.getChar(nextInt));
        }
    }

    @Test
    public void characters_7() {
        CapitalLetterCharacters characters = new CapitalLetterCharacters();

        for (int i = 0; i < 10; i++) {
            int nextInt = RandomUtils.nextInt(0, characters.getSize());
            System.out.println(nextInt + " - " + characters.getChar(nextInt));
        }
    }

    @Test
    public void characters_8() {

        Characters charPool = StandardCharacterSet.BASIC_LATIN;

        for (int i = 0; i < 100; i++) {
            int nextInt = RandomUtils.nextInt(0, charPool.getSize());
            System.out.print(charPool.getChar(nextInt));
        }
    }

    @Test
    public void buildString_1() {
        StringSeedFactory factory = new StringSeedFactory();
        StringSeedConfig genConfig = new StringSeedConfig();
        genConfig.setMinLength(2);
        genConfig.setMaxLength(64);
        genConfig.setAllowNullable(false);
        genConfig.setCharacterSet(new HashSet<>(Arrays.asList(StandardCharacterSet.CAPITAL_LETTER, StandardCharacterSet.NUMERIC)));

        Supplier<String> stringSupplier = factory.createSeed(genConfig);
        for (int i = 0; i < 10; i++) {
            System.out.println(stringSupplier.get());
        }
    }

    @Test
    public void buildString_2() {
        StringSeedFactory factory = new StringSeedFactory();
        StringSeedConfig genConfig = new StringSeedConfig();
        genConfig.setMinLength(2);
        genConfig.setMaxLength(64);
        genConfig.setAllowNullable(false);
        genConfig.setCharacterSet(new HashSet<>(Collections.singletonList(StandardCharacterSet.HEX)));

        Supplier<String> stringSupplier = factory.createSeed(genConfig);
        for (int i = 0; i < 10; i++) {
            System.out.println(stringSupplier.get());
        }
    }

    @Test
    public void buildString_3() {
        StringSeedFactory factory = new StringSeedFactory();
        StringSeedConfig genConfig = new StringSeedConfig();
        genConfig.setMinLength(2);
        genConfig.setMaxLength(64);
        genConfig.setAllowNullable(true);
        genConfig.setAllowEmpty(true);
        genConfig.setNullableRatio(20.0f);
        genConfig.setCharacterSet(new HashSet<>(Arrays.asList(StandardCharacterSet.HEX)));

        Supplier<String> stringSupplier = factory.createSeed(genConfig);
        for (int i = 0; i < 10; i++) {
            System.out.println(stringSupplier.get());
        }
    }
}
