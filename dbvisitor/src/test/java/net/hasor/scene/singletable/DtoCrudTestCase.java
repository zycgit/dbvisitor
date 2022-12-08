package net.hasor.scene.singletable;
import net.hasor.dbvisitor.lambda.EntityQueryOperation;
import net.hasor.dbvisitor.lambda.InsertOperation;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.scene.singletable.dto.UserDTO;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/** 使用带有映射的 DTO 对象进行数据 CRUD */
public class DtoCrudTestCase {

    // 简单的将普通 DTO 映射到表
    @Test
    public void insertByBean() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            UserDTO userData = new UserDTO();
            userData.setAge(36);
            userData.setName("default user");
            userData.setCreateTime(new Date());

            InsertOperation<UserDTO> lambdaInsert = lambdaTemplate.lambdaInsert(UserDTO.class);
            int res = lambdaInsert.applyEntity(userData).executeSumResult();
            assert res == 1;

            // 校验结果
            EntityQueryOperation<UserDTO> lambdaQuery = lambdaTemplate.lambdaQuery(UserDTO.class);
            UserDTO resultData = lambdaQuery.eq(UserDTO::getName, "default user").queryForObject();
            assert resultData.getName().equals(userData.getName());
        }
    }

    // 简单的将普通 DTO 映射到表。偶尔数据是以 map 形式出现，为了避免 map/bean 的转换，允许直接将 map 插入到数据
    @Test
    public void insertByMap() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            Map<String, Object> userData = new HashMap<>();
            userData.put("age", 36);
            userData.put("name", "default user");
            userData.put("create_time", new Date());// 默认驼峰转换是关闭的，因此在没有任何配置的情况下 key 需要和列名完全一致。

            InsertOperation<UserDTO> lambdaInsert = lambdaTemplate.lambdaInsert(UserDTO.class);
            int res = lambdaInsert.applyMap(userData).executeSumResult();
            assert res == 1;

            // 校验结果
            EntityQueryOperation<UserDTO> lambdaQuery = lambdaTemplate.lambdaQuery(UserDTO.class);
            UserDTO resultData = lambdaQuery.eq(UserDTO::getName, "default user").queryForObject();
            assert resultData.getName().equals(userData.get("name"));
        }
    }

    // 基于 DTO 的 条件更新,只更新一个列
    @Test
    public void updateOneColumn() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            // update user set name = 'new name is abc' where id = 1
            lambdaTemplate.lambdaUpdate(UserDTO.class) //
                    .eq(UserDTO::getId, 1)         //
                    .updateTo(UserDTO::getName, "new name is abc")//
                    .doUpdate();

            // 校验结果
            EntityQueryOperation<UserDTO> lambdaQuery = lambdaTemplate.lambdaQuery(UserDTO.class);
            UserDTO resultData = lambdaQuery.eq(UserDTO::getId, 1).queryForObject();
            assert resultData.getName().equals("new name is abc");
        }
    }

    // 基于 DTO 的 条件更新, 更新多个列
    @Test
    public void updateMultipleColumn() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            // update user set name = 'new name is abc', age = 120 where id = 1
            lambdaTemplate.lambdaUpdate(UserDTO.class) //
                    .eq(UserDTO::getId, 1)         //
                    .updateToAdd(UserDTO::getName, "new name is abc")//
                    .updateToAdd(UserDTO::getAge, 120)//
                    .doUpdate();

            // 校验结果
            EntityQueryOperation<UserDTO> lambdaQuery = lambdaTemplate.lambdaQuery(UserDTO.class);
            UserDTO resultData = lambdaQuery.eq(UserDTO::getId, 1).queryForObject();
            assert resultData.getName().equals("new name is abc");
            assert resultData.getAge() == 120;
        }
    }

    // 基于 DTO 的条件更新, 为当更新的列比较多的时候可以使用 map 来存放新数据
    @Test
    public void updateByMap() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            Map<String, Object> newValue = new HashMap<>();
            newValue.put("name", "new name is abc");
            newValue.put("age", 120);

            // update user set name = 'new name is abc', age = 120 where id = 1
            lambdaTemplate.lambdaUpdate(UserDTO.class) //
                    .eq(UserDTO::getId, 1)//
                    .updateByMap(newValue)   //
                    .doUpdate();

            // 校验结果
            EntityQueryOperation<UserDTO> lambdaQuery = lambdaTemplate.lambdaQuery(UserDTO.class);
            UserDTO resultData = lambdaQuery.eq(UserDTO::getId, 1).queryForObject();
            assert resultData.getName().equals("new name is abc");
            assert resultData.getAge() == 120;
        }
    }

    // 基于 DTO 的条件更新, 使用 DTO 来存放新数据，不需要更新的列不设置值
    @Test
    public void updateBySample() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            UserDTO newData = new UserDTO();
            newData.setName("new name is abc");
            newData.setAge(120);

            // update user set name = 'new name is abc', age = 120 where id = 1
            lambdaTemplate.lambdaUpdate(UserDTO.class) //
                    .eq(UserDTO::getId, 1) //
                    .updateBySample(newData)  //
                    .doUpdate();

            // 校验结果
            EntityQueryOperation<UserDTO> lambdaQuery = lambdaTemplate.lambdaQuery(UserDTO.class);
            UserDTO resultData = lambdaQuery.eq(UserDTO::getId, 1).queryForObject();
            assert resultData.getName().equals("new name is abc");
            assert resultData.getAge() == 120;
        }
    }

    // 基于 DTO 的条件更新, 整行更新（危险操作）
    @Test
    public void updateRow() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            // 除了 name 和 pk 之外，其它列应该都是 null。
            UserDTO newData = new UserDTO();
            newData.setName("new name is abc");

            // update user set name = 'new name is abc', age = 120 where id = 1
            int i = lambdaTemplate.lambdaUpdate(UserDTO.class) //
                    .eq(UserDTO::getId, 1) //
                    .allowReplaceRow()  // 整行更新需要通过 allowReplaceRow 开启
                    .updateTo(newData)  //
                    .doUpdate();
            assert i == 1;

            // 校验结果（除 id 和 name 外全部都被设置为空了）
            EntityQueryOperation<UserDTO> lambdaQuery = lambdaTemplate.lambdaQuery(UserDTO.class);
            UserDTO resultData = lambdaQuery.eq(UserDTO::getId, 1).queryForObject();
            assert resultData.getId() == 1;
            assert resultData.getName().equals("new name is abc");
            assert resultData.getAge() == null;
            assert resultData.getCreateTime() == null;
        }
    }

    // 基于 DTO 的条件更新, 变更主键 ID（危险操作）
    @Test
    public void updatePK() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            // 除了 name 和 pk 之外，其它列应该都是 null。
            UserDTO newData = new UserDTO();
            newData.setId(112);
            newData.setName("new name is abc");

            // update user set name = 'new name is abc', age = 120 where id = 1
            int i = lambdaTemplate.lambdaUpdate(UserDTO.class) //
                    .eq(UserDTO::getId, 1) //
                    .allowUpdateKey()  // 需要启用 allowUpdateKey
                    .updateBySample(newData)  //
                    .doUpdate();
            assert i == 1;

            // 校验结果
            EntityQueryOperation<UserDTO> lambdaQuery = lambdaTemplate.lambdaQuery(UserDTO.class);
            UserDTO resultData = lambdaQuery.eq(UserDTO::getId, 112).queryForObject();
            assert resultData.getId() == 112;
            assert resultData.getName().equals("new name is abc");
            assert resultData.getAge() != null;
            assert resultData.getCreateTime() != null;
        }
    }

}
