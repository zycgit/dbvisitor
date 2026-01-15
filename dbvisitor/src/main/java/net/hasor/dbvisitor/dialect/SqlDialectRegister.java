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
package net.hasor.dbvisitor.dialect;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;
import net.hasor.cobble.ResourcesUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.dialect.provider.*;
import net.hasor.dbvisitor.jdbc.JdbcHelper;
import net.hasor.dbvisitor.mapping.Options;

/**
 * SQL 方言注册管理器
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public class SqlDialectRegister {
    private static final Logger                  log             = Logger.getLogger(SqlDialectRegister.class);
    private static final Map<String, Class<?>>   dialectAliasMap = new LinkedCaseInsensitiveMap<>();
    private static final Map<String, SqlDialect> dialectCache    = new LinkedCaseInsensitiveMap<>();

    static {
        registerDialectAlias(JdbcHelper.DB2, Db2Dialect.class);
        registerDialectAlias(JdbcHelper.DERBY, DerbyDialect.class);// Apache Derby
        registerDialectAlias(JdbcHelper.DM, DmDialect.class);
        registerDialectAlias(JdbcHelper.H2, H2Dialect.class);
        registerDialectAlias(JdbcHelper.HIVE, HiveDialect.class);
        registerDialectAlias(JdbcHelper.HSQL, HSQLDialect.class);
        registerDialectAlias(JdbcHelper.IMPALA, ImpalaDialect.class);
        registerDialectAlias(JdbcHelper.INFORMIX, InformixDialect.class);
        registerDialectAlias(JdbcHelper.KINGBASE, PostgreSqlDialect.class);
        registerDialectAlias(JdbcHelper.POSTGRESQL, PostgreSqlDialect.class);
        registerDialectAlias(JdbcHelper.MARIADB, MySqlDialect.class);
        registerDialectAlias(JdbcHelper.MYSQL, MySqlDialect.class);
        registerDialectAlias(JdbcHelper.ORACLE, OracleDialect.class);
        registerDialectAlias(JdbcHelper.SQLITE, SqlLiteDialect.class);
        registerDialectAlias(JdbcHelper.SQL_SERVER, SqlServerDialect.class);
        registerDialectAlias(JdbcHelper.JTDS, SqlServerDialect.class);
        registerDialectAlias(JdbcHelper.XUGU, XuGuDialect.class);
        //registerDialectAlias(JdbcHelper.REDIS, RedisDialect.class);
        registerDialectAlias(JdbcHelper.MONGO, MongoDialect.class);
        registerDialectAlias(JdbcHelper.ELASTIC6, Elastic6Dialect.class);
        registerDialectAlias(JdbcHelper.ELASTIC7, Elastic7Dialect.class);
    }

    /** 清空方言缓存 */
    public static void clearDialectCache() {
        dialectCache.clear();
    }

    /**
     * 注册方言别名
     * @param dialectName 方言名称
     * @param dialectClass 方言类
     */
    public static void registerDialectAlias(String dialectName, Class<? extends SqlDialect> dialectClass) {
        dialectAliasMap.put(dialectName, dialectClass);
        dialectAliasMap.put(dialectClass.getName(), dialectClass);
    }

    /**
     * 查找或创建方言实例（使用默认类加载器）
     * @param dialectName 方言名称或类名
     * @return 方言实例
     */
    public static SqlDialect findOrCreate(String dialectName) {
        return findOrCreate(dialectName, null);
    }

    /**
     * 查找或创建方言实例
     * @param dialectName 方言名称或类名
     * @param loader 类加载器
     * @return 方言实例
     */
    public static SqlDialect findOrCreate(String dialectName, ClassLoader loader) {
        if (StringUtils.isBlank(dialectName)) {
            return DefaultSqlDialect.DEFAULT;
        }
        SqlDialect dialect = dialectCache.get(dialectName);
        if (dialect != null) {
            return dialect;
        }
        //
        loader = (loader == null) ? Thread.currentThread().getContextClassLoader() : loader;
        String lastMessage = null;
        Class<?> aClass = dialectAliasMap.get(dialectName);
        if (aClass == null) {
            try {
                aClass = ResourcesUtils.classForName(loader, dialectName);
            } catch (ClassNotFoundException e) {
                lastMessage = "load dialect '" + dialectName + "' class not found";
            }
        }
        if (aClass != null) {
            try {
                dialect = (SqlDialect) aClass.newInstance();
            } catch (Exception e) {
                throw new IllegalStateException("load dialect '" + aClass.getName() + "' failed, " + e.getMessage(), e);
            }
        } else {
            if (StringUtils.isNotBlank(lastMessage)) {
                throw new IllegalStateException(lastMessage);
            } else {
                throw new IllegalStateException("no dialect '" + dialectName + "' found.");
            }
        }
        //
        dialectCache.put(dialectName, dialect);
        return dialect;
    }

    private static SqlDialect getDialectFromOptions(Options option) {
        if (option != null && option.getDialect() != null) {
            return option.getDialect();
        }
        return null;
    }

    /**
     * 查找配置中的方言或返回默认方言
     * @param option 配置选项
     * @return 方言实例
     */
    public static SqlDialect findOrDefault(Options option) {
        SqlDialect dialect = getDialectFromOptions(option);
        if (dialect != null) {
            return dialect;
        } else {
            log.warn("No SQL dialect is specified, the default dialect is selected");
            return DefaultSqlDialect.DEFAULT;
        }
    }

    /**
     * 根据连接查找分页方言
     * @param conn 数据库连接
     * @return 分页方言实例
     * @throws SQLException 如果获取元数据失败
     */
    public static SqlDialect findDialect(Options option, Connection conn) throws SQLException {
        SqlDialect dialect = getDialectFromOptions(option);
        if (dialect != null) {
            return dialect;
        }

        if (conn != null) {
            DatabaseMetaData metaData = conn.getMetaData();
            String dbVersion = metaData.getDatabaseProductVersion();
            String tmpDbType = JdbcHelper.getDbType(metaData.getURL(), metaData.getDriverName(), dbVersion);
            SqlDialect tempDialect = SqlDialectRegister.findOrCreate(tmpDbType);
            if (tempDialect != null) {
                return tempDialect;
            }
        }

        log.warn("No SQL dialect is specified, the default dialect is selected");
        return DefaultSqlDialect.DEFAULT;
    }
}
