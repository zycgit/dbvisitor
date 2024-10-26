package net.hasor.scene.singletable;
import net.hasor.dbvisitor.lambda.EntityQueryOperation;
import net.hasor.dbvisitor.lambda.InsertOperation;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.scene.singletable.dto.UserTableDTO;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/** 使用带有映射的 DTO 对象进行数据 CRUD */
public class DtoCrudTestCase {

    // 简单的将普通 DTO 映射到表
    @Test
    public void insertByBean() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            lambdaTemplate.deleteBySpace(UserTableDTO.class).allowEmptyWhere().doDelete();

            UserTableDTO userData = new UserTableDTO();
            userData.setAge(36);
            userData.setName("default user");
            userData.setCreateTime(new Date());
            assert userData.getId() == null;

            InsertOperation<UserTableDTO> lambdaInsert = lambdaTemplate.insertBySpace(UserTableDTO.class);
            assert 1 == lambdaInsert.applyEntity(userData).executeSumResult();
            assert userData.getId() != null;// 自增 ID 回填

            // 根据 ID 反查
            EntityQueryOperation<UserTableDTO> lambdaQuery = lambdaTemplate.queryBySpace(UserTableDTO.class);
            UserTableDTO resultData = lambdaQuery.eq(UserTableDTO::getId, userData.getId()).queryForObject();
            assert resultData.getName().equals("default user");
        }
    }

    // 简单的将普通 DTO 映射到表。偶尔数据是以 map 形式出现，为了避免 map/bean 的转换，允许直接将 map 插入到数据
    @Test
    public void insertByMap() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            lambdaTemplate.deleteBySpace(UserTableDTO.class).allowEmptyWhere().doDelete();

            Map<String, Object> userData = new HashMap<>();
            userData.put("age", 36);
            userData.put("name", "default user");
            userData.put("create_time", new Date());// 默认驼峰转换是关闭的，因此在没有任何配置的情况下 key 需要和列名完全一致。
            assert userData.get("id") == null;

            InsertOperation<UserTableDTO> lambdaInsert = lambdaTemplate.insertBySpace(UserTableDTO.class);
            int res = lambdaInsert.applyMap(userData).executeSumResult();
            assert res == 1;
            assert userData.get("id") != null;// 根据 UserDTO 属性信息，自增 ID 回填

            // 校验结果
            EntityQueryOperation<UserTableDTO> lambdaQuery = lambdaTemplate.queryBySpace(UserTableDTO.class);
            UserTableDTO resultData = lambdaQuery.eq(UserTableDTO::getId, userData.get("id")).queryForObject();
            assert resultData.getName().equals("default user");
        }
    }

    // 基于 DTO 的 条件更新,只更新一个列
    @Test
    public void updateOneColumn() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            // update user set name = 'new name is abc' where id = 1
            lambdaTemplate.updateBySpace(UserTableDTO.class) //
                    .eq(UserTableDTO::getId, 1)       //
                    .updateTo(UserTableDTO::getName, "new name is abc")//
                    .doUpdate();

            // 校验结果
            EntityQueryOperation<UserTableDTO> lambdaQuery = lambdaTemplate.queryBySpace(UserTableDTO.class);
            UserTableDTO resultData = lambdaQuery.eq(UserTableDTO::getId, 1).queryForObject();
            assert resultData.getName().equals("new name is abc");
        }
    }

    // 基于 DTO 的 条件更新, 更新多个列
    @Test
    public void updateMultipleColumn() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            // update user set name = 'new name is abc', age = 120 where id = 1
            lambdaTemplate.updateBySpace(UserTableDTO.class) //
                    .eq(UserTableDTO::getId, 1)       //
                    .updateTo(UserTableDTO::getName, "new name is abc")//
                    .updateTo(UserTableDTO::getAge, 120)//
                    .doUpdate();

            // 校验结果
            EntityQueryOperation<UserTableDTO> lambdaQuery = lambdaTemplate.queryBySpace(UserTableDTO.class);
            UserTableDTO resultData = lambdaQuery.eq(UserTableDTO::getId, 1).queryForObject();
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
            lambdaTemplate.updateBySpace(UserTableDTO.class) //
                    .eq(UserTableDTO::getId, 1)//
                    .updateToMap(newValue)   //
                    .doUpdate();

            // 校验结果
            EntityQueryOperation<UserTableDTO> lambdaQuery = lambdaTemplate.queryBySpace(UserTableDTO.class);
            UserTableDTO resultData = lambdaQuery.eq(UserTableDTO::getId, 1).queryForObject();
            assert resultData.getName().equals("new name is abc");
            assert resultData.getAge() == 120;
        }
    }

    // 基于 DTO 的条件更新, 使用 DTO 来存放新数据，不需要更新的列不设置值
    @Test
    public void updateBySample() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            UserTableDTO newData = new UserTableDTO();
            newData.setName("new name is abc");
            newData.setAge(120);

            // update user set name = 'new name is abc', age = 120 where id = 1
            lambdaTemplate.updateBySpace(UserTableDTO.class) //
                    .eq(UserTableDTO::getId, 1) //
                    .updateToSample(newData)  //
                    .doUpdate();

            // 校验结果
            EntityQueryOperation<UserTableDTO> lambdaQuery = lambdaTemplate.queryBySpace(UserTableDTO.class);
            UserTableDTO resultData = lambdaQuery.eq(UserTableDTO::getId, 1).queryForObject();
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
            UserTableDTO newData = new UserTableDTO();
            newData.setName("new name is abc");

            // update user set name = 'new name is abc', age = 120 where id = 1
            int i = lambdaTemplate.updateBySpace(UserTableDTO.class) //
                    .eq(UserTableDTO::getId, 1) //
                    .allowReplaceRow()  // 整行更新需要通过 allowReplaceRow 开启
                    .updateTo(newData)  //
                    .doUpdate();
            assert i == 1;

            // 校验结果（除 id 和 name 外全部都被设置为空了）
            EntityQueryOperation<UserTableDTO> lambdaQuery = lambdaTemplate.queryBySpace(UserTableDTO.class);
            UserTableDTO resultData = lambdaQuery.eq(UserTableDTO::getId, 1).queryForObject();
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
            UserTableDTO newData = new UserTableDTO();
            newData.setId(112);
            newData.setName("new name is abc");

            // update user set name = 'new name is abc', age = 120 where id = 1
            int i = lambdaTemplate.updateBySpace(UserTableDTO.class) //
                    .eq(UserTableDTO::getId, 1) //
                    .allowUpdateKey()  // 需要启用 allowUpdateKey
                    .updateToSample(newData)  //
                    .doUpdate();
            assert i == 1;

            // 校验结果
            EntityQueryOperation<UserTableDTO> lambdaQuery = lambdaTemplate.queryBySpace(UserTableDTO.class);
            UserTableDTO resultData = lambdaQuery.eq(UserTableDTO::getId, 112).queryForObject();
            assert resultData.getId() == 112;
            assert resultData.getName().equals("new name is abc");
            assert resultData.getAge() != null;
            assert resultData.getCreateTime() != null;
        }
    }

    // 基于条件删除
    @Test
    public void deleteByID() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            // delete from user where id = 1;
            int i = lambdaTemplate.deleteBySpace(UserTableDTO.class) //
                    .eq(UserTableDTO::getId, 1) //
                    .doDelete();
            assert i == 1;

            // 校验结果
            EntityQueryOperation<UserTableDTO> lambdaQuery = lambdaTemplate.queryBySpace(UserTableDTO.class);
            UserTableDTO resultData = lambdaQuery.eq(UserTableDTO::getId, 1).queryForObject();
            assert resultData == null;
        }
    }

    // 基于 DTO 样本的条件删除
    @Test
    public void deleteBySample() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            // 条件对象
            UserTableDTO sample = new UserTableDTO();
            sample.setId(1);
            sample.setName("mali");

            // delete from user where id = 1 and name = 'mail';
            int i = lambdaTemplate.deleteBySpace(UserTableDTO.class) //
                    .eqBySample(sample)//
                    .doDelete();
            assert i == 1;

            // 校验结果
            EntityQueryOperation<UserTableDTO> lambdaQuery = lambdaTemplate.queryBySpace(UserTableDTO.class);
            UserTableDTO resultData = lambdaQuery.eq(UserTableDTO::getId, 1).queryForObject();
            assert resultData == null;
        }
    }

    // 基于 map 样本的条件删除
    @Test
    public void deleteBySampleMap() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            Map<String, Object> newValue = new HashMap<>();
            newValue.put("id", 1);
            newValue.put("name", "mali");

            // delete from user where id = 1 and name = 'mail';
            int i = lambdaTemplate.deleteBySpace(UserTableDTO.class) //
                    .eqBySampleMap(newValue)//
                    .doDelete();
            assert i == 1;

            // 校验结果
            EntityQueryOperation<UserTableDTO> lambdaQuery = lambdaTemplate.queryBySpace(UserTableDTO.class);
            UserTableDTO resultData = lambdaQuery.eq(UserTableDTO::getId, 1).queryForObject();
            assert resultData == null;
        }
    }

    // 全部删除
    @Test
    public void deleteALL() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            // delete from user;
            int i = lambdaTemplate.deleteBySpace(UserTableDTO.class) //
                    .allowEmptyWhere()// 无条件删除需要启用空条件
                    .doDelete();
            assert i == 5;

            // 校验结果
            EntityQueryOperation<UserTableDTO> lambdaQuery = lambdaTemplate.queryBySpace(UserTableDTO.class);
            assert lambdaQuery.queryForCount() == 0;
        }
    }

    // 简单的将普通 DTO 映射到表
    @Test
    public void batchInsertByDTO() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            lambdaTemplate.deleteBySpace(UserTableDTO.class).allowEmptyWhere().doDelete();

            InsertOperation<UserTableDTO> lambdaInsert = lambdaTemplate.insertBySpace(UserTableDTO.class);
            List<UserTableDTO> insertData = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                UserTableDTO userData = new UserTableDTO();
                userData.setAge(36);
                userData.setName("default user " + i);
                userData.setCreateTime(new Date());
                lambdaInsert.applyEntity(userData);
                insertData.add(userData);
            }
            int res = lambdaInsert.executeSumResult();
            assert res == 10;
            for (int i = 0; i < 10; i++) {
                assert insertData.get(i).getId() != null;// 自增 id 自动回填
            }

            // 校验结果
            EntityQueryOperation<UserTableDTO> lambdaQuery = lambdaTemplate.queryBySpace(UserTableDTO.class);
            List<UserTableDTO> resultData = lambdaQuery.likeRight(UserTableDTO::getName, "default user ").queryForList();
            List<String> result = resultData.stream().map(UserTableDTO::getName).collect(Collectors.toList());
            assert result.size() == 10;

            for (int i = 0; i < 10; i++) {
                assert result.get(i).equals("default user " + i);
            }
        }
    }
}
