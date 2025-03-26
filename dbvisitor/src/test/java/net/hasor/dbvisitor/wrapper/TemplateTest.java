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
package net.hasor.dbvisitor.wrapper;
import net.hasor.dbvisitor.dialect.BatchBoundSql;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.provider.MySqlDialect;
import net.hasor.dbvisitor.dynamic.MacroRegistry;
import net.hasor.dbvisitor.dynamic.RuleRegistry;
import net.hasor.dbvisitor.jdbc.core.JdbcQueryContext;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import net.hasor.dbvisitor.wrapper.dto.PointTable;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-3-22
 */
public class TemplateTest {
    private static Map<String, Object> mapForData1() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", 1);
        map.put("point", "point(12,12)");
        return map;
    }

    private WrapperAdapter newLambda() throws SQLException {
        Options opt = Options.of().dialect(new MySqlDialect());
        JdbcQueryContext context = new JdbcQueryContext();
        context.setTypeRegistry(new TypeHandlerRegistry());
        context.setMacroRegistry(new MacroRegistry());
        context.setRuleRegistry(new RuleRegistry());
        MappingRegistry registry = new MappingRegistry(null, context.getTypeRegistry(), opt);

        return new WrapperAdapter((DataSource) null, registry, context);
    }

    @Test
    public void insert_1() throws SQLException {
        InsertWrapper<PointTable> lambdaInsert = newLambda().insert(PointTable.class);
        lambdaInsert.applyMap(mapForData1());

        BoundSql boundSql1 = lambdaInsert.getBoundSql();
        assert boundSql1 instanceof BatchBoundSql;
        assert boundSql1.getSqlString().equals("INSERT INTO point_table (id, point) VALUES (?, GeomFromText(?))");
        assert ((Object[]) boundSql1.getArgs()[0])[0].equals(1);
        assert ((Object[]) boundSql1.getArgs()[0])[1].equals("point(12,12)");
    }

    @Test
    public void update_1() throws SQLException {
        PointTable data = new PointTable();
        data.setPoint("point(22,22)");
        EntityUpdateWrapper<PointTable> lambdaUpdate = new WrapperAdapter().update(PointTable.class);
        lambdaUpdate.and(qb -> qb.eq(PointTable::getId, 1)).updateToSample(data);

        BoundSql boundSql1 = lambdaUpdate.getBoundSql();
        assert !(boundSql1 instanceof BatchBoundSql);
        assert boundSql1.getSqlString().equals("UPDATE point_table SET point = GeomFromText(?) WHERE ( id = ? )");
        assert boundSql1.getArgs()[0].equals("point(22,22)");
        assert boundSql1.getArgs()[1].equals(1);
    }

    @Test
    public void update_2() throws SQLException {
        PointTable data = new PointTable();
        data.setPoint("point(22,22)");
        EntityUpdateWrapper<PointTable> lambdaUpdate = new WrapperAdapter().update(PointTable.class);
        lambdaUpdate.and(qb -> qb.eq(PointTable::getPoint, "point(11,11)")).updateToSample(data);

        BoundSql boundSql1 = lambdaUpdate.getBoundSql();
        assert !(boundSql1 instanceof BatchBoundSql);
        assert boundSql1.getSqlString().equals("UPDATE point_table SET point = GeomFromText(?) WHERE ( AsText(point) = ? )");
        assert boundSql1.getArgs()[0].equals("point(22,22)");
        assert boundSql1.getArgs()[1].equals("point(11,11)");
    }

    @Test
    public void delete_1() throws SQLException {
        EntityDeleteWrapper<PointTable> lambdaDelete = new WrapperAdapter().delete(PointTable.class);
        lambdaDelete.and(qb -> qb.eq(PointTable::getPoint, "point(11,11)"));

        BoundSql boundSql1 = lambdaDelete.getBoundSql();
        assert !(boundSql1 instanceof BatchBoundSql);
        assert boundSql1.getSqlString().equals("DELETE FROM point_table WHERE ( AsText(point) = ? )");
        assert boundSql1.getArgs()[0].equals("point(11,11)");
    }

    @Test
    public void select_1() throws SQLException {
        EntityQueryWrapper<PointTable> lambdaQuery = new WrapperAdapter().query(PointTable.class);
        lambdaQuery.select(PointTable::getId, PointTable::getPoint).and(qb -> qb.eq(PointTable::getPoint, "point(11,11)"));

        BoundSql boundSql1 = lambdaQuery.getBoundSql();
        assert !(boundSql1 instanceof BatchBoundSql);
        assert boundSql1.getSqlString().equals("SELECT id , AsText(point) FROM point_table WHERE ( AsText(point) = ? )");
        assert boundSql1.getArgs()[0].equals("point(11,11)");
    }
}
