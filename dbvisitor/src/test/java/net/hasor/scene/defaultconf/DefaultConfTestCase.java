package net.hasor.scene.defaultconf;
import net.hasor.dbvisitor.lambda.EntityQueryOperation;
import net.hasor.dbvisitor.lambda.InsertOperation;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.lambda.MapQueryOperation;
import net.hasor.dbvisitor.mapping.MappingOptions;
import net.hasor.scene.defaultconf.dto.UserDTO;
import net.hasor.scene.defaultconf.dto.UserTable;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DefaultConfTestCase {
    private static final MappingOptions options = MappingOptions.buildNew().mapUnderscoreToCamelCase(true);

    // 普通 pojo
    @Test
    public void insertByPojo() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            UserTable userData = new UserTable();
            userData.setId(128);
            userData.setAge(36);
            userData.setName("default user");
            userData.setCreateTime(new Date());// 驼峰由 @TableDefault 进行启用

            InsertOperation<UserTable> lambdaInsert = lambdaTemplate.insertByEntity(UserTable.class);
            assert 1 == lambdaInsert.applyEntity(userData).executeSumResult();

            // 校验结果
            EntityQueryOperation<UserTable> lambdaQuery = lambdaTemplate.queryByEntity(UserTable.class);
            UserTable resultData = lambdaQuery.eq(UserTable::getId, 128).queryForObject();

            assert resultData.getId() == 128;
            assert resultData.getAge() == 36;
            assert resultData.getName().equals("default user");
            assert resultData.getCreateTime() != null;
        }
    }

    // 普通 DTO
    @Test
    public void insertByDTO() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            UserDTO userData = new UserDTO();
            userData.setId(128);
            userData.setAge(36);
            userData.setName("default user");
            userData.setCreateTime(new Date());// 驼峰由 @TableDefault 进行启用

            InsertOperation<UserDTO> lambdaInsert = lambdaTemplate.insertByEntity(UserDTO.class);
            assert 1 == lambdaInsert.applyEntity(userData).executeSumResult();

            // 校验结果
            EntityQueryOperation<UserDTO> lambdaQuery = lambdaTemplate.queryByEntity(UserDTO.class);
            UserDTO resultData = lambdaQuery.eq(UserDTO::getId, 128).queryForObject();

            assert resultData.getId() == 128;
            assert resultData.getAge() == 36;
            assert resultData.getName().equals("default user");
            assert resultData.getCreateTime() != null;
        }
    }

    // 普通 map,没有映射 key 为列名
    @Test
    public void insertByMap() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", 128);
            userData.put("age", 36);
            userData.put("name", "default user");
            userData.put("create_time", new Date());// 默认驼峰转换是关闭的，因此在没有任何配置的情况下 key 需要和列名完全一致。

            InsertOperation<Map<String, Object>> lambdaInsert = lambdaTemplate.insertByTable("user_table");
            assert 1 == lambdaInsert.applyMap(userData).executeSumResult();

            // 校验结果
            MapQueryOperation lambdaQuery = lambdaTemplate.queryByTable("user_table").asMap();
            Map<String, Object> resultData = lambdaQuery.eq("id", 128).queryForObject();

            assert resultData.get("id").equals(128);
            assert resultData.get("age").equals(36);
            assert resultData.get("name").equals("default user");
            assert resultData.get("create_time") != null;
        }
    }
}