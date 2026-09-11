/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapping;
import net.hasor.cobble.StringUtils;

/**
 * DDL生成和执行规则
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-12-01
 */
public enum DdlAuto {
    /** 关闭所有 DDL 自动执行功能 */
    None,
    /** 启动时自动创建不存在的表结构 */
    Create,
    /** 仅自动添加新增的列 */
    AddColumn,
    /** 全量更新表结构（包含新增列、修改列等操作） */
    Update,
    /** 启动时先删除表再重建（适用于开发环境） */
    CreateDrop;

    /**
     * 将字符串转换为对应的枚举值（支持名称匹配和别名转换）
     * @param code 策略名称或别名（不区分大小写）
     * @return 匹配的枚举值，未找到时返回None
     */
    public static DdlAuto valueOfCode(String code) {
        if (StringUtils.isNotBlank(code)) {
            for (DdlAuto ddlAuto : DdlAuto.values()) {
                if (StringUtils.equalsIgnoreCase(ddlAuto.name(), code)) {
                    return ddlAuto;
                }
            }

            return switch (code.toLowerCase()) {
                case "none" -> DdlAuto.None;
                case "create" -> DdlAuto.Create;
                case "add" -> DdlAuto.AddColumn;
                case "update" -> DdlAuto.Update;
                case "create-drop" -> DdlAuto.CreateDrop;
                default -> DdlAuto.None;
            };

        }
        return DdlAuto.None;
    }
}
