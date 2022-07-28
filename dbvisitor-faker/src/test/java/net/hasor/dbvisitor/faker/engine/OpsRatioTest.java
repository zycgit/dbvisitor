package net.hasor.dbvisitor.faker.engine;
import net.hasor.dbvisitor.faker.engine.ratio.OpsRatio;
import org.junit.Test;

import java.io.IOException;

public class OpsRatioTest {
    @Test
    public void iteratorlocks() throws IOException {
        OpsRatio opsRatio = new OpsRatio("I#20,UPDATE# 12;d # 33");
        System.out.println();
    }
}