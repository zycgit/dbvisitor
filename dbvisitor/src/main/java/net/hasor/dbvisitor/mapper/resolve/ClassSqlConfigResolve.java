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
package net.hasor.dbvisitor.mapper.resolve;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.xml.parsers.ParserConfigurationException;
import net.hasor.cobble.ExceptionUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dynamic.logic.ArrayDynamicSql;
import net.hasor.dbvisitor.dynamic.segment.PlanDynamicSql;
import net.hasor.dbvisitor.mapper.*;
import net.hasor.dbvisitor.mapper.def.*;
import net.hasor.dbvisitor.mapping.MappingHelper;
import net.hasor.dbvisitor.mapping.ResultMap;
import org.xml.sax.SAXException;

/**
 * 解析动态 SQL 配置（注解形式）
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-05
 */
public class ClassSqlConfigResolve implements SqlConfigResolve<Method>, ConfigKeys {

    public static boolean matchAnnotation(Annotation annotation) {
        return annotation instanceof Insert     //
                || annotation instanceof Delete //
                || annotation instanceof Update //
                || annotation instanceof Query  //
                || annotation instanceof Execute//
                || annotation instanceof Call   //
                || annotation instanceof Segment//
                ;
    }

    protected void parseSelectKey(InsertConfig parentConfig, Method dalMethod) {
        SelectKeySql keySql = dalMethod.getAnnotation(SelectKeySql.class);
        if (keySql != null) {
            Map<String, String> cfg = new HashMap<>();
            cfg.put(STATEMENT_TYPE, keySql.statementType().getTypeName());
            cfg.put(TIMEOUT, String.valueOf(keySql.timeout()));
            cfg.put(FETCH_SIZE, String.valueOf(keySql.fetchSize()));
            cfg.put(RESULT_SET_TYPE, keySql.resultSetType().getTypeName());
            cfg.put(KEY_PROPERTY, keySql.keyProperty());
            cfg.put(KEY_COLUMN, keySql.keyColumn());
            cfg.put(ORDER, keySql.order().name());

            ArrayDynamicSql dynamicSql = new ArrayDynamicSql();
            dynamicSql.addChildNode(new PlanDynamicSql(StringUtils.join(keySql.value(), " ")));
            parentConfig.setSelectKey(new SelectKeyConfig(dynamicSql, cfg::get));
        }
    }

