/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.scenario;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.assertTrue;

public abstract class RedisScenarioSupport {
    private final String prefix = "nxn:redis:scenario:" + UUID.randomUUID() + ":";
    private final List<String> keys = new ArrayList<>();
    protected Session session;
    private JdbcTemplate jdbc;

    @Before
    public void open() throws Exception {
        OneApiDataSourceManager.assumeCurrentDataSource("redis");
        this.session = new Configuration().newSession(OneApiDataSourceManager.getConnection("redis"));
        this.jdbc = new JdbcTemplate(this.session.getConnection());
    }

    protected String key(String suffix) {
        String key = this.prefix + suffix;
        this.keys.add(key);
        return key;
    }

    protected void assertTtl(String key, int maximum) throws Exception {
        assertTtl(key, 0, maximum);
    }

    protected void assertTtl(String key, int minimum, int maximum) throws Exception {
        Long ttl = this.jdbc.queryForLong("TTL ?", key);
        assertTrue("TTL must be greater than " + minimum + " and at most " + maximum + ", got " + ttl,
                ttl != null && ttl > minimum && ttl <= maximum);
    }

    @After
    public void close() throws Exception {
        try {
            if (this.jdbc != null) {
                for (String key : this.keys) {
                    this.jdbc.executeUpdate("DEL ?", key);
                }
            }
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
    }
}
