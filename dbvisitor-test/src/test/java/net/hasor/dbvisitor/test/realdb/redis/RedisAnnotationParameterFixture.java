/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis;

import java.sql.SQLException;
import java.util.UUID;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.ParameterBindingMapper;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.realdb.redis.dto1.RedisParameterBindingMapper;

/** JSON fields may contain null; this fixture does not claim Redis stores a root SQL NULL. */
public final class RedisAnnotationParameterFixture implements AutoCloseable {
    private final String       keyPrefix = "nxn_anno_params_" + UUID.randomUUID().toString().replace("-", "") + ":";
    private       JdbcTemplate jdbc;
    private       Session      session;

    public JdbcTemplate open() throws SQLException {
        if (this.jdbc == null) {
            OneApiDataSourceManager.assumeCurrentDataSource("redis");
            this.jdbc = new JdbcTemplate(OneApiDataSourceManager.getConnection("redis"));
        }
        return this.jdbc;
    }

    public ParameterBindingMapper createMapper(Configuration configuration) throws Exception {
        return createMapper(configuration, RedisParameterBindingMapper.class);
    }

    public ParameterBindingMapper createMapper(Configuration configuration, Class<? extends ParameterBindingMapper> mapperType) throws Exception {
        configuration.addMacro("nxnInsertNamed", insert("id", "'id': id, 'name': name, 'age': age, 'email': email"));
        configuration.addMacro("nxnInsertBean", insert("id", "'id': id, 'name': name, 'age': age, 'email': email, 'createTime': createTime"));
        configuration.addMacro("nxnInsertMixed", insert("user.id", "'id': user.id, 'name': user.name, 'age': user.age, 'email': email"));
        configuration.addMacro("nxnInsertReuse", insert("id", "'id': id, 'name': name, 'age': 25, 'email': name"));
        configuration.addMacro("nxnSelectId", "GET #{'" + this.keyPrefix + "' + id}");
        configuration.addMacro("nxnInsertPosition", "EVAL \"redis.call('SET','" + this.keyPrefix + "'..ARGV[1],cjson.encode({id=tonumber(ARGV[1]),name=ARGV[2],age=tonumber(ARGV[3])})); return 1\" 0 ? ? ?");
        configuration.addMacro("nxnUpdatePosition", "EVAL \"local key='" + this.keyPrefix + "'..ARGV[2]; local value=redis.call('GET',key); if not value then return 0 end; local user=cjson.decode(value); user.age=tonumber(ARGV[1]); redis.call('SET',key,cjson.encode(user)); return 1\" 0 ? ?");
        configuration.addMacro("nxnRangeInsert", "ZADD '" + this.keyPrefix + "ages' #{age} " + "#{#{'id':id,'name':name,'age':age,'email':email}, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler}");
        configuration.addMacro("nxnSelectRange", "ZRANGEBYSCORE '" + this.keyPrefix + "ages' #{minAge} #{maxAge}");
        this.session = configuration.newSession(this.jdbc.getConnection());
        return this.session.createMapper(mapperType);
    }

    private String insert(String idExpression, String entries) {
        // The real expression engine reads @Param / Bean / Map properties; the built-in handler serializes values.
        return "SET #{'" + this.keyPrefix + "' + " + idExpression + "} " + "#{#{" + entries + "}, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler}";
    }

    @Override
    public void close() throws Exception {
        try {
            if (this.jdbc != null) {
                for (String key : this.jdbc.queryForList("KEYS ?", this.keyPrefix + "*", String.class)) {
                    this.jdbc.executeUpdate("DEL ?", key);
                }
            }
        } finally {
            if (this.session != null) {
                this.session.close();
            } else if (this.jdbc != null) {
                this.jdbc.getConnection().close();
            }
        }
    }
}
