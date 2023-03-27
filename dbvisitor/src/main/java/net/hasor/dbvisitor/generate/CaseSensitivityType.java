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
package net.hasor.dbvisitor.generate;
/**
 * 大小写敏感类别
 * @version : 2023-03-13
 * @author 赵永春 (zyc@hasor.net)
 */
public enum CaseSensitivityType {

    Upper("UPPER"),

    Lower("LOWER"),

    /** complete same */
    Exact("EXACT"),

    /** case not sensitive */
    Fuzzy("FUZZY");

    private final String typeName;

    CaseSensitivityType(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return this.typeName;
    }

    public static CaseSensitivityType valueOfCode(String code) {
        for (CaseSensitivityType constraintType : CaseSensitivityType.values()) {
            if (constraintType.typeName.equalsIgnoreCase(code)) {
                return constraintType;
            }
            if (constraintType.name().equalsIgnoreCase(code)) {
                return constraintType;
            }
        }
        throw new UnsupportedOperationException("Unsupported CaseSensitivity Type " + code);
    }
}
