/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.annotation;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class AnnotationMapperTextValueCase extends AnnotationMapperBoundarySupport {
    // 能力归属：参数传递与规则 / 名称参数 / 方法注解。
    @Test
    @Capability(value = CapabilityId.MAPPER_ANNOTATION_SPECIAL_TEXT, column = "parameters/positional-and-named-parameters/named")
    public void annotationMapperInsert_shouldBindQuotesAndUnicodeAsValues() throws Exception {
        int quotedId = baseId() + 2;
        int unicodeId = baseId() + 3;

        assertEquals(1, this.mapper.insertUserWithParams(quotedId, "O'Brien & Co.", 28, "quoted@nxn.test"));
        assertEquals(1, this.mapper.insertUserWithParams(unicodeId, "测试用户 テスト", 25, "unicode@nxn.test"));

        assertEquals("O'Brien & Co.", this.mapper.selectById(quotedId).getName());
        assertEquals("测试用户 テスト", this.mapper.selectById(unicodeId).getName());
    }
}
