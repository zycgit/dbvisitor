package net.hasor.scene.singletable;
import net.hasor.dbvisitor.lambda.EntityQueryOperation;
import net.hasor.dbvisitor.lambda.InsertOperation;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.lambda.MapQueryOperation;
import net.hasor.scene.singletable.dto.UserTableDTO;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/** 使用 MAP 映射数据库 CRUD */
public class MapCrudTestCase {

    // map 方式映射到表
    @Test
    public void insertByBean() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            Map<String, Object> userData = new LinkedHashMap<>();
            userData.put("id", 100);
            userData.put("age", 36);
            userData.put("name", "default user");
            userData.put("create_time", new Date());// Map 方式下 key 就是列名

            InsertOperation<Map<String, Object>> lambdaInsert = lambdaTemplate.insertByTable("user_table");
            assert 1 == lambdaInsert.applyEntity(userData).executeSumResult();

            // 校验结果（默认大小写不敏感，使用小写ID属性名反查）
            MapQueryOperation lambdaQuery1 = lambdaTemplate.queryByTable("user_table").asMap();
            Map<String, Object> resultData1 = lambdaQuery1.eq("ID", userData.get("id")).queryForObject();
            assert resultData1.get("name").equals(userData.get("name"));

            // 校验结果（默认大小写不敏感，使用大写ID属性名反查）
            MapQueryOperation lambdaQuery2 = lambdaTemplate.queryByTable("user_table").asMap();
            Map<String, Object> resultData2 = lambdaQuery2.eq("ID", userData.get("id")).queryForObject();
            assert resultData2.get("name").equals(userData.get("name"));
        }
    }

    // 简单的将普通 map 映射到表。偶尔数据是以 map 形式出现，为了避免 map/bean 的转换，允许直接将 map 插入到数据
    @Test
    public void insertByMap() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            Map<String, Object> userData = new LinkedHashMap<>();
            userData.put("id", 100);
            userData.put("age", 36);
            userData.put("name", "default user");
            userData.put("create_time", new Date());// Map 方式下 key 就是列名

            InsertOperation<Map<String, Object>> lambdaInsert = lambdaTemplate.insertByTable("user_table");
            assert 1 == lambdaInsert.applyMap(userData).executeSumResult();

            // 校验结果（默认大小写不敏感，使用小写ID属性名反查）
            MapQueryOperation lambdaQuery1 = lambdaTemplate.queryByTable("user_table").asMap();
            Map<String, Object> resultData1 = lambdaQuery1.eq("ID", userData.get("id")).queryForObject();
            assert resultData1.get("name").equals(userData.get("name"));

            // 校验结果（默认大小写不敏感，使用大写ID属性名反查）
            MapQueryOperation lambdaQuery2 = lambdaTemplate.queryByTable("user_table").asMap();
            Map<String, Object> resultData2 = lambdaQuery2.eq("ID", userData.get("id")).queryForObject();
            assert resultData2.get("name").equals(userData.get("name"));
        }
    }

    // 基于 map 的 条件更新,只更新一个列
    @Test
    public void updateOneColumn() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            // update user set name = 'new name is abc' where id = 1
            lambdaTemplate.updateByTable("user_table").asMap() //
                    .eq("id", 1)             //
                    .updateTo("name", "new name is abc")//
                    .doUpdate();

            // 校验结果
            MapQueryOperation lambdaQuery = lambdaTemplate.queryByTable("user_table").asMap();
            Map<String, Object> resultData = lambdaQuery.eq("id", 1).queryForObject();
            assert resultData.get("name").equals("new name is abc");
        }
    }

    // 基于 map 的 条件更新, 更新多个列
    @Test
    public void updateMultipleColumn() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            // update user set name = 'new name is abc', age = 120 where id = 1
            lambdaTemplate.updateByTable("user_table").asMap()  //
                    .eq("id", 1)        //
                    .updateTo("name", "new name is abc")//
                    .updateTo("age", 120)//
                    .doUpdate();

            // 校验结果
            MapQueryOperation lambdaQuery = lambdaTemplate.queryByTable("user_table").asMap();
            Map<String, Object> resultData = lambdaQuery.eq("id", 1).queryForObject();
            assert resultData.get("name").equals("new name is abc");
            assert resultData.get("age").equals(120);
        }
    }

    // 基于 map 的条件更新, 为当更新的列比较多的时候可以使用 map 来存放新数据
    @Test
    public void updateByMap() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            Map<String, Object> newValue = new HashMap<>();
            newValue.put("name", "new name is abc");
            newValue.put("age", 120);

            // update user set name = 'new name is abc', age = 120 where id = 1
            lambdaTemplate.updateByTable("user_table") //
                    .eq("id", 1)//
                    .updateToSampleMap(newValue)   //
                    .doUpdate();

            // 校验结果
            MapQueryOperation lambdaQuery = lambdaTemplate.queryByTable("user_table").asMap();
            Map<String, Object> resultData = lambdaQuery.eq("id", 1).queryForObject();
            assert resultData.get("name").equals("new name is abc");
            assert resultData.get("age").equals(120);
        }
    }

    // 基于 map 的条件更新, 使用 DTO 来存放新数据，不需要更新的列不设置值
    @Test
    public void updateBySample() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            Map<String, Object> newData = new HashMap<>();
            newData.put("name", "new name is abc");
            newData.put("age", 120);

            // update user set name = 'new name is abc', age = 120 where id = 1
            lambdaTemplate.updateByTable("user_table") //
                    .eq("id", 1) //
                    .updateToSample(newData)  // updateBySample 在 map 模式下和 updateByMap 行为一样；
                    .doUpdate();

            // 校验结果
            MapQueryOperation lambdaQuery = lambdaTemplate.queryByTable("user_table").asMap();
            Map<String, Object> resultData = lambdaQuery.eq("id", 1).queryForObject();
            assert resultData.get("name").equals("new name is abc");
            assert resultData.get("age").equals(120);

        }
    }

    // 基于 map 的条件更新, 整行更新（危险操作）
    @Test
    public void updateRow() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            // 除了 name 和 pk 之外，其它列应该都是 null。
            Map<String, Object> newData = new HashMap<>();
            newData.put("id", 1);
            newData.put("name", "new name is abc");

            // update user set name = 'new name is abc', age = 120 where id = 1
            int i = lambdaTemplate.updateByTable("user_table") //
                    .eq("id", 1) //
                    .updateRow(newData)  //
                    .doUpdate();
            assert i == 1;

            // 校验结果（不同于 DTO 模式，只会更新 newData 中包含的列）
            MapQueryOperation lambdaQuery = lambdaTemplate.queryByTable("user_table").asMap();
            Map<String, Object> resultData = lambdaQuery.eq("id", 1).queryForObject();
            assert resultData.get("id").equals(1);
            assert resultData.get("name").equals("new name is abc");
            assert resultData.get("age").equals(26);
            assert resultData.get("create_time") != null;
        }
    }

    // 基于 map 的条件更新, 变更主键 ID（危险操作）
    @Test
    public void updatePK() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            // 除了 name 和 pk 之外，其它列应该都是 null。
            Map<String, Object> newData = new HashMap<>();
            newData.put("id", 112);
            newData.put("name", "new name is abc");

            // update user set name = 'new name is abc', age = 120 where id = 1
            int i = lambdaTemplate.updateByTable("user_table") //
                    .eq("id", 1) //
                    .allowUpdateKey()  // 需要启用 allowUpdateKey
                    .updateToSample(newData)  //
                    .doUpdate();
            assert i == 1;

            // 通过新 id 反查数据
            MapQueryOperation lambdaQuery = lambdaTemplate.queryByTable("user_table").asMap();
            Map<String, Object> resultData = lambdaQuery.eq("id", 112).queryForObject();
            assert resultData.get("id").equals(112);
            assert resultData.get("name").equals("new name is abc");
            assert resultData.get("age") != null;
            assert resultData.get("create_time") != null;
        }
    }

    // 基于条件删除
    @Test
    public void deleteByID() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            // delete from user where id = 1;
            int i = lambdaTemplate.deleteByTable("user_table") //
                    .eq("id", 1) //
                    .doDelete();
            assert i == 1;

            // 校验结果
            MapQueryOperation lambdaQuery = lambdaTemplate.queryByTable("user_table").asMap();
            Map<String, Object> resultData = lambdaQuery.eq("id", 1).queryForObject();
            assert resultData == null;
        }
    }

    // 基于 map 样本的条件删除
    @Test
    public void deleteBySample() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            // 条件对象
            Map<String, Object> sample = new HashMap<>();
            sample.put("id", 1);
            sample.put("name", "mali");

            // delete from user where id = 1 and name = 'mail';
            int i = lambdaTemplate.deleteByTable("user_table") //
                    .eqBySample(sample)//
                    .doDelete();
            assert i == 1;

            // 校验结果
            MapQueryOperation lambdaQuery = lambdaTemplate.queryByTable("user_table").asMap();
            Map<String, Object> resultData = lambdaQuery.eq("id", 1).queryForObject();
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
            int i = lambdaTemplate.deleteByTable("user_table") //
                    .eqBySampleMap(newValue)//
                    .doDelete();
            assert i == 1;

            // 校验结果
            MapQueryOperation lambdaQuery = lambdaTemplate.queryByTable("user_table").asMap();
            Map<String, Object> resultData = lambdaQuery.eq("id", 1).queryForObject();
            assert resultData == null;
        }
    }

    // 全部删除
    @Test
    public void deleteALL() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            // delete from user;
            int i = lambdaTemplate.deleteByTable("user_table") //
                    .allowEmptyWhere()// 无条件删除需要启用空条件
                    .doDelete();
            assert i == 5;

            // 校验结果
            assert lambdaTemplate.queryByTable("user_table").queryForCount() == 0;
        }
    }

    // 简单的将普通 map 映射到表
    @Test
    public void batchInsertByMap() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            InsertOperation<Map<String, Object>> lambdaInsert = lambdaTemplate.insertByTable("user_table");
            for (int i = 0; i < 10; i++) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", i + 10);
                userData.put("age", 36);
                userData.put("name", "default user " + i);
                userData.put("create_time", new Date());
                lambdaInsert.applyEntity(userData);
            }
            int res = lambdaInsert.executeSumResult();
            assert res == 10;

            // 校验结果
            EntityQueryOperation<UserTableDTO> lambdaQuery = lambdaTemplate.queryByEntity(UserTableDTO.class);
            List<UserTableDTO> resultData = lambdaQuery.likeRight(UserTableDTO::getName, "default user ").queryForList();
            List<String> result = resultData.stream().map(UserTableDTO::getName).collect(Collectors.toList());
            assert result.size() == 10;

            for (int i = 0; i < 10; i++) {
                assert result.get(i).equals("default user " + i);
            }
        }
    }
}