package net.hasor.scene.wrapper.crud;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.wrapper.EntityQueryWrapper;
import net.hasor.dbvisitor.wrapper.InsertWrapper;
import net.hasor.dbvisitor.wrapper.WrapperAdapter;
import net.hasor.scene.wrapper.crud.dto.UserTable;
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

    private static final Options options = Options.of().mapUnderscoreToCamelCase(true);

    // 简单的将普通 Bean 映射到表
    @Test
    public void insertByBean() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambdaTemplate = new WrapperAdapter(c, options);

            UserTable userData = new UserTable();
            userData.setId(100);// POJO 由于没有配置忽略信息，因此无法享受自增ID，必须指定ID
            userData.setAge(36);
            userData.setName("default user");
            userData.setCreate_time(new Date());// 默认驼峰转换是关闭的，因此在没有任何配置的情况下普通 pojo 的列名需要和表名字段完全一致。

            InsertWrapper<UserTable> lambdaInsert = lambdaTemplate.insert(UserTable.class);
            assert 1 == lambdaInsert.applyEntity(userData).executeSumResult();

            // 校验结果
            EntityQueryWrapper<UserTable> lambdaQuery = lambdaTemplate.query(UserTable.class);
            UserTable resultData = lambdaQuery.eq(UserTable::getName, "default user").queryForObject();
            assert resultData.getName().equals(userData.getName());
        }
    }

    // 简单的将普通 Bean 映射到表。偶尔数据是以 map 形式出现，为了避免 map/bean 的转换，允许直接将 map 插入到数据
    @Test
    public void insertByMap() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambdaTemplate = new WrapperAdapter(c, options);

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", 100);// POJO 由于没有配置忽略信息，因此无法享受自增ID，必须指定ID
            userData.put("age", 36);
            userData.put("name", "default user");
            userData.put("create_time", new Date());// 默认驼峰转换是关闭的，因此在没有任何配置的情况下 key 需要和列名完全一致。

            InsertWrapper<UserTable> lambdaInsert = lambdaTemplate.insert(UserTable.class);
            assert 1 == lambdaInsert.applyMap(userData).executeSumResult();

            // 校验结果
            EntityQueryWrapper<UserTable> lambdaQuery = lambdaTemplate.query(UserTable.class);
            UserTable resultData = lambdaQuery.eq(UserTable::getName, "default user").queryForObject();
            assert resultData.getName().equals(userData.get("name"));
        }
    }

    // 基于 pojo 的 条件更新,只更新一个列
    @Test
    public void updateOneColumn() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambdaTemplate = new WrapperAdapter(c, options);

            // update user set name = 'new name is abc' where id = 1
            lambdaTemplate.update(UserTable.class) //
                    .eq(UserTable::getId, 1)         //
                    .updateTo(UserTable::getName, "new name is abc")//
                    .doUpdate();

            // 校验结果
            EntityQueryWrapper<UserTable> lambdaQuery = lambdaTemplate.query(UserTable.class);
            UserTable resultData = lambdaQuery.eq(UserTable::getId, 1).queryForObject();
            assert resultData.getName().equals("new name is abc");
        }
    }

    // 基于 pojo 的 条件更新, 更新多个列
    @Test
    public void updateMultipleColumn() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambdaTemplate = new WrapperAdapter(c, options);

            // update user set name = 'new name is abc', age = 120 where id = 1
            lambdaTemplate.update(UserTable.class) //
                    .eq(UserTable::getId, 1)         //
                    .updateTo(UserTable::getName, "new name is abc")//
                    .updateTo(UserTable::getAge, 120)//
                    .doUpdate();

            // 校验结果
            EntityQueryWrapper<UserTable> lambdaQuery = lambdaTemplate.query(UserTable.class);
            UserTable resultData = lambdaQuery.eq(UserTable::getId, 1).queryForObject();
            assert resultData.getName().equals("new name is abc");
            assert resultData.getAge() == 120;
        }
    }

    // 基于 pojo 的条件更新, 为当更新的列比较多的时候可以使用 map 来存放新数据
    @Test
    public void updateByMap() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambdaTemplate = new WrapperAdapter(c, options);

            Map<String, Object> newValue = new HashMap<>();
            newValue.put("name", "new name is abc");
            newValue.put("age", 120);

            // update user set name = 'new name is abc', age = 120 where id = 1
            lambdaTemplate.update(UserTable.class) //
                    .eq(UserTable::getId, 1)//
                    .updateToSampleMap(newValue)   //
                    .doUpdate();

            // 校验结果
            EntityQueryWrapper<UserTable> lambdaQuery = lambdaTemplate.query(UserTable.class);
            UserTable resultData = lambdaQuery.eq(UserTable::getId, 1).queryForObject();
            assert resultData.getName().equals("new name is abc");
            assert resultData.getAge() == 120;
        }
    }

    // 基于 pojo 的条件更新, 使用 pojo 来存放新数据，不需要更新的列不设置值
    @Test
    public void updateBySample() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambdaTemplate = new WrapperAdapter(c, options);

            UserTable newData = new UserTable();
            newData.setName("new name is abc");
            newData.setAge(120);

            // update user set name = 'new name is abc', age = 120 where id = 1
            lambdaTemplate.update(UserTable.class) //
                    .eq(UserTable::getId, 1) //
                    .updateToSample(newData)  //
                    .doUpdate();

            // 校验结果
            EntityQueryWrapper<UserTable> lambdaQuery = lambdaTemplate.query(UserTable.class);
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
            WrapperAdapter lambdaTemplate = new WrapperAdapter(c, options);

            // 除了 name 和 pk 之外，其它列应该都是 null。
            UserTable newData = new UserTable();
            newData.setId(1);
            newData.setName("new name is abc");

            // update user set name = 'new name is abc', age = 120 where id = 1
            int i = lambdaTemplate.update(UserTable.class) //
                    .eq(UserTable::getId, 1) //
                    .updateRow(newData)  //
                    .doUpdate();
            assert i == 1;

            // 校验结果
            EntityQueryWrapper<UserTable> lambdaQuery = lambdaTemplate.query(UserTable.class);
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
            WrapperAdapter lambdaTemplate = new WrapperAdapter(c, options);

            // delete from user where id = 1;
            int i = lambdaTemplate.delete(UserTable.class) //
                    .eq(UserTable::getId, 1) //
                    .doDelete();
            assert i == 1;

            // 校验结果
            EntityQueryWrapper<UserTable> lambdaQuery = lambdaTemplate.query(UserTable.class);
            UserTable resultData = lambdaQuery.eq(UserTable::getId, 1).queryForObject();
            assert resultData == null;
        }
    }

    // 基于 map 样本的条件删除
    @Test
    public void deleteBySample() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambdaTemplate = new WrapperAdapter(c, options);

            // 条件对象
            UserTable sample = new UserTable();
            sample.setId(1);
            sample.setName("mali");

            // delete from user where id = 1 and name = 'mail';
            int i = lambdaTemplate.delete(UserTable.class) //
                    .eqBySample(sample)//
                    .doDelete();
            assert i == 1;

            // 校验结果
            EntityQueryWrapper<UserTable> lambdaQuery = lambdaTemplate.query(UserTable.class);
            UserTable resultData = lambdaQuery.eq(UserTable::getId, 1).queryForObject();
            assert resultData == null;
        }
    }

    // 基于 map 样本的条件删除
    @Test
    public void deleteBySampleMap() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambdaTemplate = new WrapperAdapter(c, options);

            Map<String, Object> newValue = new HashMap<>();
            newValue.put("id", 1);
            newValue.put("name", "mali");

            // delete from user where id = 1 and name = 'mail';
            int i = lambdaTemplate.delete(UserTable.class) //
                    .eqBySampleMap(newValue)//
                    .doDelete();
            assert i == 1;

            // 校验结果
            EntityQueryWrapper<UserTable> lambdaQuery = lambdaTemplate.query(UserTable.class);
            UserTable resultData = lambdaQuery.eq(UserTable::getId, 1).queryForObject();
            assert resultData == null;
        }
    }

    // 全部删除
    @Test
    public void deleteALL() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambdaTemplate = new WrapperAdapter(c, options);

            // delete from user;
            int i = lambdaTemplate.delete(UserTable.class) //
                    .allowEmptyWhere()// 无条件删除需要启用空条件
                    .doDelete();
            assert i == 5;

            // 校验结果
            assert lambdaTemplate.query(UserTable.class).queryForCount() == 0;
        }
    }

    // 简单的将普通 pojo 映射到表
    @Test
    public void batchInsertByPojo() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambdaTemplate = new WrapperAdapter(c, options);

            InsertWrapper<UserTable> lambdaInsert = lambdaTemplate.insert(UserTable.class);
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
            EntityQueryWrapper<UserTable> lambdaQuery = lambdaTemplate.query(UserTable.class);
            List<UserTable> resultData = lambdaQuery.likeRight(UserTable::getName, "default user ").queryForList();
            List<String> result = resultData.stream().map(UserTable::getName).collect(Collectors.toList());
            assert result.size() == 10;

            for (int i = 0; i < 10; i++) {
                assert result.get(i).equals("default user " + i);
            }
        }
    }
}