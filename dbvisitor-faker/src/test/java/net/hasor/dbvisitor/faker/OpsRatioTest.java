package net.hasor.dbvisitor.faker;
import net.hasor.cobble.ref.Ratio;
import org.junit.Test;

import java.io.IOException;

public class OpsRatioTest {
    @Test
    public void iteratorlocks() throws IOException {
        Ratio<OpsType> opsRatio = RatioUtils.passerByConfig("I#20,UPDATE# 12;d # 33");
        System.out.println();
    }
}