package net.hasor.dbvisitor.driver;

import net.hasor.cobble.StringUtils;

import java.sql.Connection;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JdbcDriver implements java.sql.Driver {
    private static final Logger      loggerParent = Logger.getLogger("dbvisitor.adapter");
    private static final Logger      logger       = Logger.getLogger("dbvisitor.adapter.driver");
    private static final ClassLoader classLoader  = JdbcDriver.class.getClassLoader();

    //
    public static final String P_SERVER       = "server";
    public static final String P_ADAPTER_NAME = "adapterName";
    public static final String P_START_URL    = "jdbc:dbvisitor:";
    //
    /** The major version of this adapter. */
    public static final String NAME           = "dbVisitor JDBC Adapter";
    /** The major version of this adapter. */
    public static final String VERSION        = "6.0";
    /** The major version of this adapter. */
    public static final int    VERSION_MAJOR  = 6;
    /** The minor version of this adapter. */
    public static final int    VERSION_MINOR  = 0;

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        return new JdbcConnection(url, parseURL(url, info), classLoader);
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        if (url == null) {
            throw new SQLException("driver jdbcUrl format error.", JdbcErrorCode.CODE_URL_FORMAT_ERROR);
        } else {
            return url.startsWith(P_START_URL);
        }
    }

    /**
     * Constructs a new DriverURL, splitting the specified URL into its component parts.
     *
     * @param url      JDBC URL to parse
     * @param defaults Default properties
     * @return Properties with elements added from the url
     */
    public static Properties parseURL(String url, Properties defaults) {
        String urlServer = url;
        String urlArgs = "";

        int qPos = url.indexOf('?');
        if (qPos != -1) {
            urlServer = url.substring(0, qPos);
            urlArgs = url.substring(qPos + 1);
        }

        if (!StringUtils.startsWith(urlServer, P_START_URL)) {
            logger.log(Level.FINE, "JDBC URL must start with \"jdbc:dbvisitor:xxx\" but was: {0}", url);
            return null;
        }
        urlServer = urlServer.substring(P_START_URL.length());
        if (urlServer.startsWith("//")) {
            logger.log(Level.FINE, "JDBC URL missing adapter is like \"jdbc:dbvisitor:name\" but was: {0}", url);
            return null;
        }

        // parse the args part of the url
        Properties properties = new Properties(defaults);
        Properties urlArgMap = new Properties();
        for (String cfgPart : StringUtils.split(urlArgs, "&")) {
            if (StringUtils.contains(cfgPart, "=")) {
                String k = StringUtils.substringBefore(cfgPart, "=").trim();
                String v = StringUtils.substringAfter(cfgPart, "=").trim();
                urlArgMap.put(k, v);
            }
        }

        // adapterName
        int adapterNameIdx = urlServer.indexOf("//");
        String adapterName = urlServer.substring(0, adapterNameIdx);
        adapterName = adapterName.endsWith(":") ? adapterName.substring(0, adapterName.length() - 1) : adapterName;
        properties.setProperty(P_ADAPTER_NAME, adapterName);

        // hosts
        String configUrl = urlServer.substring(adapterNameIdx + 2);
        if (StringUtils.isNotBlank(configUrl)) {
            properties.setProperty(P_SERVER, configUrl);
        } else {
            properties.putAll(urlArgMap);
        }

        return properties;
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
        Properties copy = new Properties(info);
        Properties parse = parseURL(url, copy);
        if (parse != null) {
            copy = parse;
        }

        String adapterName = copy.getProperty(P_ADAPTER_NAME);
        if (adapterName == null) {
            return new DriverPropertyInfo[0];
        }

        String[] knownProperties = AdapterManager.propertyNames(adapterName, copy, classLoader);
        DriverPropertyInfo[] props = new DriverPropertyInfo[knownProperties.length];
        for (int i = 0; i < props.length; ++i) {
            String name = knownProperties[i];
            String value = copy.getProperty(name);
            props[i] = new DriverPropertyInfo(name, value);
        }

        return props;
    }

    @Override
    public int getMajorVersion() {
        return VERSION_MAJOR;
    }

    @Override
    public int getMinorVersion() {
        return VERSION_MINOR;
    }

    @Override
    public boolean jdbcCompliant() {
        // Adapter is not SQL92 compliant.
        return false;
    }

    //------------------------- JDBC 4.1 -----------------------------------

    @Override
    public Logger getParentLogger() {
        return loggerParent;
    }
}