    protected SqlConfig createDynamicSql(Method method, Annotation annotation) throws ParserConfigurationException, IOException, SAXException {
        Class<?> requiredClass = MappingHelper.resolveReturnType(method);
        if (annotation instanceof Insert) {
            Map<String, String> cfg = new HashMap<>();
            cfg.put(STATEMENT_TYPE, ((Insert) annotation).statementType().getTypeName());
            cfg.put(TIMEOUT, String.valueOf(((Insert) annotation).timeout()));
            cfg.put(KEY_GENERATED, String.valueOf(((Insert) annotation).useGeneratedKeys()));
            cfg.put(KEY_PROPERTY, ((Insert) annotation).keyProperty());
            cfg.put(KEY_COLUMN, ((Insert) annotation).keyColumn());

            ArrayDynamicSql dynamicSql = new ArrayDynamicSql();
            dynamicSql.addChildNode(new PlanDynamicSql(StringUtils.join(((Insert) annotation).value(), " ")));
            InsertConfig insertConfig = new InsertConfig(dynamicSql, cfg::get);
            this.parseSelectKey(insertConfig, method);
            return insertConfig;
        } else if (annotation instanceof Delete) {
            Map<String, String> cfg = new HashMap<>();
            cfg.put(STATEMENT_TYPE, ((Delete) annotation).statementType().getTypeName());
            cfg.put(TIMEOUT, String.valueOf(((Delete) annotation).timeout()));

            ArrayDynamicSql dynamicSql = new ArrayDynamicSql();
            dynamicSql.addChildNode(new PlanDynamicSql(StringUtils.join(((Delete) annotation).value(), " ")));
            return new DeleteConfig(dynamicSql, cfg::get);
        } else if (annotation instanceof Update) {
            Map<String, String> cfg = new HashMap<>();
            cfg.put(STATEMENT_TYPE, ((Update) annotation).statementType().getTypeName());
            cfg.put(TIMEOUT, String.valueOf(((Update) annotation).timeout()));

            ArrayDynamicSql dynamicSql = new ArrayDynamicSql();
            dynamicSql.addChildNode(new PlanDynamicSql(StringUtils.join(((Update) annotation).value(), " ")));
            return new UpdateConfig(dynamicSql, cfg::get);
        } else if (annotation instanceof Execute) {
            Map<String, String> cfg = new HashMap<>();
            cfg.put(STATEMENT_TYPE, ((Execute) annotation).statementType().getTypeName());
            cfg.put(TIMEOUT, String.valueOf(((Execute) annotation).timeout()));
            cfg.put(BIND_OUT, StringUtils.join(((Execute) annotation).bindOut(), ","));

            ArrayDynamicSql dynamicSql = new ArrayDynamicSql();
            dynamicSql.addChildNode(new PlanDynamicSql(StringUtils.join(((Execute) annotation).value(), " ")));
            return new ExecuteConfig(dynamicSql, cfg::get);
        } else if (annotation instanceof Call) {
            Map<String, String> cfg = new HashMap<>();
            cfg.put(STATEMENT_TYPE, StatementType.Callable.getTypeName());
            cfg.put(TIMEOUT, String.valueOf(((Call) annotation).timeout()));
            cfg.put(BIND_OUT, StringUtils.join(((Call) annotation).bindOut(), ","));

            ArrayDynamicSql dynamicSql = new ArrayDynamicSql();
            dynamicSql.addChildNode(new PlanDynamicSql(StringUtils.join(((Call) annotation).value(), " ")));
            return new ExecuteConfig(dynamicSql, cfg::get);
        } else if (annotation instanceof Query) {
            Map<String, String> cfg = new HashMap<>();
            cfg.put(STATEMENT_TYPE, ((Query) annotation).statementType().getTypeName());
            cfg.put(TIMEOUT, String.valueOf(((Query) annotation).timeout()));
            cfg.put(FETCH_SIZE, String.valueOf(((Query) annotation).fetchSize()));
            cfg.put(RESULT_SET_TYPE, ((Query) annotation).resultSetType().getTypeName());

            if (((Query) annotation).resultSetExtractor() != Object.class) {
                cfg.put(RESULT_SET_EXTRACTOR, ((Query) annotation).resultSetExtractor().getName());
            } else if (((Query) annotation).resultRowCallback() != Object.class) {
                cfg.put(RESULT_ROW_CALLBACK, ((Query) annotation).resultRowCallback().getName());
            } else if (((Query) annotation).resultRowMapper() != Object.class) {
                cfg.put(RESULT_ROW_MAPPER, ((Query) annotation).resultRowMapper().getName());
            } else if (method.isAnnotationPresent(ResultMap.class)) {
                MappingHelper.NameInfo nameInfo = MappingHelper.findNameInfo(method);
                if (nameInfo == null) {
                    throw new IllegalArgumentException("the @ResultMap annotation on the method(" + method + ") is incorrectly configured");
                }
                cfg.put(RESULT_MAP_SPACE, StringUtils.isBlank(nameInfo.getSpace()) ? method.getDeclaringClass().getName() : nameInfo.getSpace());
                cfg.put(RESULT_MAP_ID, nameInfo.getName());
            } else {
                cfg.put(RESULT_MAP_SPACE, method.getDeclaringClass().getName());
                cfg.put(RESULT_MAP_ID, null);
                cfg.put(RESULT_TYPE, MappingHelper.typeName(requiredClass));
            }

            cfg.put(BIND_OUT, StringUtils.join(((Query) annotation).bindOut(), ","));

            ArrayDynamicSql dynamicSql = new ArrayDynamicSql();
            dynamicSql.addChildNode(new PlanDynamicSql(StringUtils.join(((Query) annotation).value(), " ")));
            return new SelectConfig(dynamicSql, cfg::get);
        } else if (annotation instanceof Segment) {
            ArrayDynamicSql dynamicSql = new ArrayDynamicSql();
            dynamicSql.addChildNode(new PlanDynamicSql(StringUtils.join(((Segment) annotation).value(), " ")));
            return new SegmentConfig(dynamicSql, s -> null);
        } else {
            return null;
        }
    }

    @Override
    public SqlConfig parseSqlConfig(String namespace, Method method) {
        Objects.requireNonNull(method, "dalMethod is null.");
        for (Annotation anno : method.getAnnotations()) {
            if (matchAnnotation(anno)) {
                try {
                    return this.createDynamicSql(method, anno);
                } catch (ParserConfigurationException | IOException | SAXException e) {
                    throw ExceptionUtils.toRuntime(e);
                }
            }
        }
        return null;
    }
}