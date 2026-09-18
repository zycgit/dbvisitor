package net.hasor.dbvisitor.driver;
import java.sql.Connection;
import java.sql.DriverPropertyInfo;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class DriverPropertyInfoTest {
    private final        JdbcDriver driver = new JdbcDriver();
    private static final String     URL    = "jdbc:dbvisitor:property-test://localhost";

    @Before
    public void registerOfflineFactory() {
        AdapterManager.register("property-test", new MockAdapterFactory() {
            @Override
            public String[] getPropertyNames() {
                return new String[] { "adapterName", "user", "password", "option", "option" };
            }

            @Override
            public AdapterConnection createConnection(Connection owner, String url, Properties properties) {
                throw new AssertionError("Property discovery must not open a connection");
            }

            @Override
            public TypeSupport createTypeSupport(Properties properties) {
                throw new AssertionError("Property discovery must not initialize runtime support");
            }
        });
    }

    private Map<String, DriverPropertyInfo> properties(String url, Properties input) {
        Map<String, DriverPropertyInfo> result = new LinkedHashMap<>();
        for (DriverPropertyInfo property : driver.getPropertyInfo(url, input)) {
            assertNull("Duplicate property: " + property.name, result.put(property.name, property));
        }
        return result;
    }

    @Test
    public void returnsUnsetPropertiesInDeclarationOrder() {
        Map<String, DriverPropertyInfo> result = properties(URL, new Properties());
        assertEquals(Arrays.asList("user", "password", "option", "server"), Arrays.asList(result.keySet().toArray()));
        assertNull(result.get("option").value);
        assertEquals("localhost", result.get("server").value);
        assertFalse(result.containsKey(JdbcDriver.P_ADAPTER_NAME));
    }

    @Test
    public void preservesPropertiesAndInheritedDefaultsWithUrlPrecedence() {
        Properties defaults = new Properties();
        defaults.setProperty("user", "inherited-user");
        defaults.setProperty("option", "inherited-option");
        Properties input = new Properties(defaults);
        input.setProperty("option", "explicit-option");
        input.setProperty("password", "test-password");
        Map<String, DriverPropertyInfo> result = properties(URL + "?option=url-option", input);
        assertEquals("inherited-user", result.get("user").value);
        assertEquals("test-password", result.get("password").value);
        assertEquals("url-option", result.get("option").value);
        assertEquals("explicit-option", properties(URL, input).get("option").value);
        assertEquals(2, input.size());
        assertEquals("explicit-option", input.getProperty("option"));
        assertEquals("inherited-option", defaults.getProperty("option"));
        assertFalse(input.containsKey("server"));
    }

    @Test
    public void supportsNullPropertiesAndPartialUrls() {
        for (String url : new String[] { URL, "jdbc:dbvisitor:property-test://", "jdbc:dbvisitor:property-test:", "jdbc:dbvisitor:property-test" }) {
            assertEquals(4, properties(url, null).size());
        }
        Properties input = new Properties();
        input.setProperty("adapterName", "property-test");
        assertEquals(4, properties(null, input).size());
        assertEquals(4, properties("", input).size());
    }

    @Test
    public void returnsEmptyWhenAdapterCannotBeIdentified() {
        for (String url : new String[] { null, "", "jdbc:dbvisitor:", "jdbc:dbvisitor://localhost", "jdbc:dbvisitor:unknown://localhost", "jdbc:mysql://localhost" }) {
            assertTrue(properties(url, null).isEmpty());
        }
    }

    @Test
    public void discoveryResultsCannotCorruptCachedNames() {
        String[] names = AdapterManager.propertyNames("property-test", new Properties());
        names[0] = "corrupted";
        assertTrue(properties(URL, null).containsKey("user"));
        DriverPropertyInfo[] first = driver.getPropertyInfo(URL, null);
        first[0].name = "corrupted";
        first[0].value = "changed";
        assertNull(properties(URL, null).get("user").value);
    }
}
