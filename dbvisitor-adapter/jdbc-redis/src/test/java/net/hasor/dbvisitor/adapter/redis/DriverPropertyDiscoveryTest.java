package net.hasor.dbvisitor.adapter.redis;
import java.sql.DriverPropertyInfo;
import java.util.*;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.Test;
import static org.junit.Assert.*;

public class DriverPropertyDiscoveryTest {
    @Test
    public void discoversEveryDeclaredOptionWithoutConnecting() {
        JedisConnFactory factory = new JedisConnFactory();
        JdbcDriver driver = new JdbcDriver();
        String url = JdbcDriver.START_URL + factory.getAdapterName() + "://";
        Set<String> expected = new LinkedHashSet<>(Arrays.asList(factory.getPropertyNames()));
        expected.add(JdbcDriver.P_SERVER);
        expected.remove(JdbcDriver.P_ADAPTER_NAME);
        Map<String, DriverPropertyInfo> actual = new LinkedHashMap<>();
        for (DriverPropertyInfo property : driver.getPropertyInfo(url, null)) {
            assertNull(actual.put(property.name, property));
        }
        assertEquals(expected, actual.keySet());
        assertNull(actual.get(JdbcDriver.P_USER).value);
        assertFalse(actual.containsKey(JdbcDriver.P_ADAPTER_NAME));

        Properties input = new Properties();
        input.setProperty(JdbcDriver.P_USER, "test-user");
        DriverPropertyInfo[] configured = driver.getPropertyInfo(url + "?timeZone=UTC", input);
        assertEquals(expected.size(), configured.length);
        for (DriverPropertyInfo property : configured) {
            if (JdbcDriver.P_USER.equals(property.name)) {
                assertEquals("test-user", property.value);
            }
            if (JdbcDriver.P_TIME_ZONE.equals(property.name)) {
                assertEquals("UTC", property.value);
            }
        }
        assertEquals(1, input.size());
    }
}
