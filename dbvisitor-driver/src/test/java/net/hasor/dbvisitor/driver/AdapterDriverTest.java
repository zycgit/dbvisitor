package net.hasor.dbvisitor.driver;

import java.util.Properties;
import org.junit.Test;

public class AdapterDriverTest {

    @Test
    public void parseURL_0() {
        JdbcDriver driver = new JdbcDriver();

        Properties p1 = driver.parseURL("jdbc:dbvisitor:es://127.0.0.1:9200", new Properties());
        Properties p2 = driver.parseURL("jdbc:dbvisitor:es://127.0.0.1", new Properties());
        Properties p3 = driver.parseURL("jdbc:dbvisitor:es://?hosts=127.0.0.1:9200", new Properties());
        Properties p4 = driver.parseURL("jdbc:dbvisitor:es://127.0.0.1:9200,127.0.0.2:9200?hosts=", new Properties());
    }
}
