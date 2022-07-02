/*
 * Copyright 2002-2007 the original author or authors.
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
package net.hasor.jdbc.dao;
import org.noe.platform.modules.db.jdbcorm.jdbc.core.support.JdbcDaoSupport;
/**
 * Extension of {@link org.noe.platform.modules.db.jdbcorm.jdbc.core.support.JdbcDaoSupport}
 * that exposes a {@link #getSimpleJdbcTemplate() SimpleJdbcTemplate} as well.
 * Only usable on Java 5 and above.
 * 
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see SimpleJdbcTemplate
 */
public class SimpleJdbcDaoSupport extends JdbcDaoSupport {
    private SimpleJdbcTemplate simpleJdbcTemplate;
    /**
     * Create a SimpleJdbcTemplate based on the configured JdbcTemplate.
     */
    protected void initTemplateConfig() {
        this.simpleJdbcTemplate = new SimpleJdbcTemplate(getJdbcTemplate());
    }
    /**
     * Return a SimpleJdbcTemplate wrapping the configured JdbcTemplate.
     */
    public SimpleJdbcTemplate getSimpleJdbcTemplate() {
        return this.simpleJdbcTemplate;
    }
}
