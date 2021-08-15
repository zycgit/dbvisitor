/*
 * Copyright 2002-2010 the original author or authors.
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
package net.hasor.db.dialect;
import net.hasor.db.JdbcUtils;
import net.hasor.db.dialect.provider.*;
import net.hasor.cobble.ResourcesUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.cobble.supplier.TypeSupplier;

import java.util.Map;

/**
 * 方言管理器
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public class SqlDialectRegister {
    private static final Map<String, Class<?>>   dialectAliasMap = new LinkedCaseInsensitiveMap<>();
    private static final Map<String, SqlDialect> dialectCache    = new LinkedCaseInsensitiveMap<>();

    static {
        registerDialectAlias(JdbcUtils.ALI_ORACLE, OracleDialect.class);
        registerDialectAlias(JdbcUtils.DB2, Db2Dialect.class);
        registerDialectAlias(JdbcUtils.DERBY, SqlServerDialect.class);// Apache Derby
        registerDialectAlias(JdbcUtils.DM, DmDialect.class);
        registerDialectAlias(JdbcUtils.H2, H2Dialect.class);
        registerDialectAlias(JdbcUtils.HERDDB, HerdDBDialect.class);
        registerDialectAlias(JdbcUtils.HIVE, HiveDialect.class);
        registerDialectAlias(JdbcUtils.HSQL, HSQLDialect.class);
        registerDialectAlias(JdbcUtils.IMPALA, ImpalaDialect.class);
        registerDialectAlias(JdbcUtils.INFORMIX, InformixDialect.class);
        registerDialectAlias(JdbcUtils.KINGBASE, KingbaseDialect.class);
        registerDialectAlias(JdbcUtils.MARIADB, MariaDBDialect.class);
        registerDialectAlias(JdbcUtils.MYSQL, MySqlDialect.class);
        registerDialectAlias("oracle12c", Oracle12cDialect.class);
        registerDialectAlias(JdbcUtils.ORACLE, OracleDialect.class);
        registerDialectAlias(JdbcUtils.PHOENIX, PhoenixDialect.class);
        registerDialectAlias(JdbcUtils.POSTGRESQL, PostgreSqlDialect.class);
        registerDialectAlias(JdbcUtils.SQLITE, SqlLiteDialect.class);
        registerDialectAlias(JdbcUtils.SQL_SERVER, SqlServerDialect.class);
        registerDialectAlias("sqlserver2012", SqlServerDialect.class);
        registerDialectAlias("sqlserver2005", SqlServer2005Dialect.class);
        registerDialectAlias(JdbcUtils.XUGU, XuGuDialect.class);
    }

    public static void clearDialectCache() {
        dialectCache.clear();
    }

    public static void registerDialectAlias(String dialectName, Class<? extends SqlDialect> dialectClass) {
        dialectAliasMap.put(dialectName, dialectClass);
    }

    public static SqlDialect findOrCreate(String dialectName) {
        return findOrCreate(dialectName, null, null);
    }

    public static SqlDialect findOrCreate(String dialectName, TypeSupplier typeSupplier) {
        return findOrCreate(dialectName, null, typeSupplier);
    }

    public static SqlDialect findOrCreate(final String dialectName, ClassLoader loader, TypeSupplier typeSupplier) {
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
        //
        if (aClass != null) {
            if (typeSupplier != null) {
                try {
                    dialect = (SqlDialect) typeSupplier.get(aClass);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            } else {
                try {
                    dialect = (SqlDialect) aClass.newInstance();
                } catch (Exception e) {
                    throw new IllegalStateException("load dialect '" + aClass.getName() + "' failed, " + e.getMessage(), e);
                }
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
}
