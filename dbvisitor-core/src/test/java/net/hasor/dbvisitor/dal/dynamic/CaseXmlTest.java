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
package net.hasor.dbvisitor.dal.dynamic;
import net.hasor.cobble.ResourcesUtils;
import net.hasor.cobble.io.IOUtils;
import net.hasor.dbvisitor.dialect.SqlBuilder;
import net.hasor.test.dal.dynamic.TextBuilderContext;
import net.hasor.test.dto.CharacterSensitiveEnum;
import net.hasor.test.dto.LicenseOfCodeEnum;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

public class CaseXmlTest {
    private final DynamicParser xmlParser = new DynamicParser();

    private String loadString(String queryConfig) throws IOException {
        return IOUtils.readToString(ResourcesUtils.getResourceAsStream(queryConfig), "UTF-8");
    }

    @Test
    public void caseTest_01() throws Throwable {
        String queryConfig = loadString("/net_hasor_db/dal_dynamic/fragment/testcase/case_01.xml");
        DynamicSql parseXml = xmlParser.parseDynamicSql(queryConfig);
        //
        String querySql1 = loadString("/net_hasor_db/dal_dynamic/fragment/testcase/case_01.xml.sql_1");
        Map<String, Object> data1 = new HashMap<>();
        data1.put("startId", "33322");
        data1.put("label", new ArrayList<>(Arrays.asList(LicenseOfCodeEnum.Private, LicenseOfCodeEnum.GPLv3)));
        data1.put("state", new ArrayList<>(Collections.singletonList(CharacterSensitiveEnum.A)));
        data1.put("consoleJobId", "123");
        SqlBuilder builder1 = parseXml.buildQuery(data1, new TextBuilderContext());
        assert builder1.getSqlString().trim().equals(querySql1.trim());
        assert ((SqlArg) builder1.getArgs()[0]).getValue().equals("33322");
        assert ((SqlArg) builder1.getArgs()[1]).getValue().equals(LicenseOfCodeEnum.Private);
        assert ((SqlArg) builder1.getArgs()[2]).getValue().equals(LicenseOfCodeEnum.GPLv3);
        assert ((SqlArg) builder1.getArgs()[3]).getValue().equals(CharacterSensitiveEnum.A);
        assert ((SqlArg) builder1.getArgs()[4]).getValue().equals("123");
        assert ((SqlArg) builder1.getArgs()[5]).getValue() == null;
    }
}
