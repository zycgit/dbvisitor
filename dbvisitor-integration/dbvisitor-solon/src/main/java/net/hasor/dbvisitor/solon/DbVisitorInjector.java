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
package net.hasor.dbvisitor.solon;
import net.hasor.cobble.ExceptionUtils;
import net.hasor.dbvisitor.jdbc.JdbcOperations;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.wrapper.WrapperAdapter;
import net.hasor.dbvisitor.wrapper.WrapperOperations;
import org.noear.solon.core.BeanInjector;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.VarHolder;
import org.noear.solon.data.datasource.DsUtils;

import java.util.Map;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2025-03-20
 */
public class DbVisitorInjector implements BeanInjector<Db> {
    private final Map<String, MapperWrap> mapperWrap;

    public DbVisitorInjector(Map<String, MapperWrap> mapperWrap) {
        this.mapperWrap = mapperWrap;
    }

    @Override
    public void doInject(VarHolder vh, Db anno) {
        vh.required(true);

        DsUtils.observeDs(vh.context(), anno.value(), (dsWrap) -> {
            inject0(vh, dsWrap, this.mapperWrap.get(anno.value()));
        });
    }

    private void inject0(VarHolder vh, BeanWrap dsBw, MapperWrap mapperWrap) {
        try {
            inject1(vh, dsBw, mapperWrap);
        } catch (Exception e) {
            throw ExceptionUtils.toRuntime(e);
        }
    }

    private void inject1(VarHolder vh, BeanWrap dsBw, MapperWrap mapperWrap) throws Exception {
        final Class<?> clz = vh.getType();
        final Session dbvSession = DsHelper.fetchSession(dsBw, mapperWrap);

        //@Db("db1") JdbcOperations
        if (JdbcOperations.class.isAssignableFrom(clz)) {
            vh.setValue(dbvSession.jdbc());
            return;
        }

        //@Db("db1") JdbcTemplate
        if (JdbcTemplate.class.isAssignableFrom(clz)) {
            vh.setValue(dbvSession.jdbc());
            return;
        }

        //@Db("db1") WrapperOperations
        if (WrapperOperations.class.isAssignableFrom(clz)) {
            vh.setValue(dbvSession.wrapper());
            return;
        }

        //@Db("db1") WrapperAdapter
        if (WrapperAdapter.class.isAssignableFrom(clz)) {
            vh.setValue(dbvSession.wrapper());
            return;
        }

        //@Db("db1") Configuration
        if (Configuration.class.isAssignableFrom(clz)) {
            vh.setValue(dbvSession.getConfiguration());
            return;
        }

        //@Db("db1") Session
        if (Session.class.isAssignableFrom(clz)) {
            vh.setValue(dbvSession);
            return;
        }

        //@Db("db1") UserMapper
        if (clz.isInterface()) {
            if (clz == BaseMapper.class) {
                Object mapper = dbvSession.createBaseMapper((Class<?>) vh.getGenericType().getActualTypeArguments()[0]);
                vh.setValue(mapper);
            } else {
                Object mapper = dbvSession.createMapper(clz);
                vh.setValue(mapper);
            }
        }

        //@Db("db1") TransactionManager
        //if (TransactionManager.class.isAssignableFrom(clz)) {
        //    vh.setValue(tranManager);
        //    return;
        //}

        //@Db("db1") TransactionTemplate
        //if (TransactionTemplate.class.isAssignableFrom(clz)) {
        //    vh.setValue(tranTemplate);
        //    return;
        //}
    }
}
