/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.type;

import java.sql.SQLException;
import java.util.Date;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.contract.material.model.annotation.JdbcTypeUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.ReadTypeHandlerUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.SpecialJavaTypeUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.TypeHandlerUser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class AnnotationTypeHandlerCase extends AbstractNxnContractTest {
    private LambdaTemplate lambda;

    @Before
    public void createLambdaTemplate() throws SQLException {
        this.lambda = mappingLambdaTemplate();
    }

    protected LambdaTemplate mappingLambdaTemplate() throws SQLException {
        return new LambdaTemplate(this.jdbcTemplate);
    }

    protected int baseId() {
        return 940000;
    }

    // 能力归属：类型处理器 / 自定义类型处理器 / 实体字段转换。
    @Test
    @Capability(value = CapabilityId.MAPPING_ANNOTATION_TYPE_HANDLER_INSERT, column = "types/custom-type-handlers/entity-fields")
    public void columnAnnotation_shouldApplyCustomTypeHandlerOnInsert() throws SQLException {
        TypeHandlerUser user = new TypeHandlerUser();
        user.setId(baseId() + 1);
        user.setName("lowercase_name");
        user.setAge(25);
        user.setEmail("type-handler@nxn.test");
        user.setCreateTime(new Date());

        this.lambda.insert(TypeHandlerUser.class).applyEntity(user).executeSumResult();

        UserInfo raw = this.lambda.query(UserInfo.class) //
                .eq(UserInfo::getId, baseId() + 1) //
                .queryForObject();
        assertNotNull(raw);
        assertEquals("LOWERCASE_NAME", raw.getName());
    }

    // 能力归属：类型处理器 / 自定义处理器 / 实体查询使用自定义读取转换。
    @Test
    @Capability(value = CapabilityId.MAPPING_ANNOTATION_TYPE_HANDLER_READ, column = "types/custom-type-handlers/entity-fields")
    public void columnAnnotation_shouldApplyCustomTypeHandlerOnRead() throws SQLException {
        int id = baseId() + 31;
        UserInfo raw = new UserInfo();
        raw.setId(id);
        raw.setName("stored_value");
        raw.setAge(32);
        raw.setEmail("read-handler@nxn.test");
        this.lambda.insert(UserInfo.class).applyEntity(raw).executeSumResult();

        ReadTypeHandlerUser loaded = this.lambda.query(ReadTypeHandlerUser.class)//
                .eq(ReadTypeHandlerUser::getId, id)//
                .queryForObject();
        assertNotNull(loaded);
        assertEquals(Integer.valueOf(id), loaded.getId());
        assertEquals("read:stored_value", loaded.getName());
        assertEquals("stored_value", this.lambda.query(UserInfo.class).eq(UserInfo::getId, id).queryForObject().getName());
    }

    // 能力归属：类型处理器 / 自定义处理器 / 字段更新使用自定义写入转换。
    @Test
    @Capability(value = CapabilityId.MAPPING_ANNOTATION_TYPE_HANDLER_UPDATE, column = "types/custom-type-handlers/entity-fields")
    public void columnAnnotation_shouldApplyCustomTypeHandlerOnUpdate() throws SQLException {
        int id = baseId() + 32;
        TypeHandlerUser user = new TypeHandlerUser();
        user.setId(id);
        user.setName("original_name");
        user.setAge(33);
        user.setEmail("update-handler@nxn.test");
        this.lambda.insert(TypeHandlerUser.class).applyEntity(user).executeSumResult();

        int rows = this.lambda.update(TypeHandlerUser.class)//
                .eq(TypeHandlerUser::getId, id)//
                .updateTo(TypeHandlerUser::getName, "updated_name")//
                .doUpdate();
        UserInfo raw = this.lambda.query(UserInfo.class).eq(UserInfo::getId, id).queryForObject();
        assertEquals(1, rows);
        assertNotNull(raw);
        assertEquals("UPDATED_NAME", raw.getName());
        assertEquals(Integer.valueOf(33), raw.getAge());
        assertEquals("update-handler@nxn.test", raw.getEmail());
    }

    // 能力归属：类型处理器 / 自定义类型处理器 / 实体字段转换。
    @Test
    @Capability(value = CapabilityId.MAPPING_ANNOTATION_JDBC_TYPE_ROUND_TRIP, column = "types/custom-type-handlers/entity-fields")
    public void columnAnnotation_shouldUseJdbcTypeDuringRoundTrip() throws SQLException {
        JdbcTypeUser user = new JdbcTypeUser();
        user.setId(baseId() + 11);
        user.setName("JdbcTypeUser");
        user.setAge(30);
        user.setEmail("jdbc-type@nxn.test");

        this.lambda.insert(JdbcTypeUser.class).applyEntity(user).executeSumResult();

        JdbcTypeUser loaded = this.lambda.query(JdbcTypeUser.class) //
                .eq(JdbcTypeUser::getId, baseId() + 11) //
                .queryForObject();
        assertNotNull(loaded);
        assertEquals("JdbcTypeUser", loaded.getName());
        assertEquals(Integer.valueOf(30), loaded.getAge());
    }

    // 能力归属：类型处理器 / 自定义类型处理器 / 实体字段转换。
    @Test
    @Capability(value = CapabilityId.MAPPING_ANNOTATION_SPECIAL_JAVA_TYPE_ROUND_TRIP, column = "types/custom-type-handlers/entity-fields")
    public void columnAnnotation_shouldUseSpecialJavaTypeDuringRoundTrip() throws SQLException {
        SpecialJavaTypeUser user = new SpecialJavaTypeUser();
        user.setId(baseId() + 21);
        user.setName("SpecialType");
        user.setAge(28);

        this.lambda.insert(SpecialJavaTypeUser.class).applyEntity(user).executeSumResult();

        SpecialJavaTypeUser loaded = this.lambda.query(SpecialJavaTypeUser.class) //
                .eq(SpecialJavaTypeUser::getId, baseId() + 21) //
                .queryForObject();
        assertNotNull(loaded);
        assertEquals("SpecialType", loaded.getName().toString());
        assertEquals(String.class, loaded.getName().getClass());
    }
}
