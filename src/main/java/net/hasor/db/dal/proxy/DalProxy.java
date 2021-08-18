package net.hasor.db.dal.proxy;
import net.hasor.db.dal.dynamic.DynamicSql;
import net.hasor.db.dal.execute.DalExecute;
import net.hasor.db.dal.repository.MapperRegistry;
import net.hasor.db.jdbc.core.JdbcTemplate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

class DalProxy implements InvocationHandler {
    private Class<?>                dalType;
    private JdbcTemplate            jdbcTemplate;
    private DalExecute              dalExecute;
    private Map<String, DynamicSql> dynamicSqlMap;

    public DalProxy(Class<?> dalType) {
        this.dalType = dalType;
        for (Method method : dalType.getMethods()) {
            DynamicSql parseXml = MapperRegistry.DEFAULT.findDynamicSql(dalType, method.getName());
            dynamicSqlMap.put(method.getName(), parseXml);
        }
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        //@Param
        Map<String, Object> context = new HashMap<>();
        DynamicSql dynamicSql = dynamicSqlMap.get(method.getName());
        // this.dalExecute.execute(this.jdbcTemplate, dynamicSql, context, null);
        return null;
    }
}
