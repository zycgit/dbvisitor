package net.hasor.scene.singletable;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.dbvisitor.lambda.EntityQueryOperation;
import net.hasor.dbvisitor.lambda.InsertOperation;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.resolve.MappingOptions;
import net.hasor.scene.singletable.dto.UserTable;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 使用标准的 pojo 来做 DTO 进行数据 插入 */
public class PojoCrudTestCase {

    private static final MappingOptions options = MappingOptions.buildNew().mapUnderscoreToCamelCase(true);

    // 简单的将普通 Bean 映射到表
    @Test
    public void insertByBean() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            UserTable userData = new UserTable();
            userData.setId(100);// POJO 由于没有配置忽略信息，因此无法享受自增ID，必须指定ID
            userData.setAge(36);
            userData.setName("default user");
            userData.setCreate_time(new Date());// 默认驼峰转换是关闭的，因此在没有任何配置的情况下普通 pojo 的列名需要和表名字段完全一致。

            InsertOperation<UserTable> lambdaInsert = lambdaTemplate.lambdaInsert(UserTable.class, options);
            assert 1 == lambdaInsert.applyEntity(userData).executeSumResult();

            // 校验结果
            EntityQueryOperation<UserTable> lambdaQuery = lambdaTemplate.lambdaQuery(UserTable.class, options);
            UserTable resultData = lambdaQuery.eq(UserTable::getName, "default user").queryForObject();
            assert resultData.getName().equals(userData.getName());
        }
    }

    // 简单的将普通 Bean 映射到表。偶尔数据是以 map 形式出现，为了避免 map/bean 的转换，允许直接将 map 插入到数据
    @Test
    public void insertByMap() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", 100);// POJO 由于没有配置忽略信息，因此无法享受自增ID，必须指定ID
            userData.put("age", 36);
            userData.put("name", "default user");
            userData.put("create_time", new Date());// 默认驼峰转换是关闭的，因此在没有任何配置的情况下 key 需要和列名完全一致。

            InsertOperation<UserTable> lambdaInsert = lambdaTemplate.lambdaInsert(UserTable.class, options);
            assert 1 == lambdaInsert.applyMap(userData).executeSumResult();

            // 校验结果
            EntityQueryOperation<UserTable> lambdaQuery = lambdaTemplate.lambdaQuery(UserTable.class, options);
            UserTable resultData = lambdaQuery.eq(UserTable::getName, "default user").queryForObject();
            assert resultData.getName().equals(userData.get("name"));
        }
    }

    // 基于 pojo 的 条件更新,只更新一个列
    @Test
    public void updateOneColumn() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            // update user set name = 'new name is abc' where id = 1
            lambdaTemplate.lambdaUpdate(UserTable.class, options) //
                    .eq(UserTable::getId, 1)         //
                    .updateTo(UserTable::getName, "new name is abc")//
                    .doUpdate();

            // 校验结果
            EntityQueryOperation<UserTable> lambdaQuery = lambdaTemplate.lambdaQuery(UserTable.class, options);
            UserTable resultData = lambdaQuery.eq(UserTable::getId, 1).queryForObject();
            assert resultData.getName().equals("new name is abc");
        }
    }

    // 基于 pojo 的 条件更新, 更新多个列
    @Test
    public void updateMultipleColumn() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            // update user set name = 'new name is abc', age = 120 where id = 1
            lambdaTemplate.lambdaUpdate(UserTable.class, options) //
                    .eq(UserTable::getId, 1)         //
                    .updateToAdd(UserTable::getName, "new name is abc")//
                    .updateToAdd(UserTable::getAge, 120)//
                    .doUpdate();

            // 校验结果
            EntityQueryOperation<UserTable> lambdaQuery = lambdaTemplate.lambdaQuery(UserTable.class, options);
            UserTable resultData = lambdaQuery.eq(UserTable::getId, 1).queryForObject();
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
            lambdaTemplate.lambdaUpdate(UserTable.class, options) //
                    .eq(UserTable::getId, 1)//
                    .updateByMap(newValue)   //
                    .doUpdate();

            // 校验结果
            EntityQueryOperation<UserTable> lambdaQuery = lambdaTemplate.lambdaQuery(UserTable.class, options);
            UserTable resultData = lambdaQuery.eq(UserTable::getId, 1).queryForObject();
            assert resultData.getName().equals("new name is abc");
            assert resultData.getAge() == 120;
        }
    }

    // 基于 pojo 的条件更新, 使用 pojo 来存放新数据，不需要更新的列不设置值
    @Test
    public void updateBySample() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            UserTable newData = new UserTable();
            newData.setName("new name is abc");
            newData.setAge(120);

            // update user set name = 'new name is abc', age = 120 where id = 1
            lambdaTemplate.lambdaUpdate(UserTable.class, options) //
                    .eq(UserTable::getId, 1) //
                    .updateBySample(newData)  //
                    .doUpdate();

            // 校验结果
            EntityQueryOperation<UserTable> lambdaQuery = lambdaTemplate.lambdaQuery(UserTable.class, options);
            UserTable resultData = lambdaQuery.eq(UserTable::getId, 1).queryForObject();
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
            UserTable newData = new UserTable();
            newData.setId(1);
            newData.setName("new name is abc");

            // update user set name = 'new name is abc', age = 120 where id = 1
            int i = lambdaTemplate.lambdaUpdate(UserTable.class, options) //
                    .eq(UserTable::getId, 1) //
                    .allowReplaceRow()  // 需要启用整行更新才能使用
                    .updateTo(newData)  //
                    .doUpdate();
            assert i == 1;

            // 校验结果
            EntityQueryOperation<UserTable> lambdaQuery = lambdaTemplate.lambdaQuery(UserTable.class, options);
            UserTable resultData = lambdaQuery.eq(UserTable::getName, "new name is abc").queryForObject();

            assert resultData.getId() == 1;
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
            int i = lambdaTemplate.lambdaDelete(UserTable.class, options) //
                    .eq(UserTable::getId, 1) //
                    .doDelete();
            assert i == 1;

            // 校验结果
            EntityQueryOperation<UserTable> lambdaQuery = lambdaTemplate.lambdaQuery(UserTable.class, options);
            UserTable resultData = lambdaQuery.eq(UserTable::getId, 1).queryForObject();
            assert resultData == null;
        }
    }

    // 基于 map 样本的条件删除
    @Test
    public void deleteBySample() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            // 条件对象
            UserTable sample = new UserTable();
            sample.setId(1);
            sample.setName("mali");

            // delete from user where id = 1 and name = 'mail';
            int i = lambdaTemplate.lambdaDelete(UserTable.class, options) //
                    .eqBySample(sample)//
                    .doDelete();
            assert i == 1;

            // 校验结果
            EntityQueryOperation<UserTable> lambdaQuery = lambdaTemplate.lambdaQuery(UserTable.class, options);
            UserTable resultData = lambdaQuery.eq(UserTable::getId, 1).queryForObject();
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
            int i = lambdaTemplate.lambdaDelete(UserTable.class, options) //
                    .eqBySampleMap(newValue)//
                    .doDelete();
            assert i == 1;

            // 校验结果
            EntityQueryOperation<UserTable> lambdaQuery = lambdaTemplate.lambdaQuery(UserTable.class, options);
            UserTable resultData = lambdaQuery.eq(UserTable::getId, 1).queryForObject();
            assert resultData == null;
        }
    }

    // 全部删除
    @Test
    public void deleteALL() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            // delete from user;
            int i = lambdaTemplate.lambdaDelete(UserTable.class, options) //
                    .allowEmptyWhere()// 无条件删除需要启用空条件
                    .doDelete();
            assert i == 5;

            // 校验结果
            assert lambdaTemplate.lambdaQuery(UserTable.class, options).queryForCount() == 0;
        }
    }

    // 简单的将普通 pojo 映射到表
    @Test
    public void batchInsertByPojo() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            InsertOperation<UserTable> lambdaInsert = lambdaTemplate.lambdaInsert(UserTable.class, options);
            for (int i = 0; i < 10; i++) {
                UserTable userData = new UserTable();
                userData.setId(i + 10);
                userData.setAge(36);
                userData.setName("default user " + i);
                userData.setCreate_time(new Date());
                lambdaInsert.applyEntity(userData);
            }
            int res = lambdaInsert.executeSumResult();
            assert res == 10;

            // 校验结果
            EntityQueryOperation<UserTable> lambdaQuery = lambdaTemplate.lambdaQuery(UserTable.class, options);
            List<UserTable> resultData = lambdaQuery.likeRight(UserTable::getName, "default user ").queryForList();
            List<String> result = resultData.stream().map(UserTable::getName).collect(Collectors.toList());
            assert result.size() == 10;

            for (int i = 0; i < 10; i++) {
                assert result.get(i).equals("default user " + i);
            }
        }
    }
}