package net.hasor.scene.singletable;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.dbvisitor.lambda.EntityQueryOperation;
import net.hasor.dbvisitor.lambda.InsertOperation;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.scene.singletable.dto.User;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/** 使用标准的 pojo 来做 DTO 进行数据 插入 */
public class PojoCrudTestCase {

    // 简单的将普通 Bean 映射到表
    @Test
    public void insertByBean() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            User userData = new User();
            userData.setAge(36);
            userData.setName("default user");
            userData.setCreate_time(new Date());// 默认驼峰转换是关闭的，因此在没有任何配置的情况下普通 pojo 的列名需要和表名字段完全一致。
            assert userData.getId() == null;

            InsertOperation<User> lambdaInsert = lambdaTemplate.lambdaInsert(User.class);
            assert 1 == lambdaInsert.applyEntity(userData).executeSumResult();
            assert userData.getId() == null; // POJO 没有 自增 ID 配置信息，因此不回填

            // 校验结果
            EntityQueryOperation<User> lambdaQuery = lambdaTemplate.lambdaQuery(User.class);
            User resultData = lambdaQuery.eq(User::getName, "default user").queryForObject();
            assert resultData.getName().equals(userData.getName());
        }
    }

    // 简单的将普通 Bean 映射到表。偶尔数据是以 map 形式出现，为了避免 map/bean 的转换，允许直接将 map 插入到数据
    @Test
    public void insertByMap() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            Map<String, Object> userData = new HashMap<>();
            userData.put("age", 36);
            userData.put("name", "default user");
            userData.put("create_time", new Date());// 默认驼峰转换是关闭的，因此在没有任何配置的情况下 key 需要和列名完全一致。

            InsertOperation<User> lambdaInsert = lambdaTemplate.lambdaInsert(User.class);
            assert 1 == lambdaInsert.applyMap(userData).executeSumResult();

            // 校验结果
            EntityQueryOperation<User> lambdaQuery = lambdaTemplate.lambdaQuery(User.class);
            User resultData = lambdaQuery.eq(User::getName, "default user").queryForObject();
            assert resultData.getName().equals(userData.get("name"));
        }
    }

    // 基于 pojo 的 条件更新,只更新一个列
    @Test
    public void updateOneColumn() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            // update user set name = 'new name is abc' where id = 1
            lambdaTemplate.lambdaUpdate(User.class) //
                    .eq(User::getId, 1)         //
                    .updateTo(User::getName, "new name is abc")//
                    .doUpdate();

            // 校验结果
            EntityQueryOperation<User> lambdaQuery = lambdaTemplate.lambdaQuery(User.class);
            User resultData = lambdaQuery.eq(User::getId, 1).queryForObject();
            assert resultData.getName().equals("new name is abc");
        }
    }

    // 基于 pojo 的 条件更新, 更新多个列
    @Test
    public void updateMultipleColumn() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            // update user set name = 'new name is abc', age = 120 where id = 1
            lambdaTemplate.lambdaUpdate(User.class) //
                    .eq(User::getId, 1)         //
                    .updateToAdd(User::getName, "new name is abc")//
                    .updateToAdd(User::getAge, 120)//
                    .doUpdate();

            // 校验结果
            EntityQueryOperation<User> lambdaQuery = lambdaTemplate.lambdaQuery(User.class);
            User resultData = lambdaQuery.eq(User::getId, 1).queryForObject();
            assert resultData.getName().equals("new name is abc");
            assert resultData.getAge() == 120;
        }
    }

    // 基于 pojo 的条件更新, 为当更新的列比较多的时候可以使用 map 来存放新数据
    @Test
    public void updateByMap() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            Map<String, Object> newValue = new HashMap<>();
            newValue.put("name", "new name is abc");
            newValue.put("age", 120);

            // update user set name = 'new name is abc', age = 120 where id = 1
            lambdaTemplate.lambdaUpdate(User.class) //
                    .eq(User::getId, 1)//
                    .updateByMap(newValue)   //
                    .doUpdate();

            // 校验结果
            EntityQueryOperation<User> lambdaQuery = lambdaTemplate.lambdaQuery(User.class);
            User resultData = lambdaQuery.eq(User::getId, 1).queryForObject();
            assert resultData.getName().equals("new name is abc");
            assert resultData.getAge() == 120;
        }
    }

    // 基于 pojo 的条件更新, 使用 pojo 来存放新数据，不需要更新的列不设置值
    @Test
    public void updateBySample() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            User newData = new User();
            newData.setName("new name is abc");
            newData.setAge(120);

            // update user set name = 'new name is abc', age = 120 where id = 1
            lambdaTemplate.lambdaUpdate(User.class) //
                    .eq(User::getId, 1) //
                    .updateBySample(newData)  //
                    .doUpdate();

            // 校验结果
            EntityQueryOperation<User> lambdaQuery = lambdaTemplate.lambdaQuery(User.class);
            User resultData = lambdaQuery.eq(User::getId, 1).queryForObject();
            assert resultData.getName().equals("new name is abc");
            assert resultData.getAge() == 120;
        }
    }

    // 基于 pojo 的条件更新, 整行更新（危险操作）
    @Test
    public void updateRow() throws SQLException {
        LoggerFactory.useStdOutLogger();
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            // 除了 name 和 pk 之外，其它列应该都是 null。
            User newData = new User();
            newData.setName("new name is abc");

            // update user set name = 'new name is abc', age = 120 where id = 1
            int i = lambdaTemplate.lambdaUpdate(User.class) //
                    .eq(User::getId, 1) //
                    .allowReplaceRow()  // 需要启用整行更新才能使用
                    .updateTo(newData)  //
                    .doUpdate();
            assert i == 1;

            // 校验结果
            EntityQueryOperation<User> lambdaQuery = lambdaTemplate.lambdaQuery(User.class);
            User resultData = lambdaQuery.eq(User::getName, "new name is abc").queryForObject();

            //  -- 因为 User.class 并没有标记 id 属性为主键，因此整行更新造成了主键被设置为 null
            //  -- H2,在 id 被设置为 null 之后，会为其重新生成一个新的 id。
            assert resultData.getId() == 6;
            assert resultData.getName().equals("new name is abc");
            assert resultData.getAge() == null;
            assert resultData.getCreate_time() == null;
        }
    }

    // 基于条件删除
    @Test
    public void deleteByID() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            // delete from user where id = 1;
            int i = lambdaTemplate.lambdaDelete(User.class) //
                    .eq(User::getId, 1) //
                    .doDelete();
            assert i == 1;

            // 校验结果
            EntityQueryOperation<User> lambdaQuery = lambdaTemplate.lambdaQuery(User.class);
            User resultData = lambdaQuery.eq(User::getId, 1).queryForObject();
            assert resultData == null;
        }
    }

    // 基于 map 样本的条件删除
    @Test
    public void deleteBySample() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            // 条件对象
            User sample = new User();
            sample.setId(1);
            sample.setName("mali");

            // delete from user where id = 1 and name = 'mail';
            int i = lambdaTemplate.lambdaDelete(User.class) //
                    .eqBySample(sample)//
                    .doDelete();
            assert i == 1;

            // 校验结果
            EntityQueryOperation<User> lambdaQuery = lambdaTemplate.lambdaQuery(User.class);
            User resultData = lambdaQuery.eq(User::getId, 1).queryForObject();
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
            int i = lambdaTemplate.lambdaDelete(User.class) //
                    .eqBySampleMap(newValue)//
                    .doDelete();
            assert i == 1;

            // 校验结果
            EntityQueryOperation<User> lambdaQuery = lambdaTemplate.lambdaQuery(User.class);
            User resultData = lambdaQuery.eq(User::getId, 1).queryForObject();
            assert resultData == null;
        }
    }

    // 全部删除
    @Test
    public void deleteALL() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            // delete from user;
            int i = lambdaTemplate.lambdaDelete(User.class) //
                    .allowEmptyWhere()// 无条件删除需要启用空条件
                    .doDelete();
            assert i == 5;

            // 校验结果
            assert lambdaTemplate.lambdaQuery(User.class).queryForCount() == 0;
        }
    }

    // 简单的将普通 pojo 映射到表
    @Test
    public void batchInsertByPojo() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            InsertOperation<User> lambdaInsert = lambdaTemplate.lambdaInsert(User.class);
            List<User> insertData = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                User userData = new User();
                userData.setAge(36);
                userData.setName("default user " + i);
                userData.setCreate_time(new Date());
                lambdaInsert.applyEntity(userData);
                insertData.add(userData);
            }
            int res = lambdaInsert.executeSumResult();
            assert res == 10;
            for (int i = 0; i < 10; i++) {
                assert insertData.get(i).getId() == null;// pojo 没有自增 id 信息，因此不会回填。
            }

            // 校验结果
            EntityQueryOperation<User> lambdaQuery = lambdaTemplate.lambdaQuery(User.class);
            List<User> resultData = lambdaQuery.likeRight(User::getName, "default user ").queryForList();
            List<String> result = resultData.stream().map(User::getName).collect(Collectors.toList());
            assert result.size() == 10;

            for (int i = 0; i < 10; i++) {
                assert result.get(i).equals("default user " + i);
            }
        }
    }
}