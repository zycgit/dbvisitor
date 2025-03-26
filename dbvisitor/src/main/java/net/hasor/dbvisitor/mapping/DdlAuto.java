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
package net.hasor.dbvisitor.mapping;
import net.hasor.cobble.StringUtils;

/**
 * DDL生成和执行规则
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-12-01
 */
public enum DdlAuto {
    /** 关闭该功能 */
    None(),
    /** 当表不存在会执行创建 */
    Create(),
    /** 仅会增列 */
    AddColumn(),
    /** 会更新表结构 */
    Update(),
    /** 每次启动应用都会删除表并重建它 */
    CreateDrop();

    public static DdlAuto valueOfCode(String code) {
        if (StringUtils.isNotBlank(code)) {
            for (DdlAuto ddlAuto : DdlAuto.values()) {
                if (StringUtils.equalsIgnoreCase(ddlAuto.name(), code)) {
                    return ddlAuto;
                }
            }

            switch (code.toLowerCase()) {
                case "none":
                    return DdlAuto.None;
                case "create":
                    return DdlAuto.Create;
                case "add":
                    return DdlAuto.AddColumn;
                case "update":
                    return DdlAuto.Update;
                case "create-drop":
                    return DdlAuto.CreateDrop;
            }

        }
        return DdlAuto.None;
    }
}