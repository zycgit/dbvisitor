package net.hasor.scene.singletable;
import net.hasor.dbvisitor.lambda.InsertOperation;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.lambda.MapQueryOperation;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/** 使用 MAP 映射数据库 CRUD */
public class MapCrudTestCase {

    // map 方式映射到表
    @Test
    public void insertByBean() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            Map<String, Object> userData = new HashMap<>();
            userData.put("age", 36);
            userData.put("name", "default user");
            userData.put("create_time", new Date());// Map 方式下 key 就是列名

            InsertOperation<Map<String, Object>> lambdaInsert = lambdaTemplate.lambdaInsert("user");
            int res = lambdaInsert.applyEntity(userData).executeSumResult();
            assert res == 1;

            // 校验结果
            MapQueryOperation lambdaQuery = lambdaTemplate.lambdaQuery("user");
            Map<String, Object> resultData = lambdaQuery.eq("name", "default user").queryForObject();
            assert resultData.get("name").equals(userData.get("name"));
        }
    }

    // 简单的将普通 map 映射到表。偶尔数据是以 map 形式出现，为了避免 map/bean 的转换，允许直接将 map 插入到数据
    @Test
    public void insertByMap() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            Map<String, Object> userData = new HashMap<>();
            userData.put("age", 36);
            userData.put("name", "default user");
            userData.put("create_time", new Date()); // Map 方式下 key 就是列名

            InsertOperation<Map<String, Object>> lambdaInsert = lambdaTemplate.lambdaInsert("user");
            int res = lambdaInsert.applyMap(userData).executeSumResult();
            assert res == 1;

            // 校验结果
            MapQueryOperation lambdaQuery = lambdaTemplate.lambdaQuery("user");
            Map<String, Object> resultData = lambdaQuery.eq("name", "default user").queryForObject();
            assert resultData.get("name").equals(userData.get("name"));
        }
    }

    // 基于 map 的 条件更新,只更新一个列
    @Test
    public void updateOneColumn() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            // update user set name = 'new name is abc' where id = 1
            lambdaTemplate.lambdaUpdate("user") //
                    .eq("id", 1)         //
                    .updateTo("name", "new name is abc")//
                    .doUpdate();

            // 校验结果
            MapQueryOperation lambdaQuery = lambdaTemplate.lambdaQuery("user");
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
            lambdaTemplate.lambdaUpdate("user")  //
                    .eq("id", 1)        //
                    .updateToAdd("name", "new name is abc")//
                    .updateToAdd("age", 120)//
                    .doUpdate();

            // 校验结果
            MapQueryOperation lambdaQuery = lambdaTemplate.lambdaQuery("user");
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
            lambdaTemplate.lambdaUpdate("user") //
                    .eq("id", 1)//
                    .updateByMap(newValue)   //
                    .doUpdate();

            // 校验结果
            MapQueryOperation lambdaQuery = lambdaTemplate.lambdaQuery("user");
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
            lambdaTemplate.lambdaUpdate("user") //
                    .eq("id", 1) //
                    .updateBySample(newData)  // updateBySample 在 map 模式下和 updateByMap 行为一样；
                    .doUpdate();

            // 校验结果
            MapQueryOperation lambdaQuery = lambdaTemplate.lambdaQuery("user");
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
            newData.put("name", "new name is abc");

            // update user set name = 'new name is abc', age = 120 where id = 1
            int i = lambdaTemplate.lambdaUpdate("user") //
                    .eq("id", 1) //
                    .allowReplaceRow()  // 整行更新需要通过 allowReplaceRow 开启
                    .updateTo(newData)  //
                    .doUpdate();
            assert i == 1;

            // 校验结果（除 id 和 name 外全部都被设置为空了）
            MapQueryOperation lambdaQuery = lambdaTemplate.lambdaQuery("user");
            Map<String, Object> resultData = lambdaQuery.eq("id", 1).queryForObject();
            assert resultData.get("id").equals(1);
            assert resultData.get("name").equals("new name is abc");
            assert resultData.get("age") == null;
            assert resultData.get("create_time") == null;
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
            int i = lambdaTemplate.lambdaUpdate("user") //
                    .eq("id", 1) //
                    .allowUpdateKey()  // 需要启用 allowUpdateKey
                    .updateBySample(newData)  //
                    .doUpdate();
            assert i == 1;

            // 通过新 id 反查数据
            MapQueryOperation lambdaQuery = lambdaTemplate.lambdaQuery("user");
            Map<String, Object> resultData = lambdaQuery.eq("id", 112).queryForObject();
            assert resultData.get("id").equals(112);
            assert resultData.get("name").equals("new name is abc");
            assert resultData.get("age") != null;
            assert resultData.get("create_time") != null;
        }
    }
}