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
package net.hasor.dbvisitor.wrapper;
import net.hasor.dbvisitor.wrapper.dto.AnnoUserInfoDTO;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import static net.hasor.test.utils.TestUtils.beanForData1;

/***
 * Lambda 方式执行 Update 操作
 * @version : 2021-3-22
 * @author 赵永春 (zyc@hasor.net)
 */
public class DoEntUpdateTest {
    @Test
    public void update_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            int update = new WrapperAdapter(c).updateByEntity(AnnoUserInfoDTO.class)//
                    .updateTo(AnnoUserInfoDTO::getName, "aaa")//
                    .allowEmptyWhere().doUpdate();
            assert update == 3;
        }
    }

    @Test
    public void update_1_2map() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            int update = new WrapperAdapter(c).updateByEntity(AnnoUserInfoDTO.class).asMap()//
                    .updateTo("name", "aaa")//
                    .allowEmptyWhere().doUpdate();
            assert update == 3;
        }
    }

    @Test
    public void allowEmptyWhere_1() {
        try (Connection c = DsUtils.h2Conn()) {
            new WrapperAdapter(c).updateByEntity(AnnoUserInfoDTO.class)//
                    .updateTo(AnnoUserInfoDTO::getName, "aaa")//
                    .doUpdate();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("The dangerous UPDATE operation, You must call `allowEmptyWhere()` to enable UPDATE ALL.");
        }
    }

    @Test
    public void allowEmptyWhere_1_2map() {
        try (Connection c = DsUtils.h2Conn()) {
            new WrapperAdapter(c).updateByEntity(AnnoUserInfoDTO.class).asMap()//
                    .updateTo("name", "aaa")//
                    .doUpdate();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("The dangerous UPDATE operation, You must call `allowEmptyWhere()` to enable UPDATE ALL.");
        }
    }

    @Test
    public void updateToSample_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            // update
            AnnoUserInfoDTO value = new AnnoUserInfoDTO();
            value.setName("abc");
            value.setPassword("def");

            new WrapperAdapter(c).updateByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .updateToSample(value)//
                    .doUpdate();

            // check
            AnnoUserInfoDTO tbUser2 = new WrapperAdapter(c).queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .queryForObject();
            assert tbUser2.getName().equals("abc");
            assert tbUser2.getPassword().equals("def");
        }
    }

    @Test
    public void updateToSample_1_2map() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            // update
            HashMap<String, Object> value = new HashMap<>();
            value.put("name", "abc");
            value.put("password", "def");

            new WrapperAdapter(c).updateByEntity(AnnoUserInfoDTO.class).asMap()//
                    .eq("loginName", "muhammad")//
                    .updateToSample(value)//
                    .doUpdate();

            // check
            AnnoUserInfoDTO tbUser2 = new WrapperAdapter(c).queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .queryForObject();
            assert tbUser2.getName().equals("abc");
            assert tbUser2.getPassword().equals("def");
        }
    }

    @Test
    public void updateToSampleMap_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambda = new WrapperAdapter(c);

            // before
            AnnoUserInfoDTO tbUser1 = lambda.queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, beanForData1().getLoginName())//
                    .queryForObject();
            assert tbUser1.getName() != null;
            assert tbUser1.getPassword() != null;

            // update
            HashMap<String, Object> valueMap = new HashMap<>();
            valueMap.put("name", "abc");
            valueMap.put("password", "pwd");

            int update = lambda.updateByEntity(AnnoUserInfoDTO.class).eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .updateToSampleMap(valueMap)//
                    .doUpdate();
            assert update == 1;

            // check
            AnnoUserInfoDTO tbUser2 = lambda.queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .queryForObject();
            assert tbUser2.getName().equals("abc");
            assert tbUser2.getPassword().equals("pwd");
        }
    }

    @Test
    public void updateToSampleMap_1_2map() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambda = new WrapperAdapter(c);

            // before
            AnnoUserInfoDTO tbUser1 = lambda.queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, beanForData1().getLoginName())//
                    .queryForObject();
            assert tbUser1.getName() != null;
            assert tbUser1.getPassword() != null;

            // update
            HashMap<String, Object> valueMap = new HashMap<>();
            valueMap.put("name", "abc");
            valueMap.put("password", "pwd");

            int update = lambda.updateByEntity(AnnoUserInfoDTO.class).asMap()//
                    .eq("loginName", "muhammad")//
                    .updateToSampleMap(valueMap)//
                    .doUpdate();
            assert update == 1;

            // check
            AnnoUserInfoDTO tbUser2 = lambda.queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .queryForObject();
            assert tbUser2.getName().equals("abc");
            assert tbUser2.getPassword().equals("pwd");
        }
    }

    @Test
    public void updateToSampleMap_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambda = new WrapperAdapter(c);

            // before
            AnnoUserInfoDTO tbUser1 = lambda.queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, beanForData1().getLoginName())//
                    .queryForObject();
            assert tbUser1.getName() != null;
            assert tbUser1.getPassword() != null;

            // update
            HashMap<String, Object> valueMap = new HashMap<>();
            valueMap.put("name", "abc");
            valueMap.put("password", "pwd");

            int update = lambda.updateByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .updateToSampleMap(valueMap, s -> s.equals("name"))//
                    .doUpdate();
            assert update == 1;

            // check
            AnnoUserInfoDTO tbUser2 = lambda.queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .queryForObject();
            assert tbUser2.getName().equals("abc");
            assert !tbUser2.getPassword().equals("pwd");
        }
    }

    @Test
    public void updateToSampleMap_2_2map() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambda = new WrapperAdapter(c);

            // before
            AnnoUserInfoDTO tbUser1 = lambda.queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, beanForData1().getLoginName())//
                    .queryForObject();
            assert tbUser1.getName() != null;
            assert tbUser1.getPassword() != null;

            // update
            HashMap<String, Object> valueMap = new HashMap<>();
            valueMap.put("name", "abc");
            valueMap.put("password", "pwd");

            int update = lambda.updateByEntity(AnnoUserInfoDTO.class).asMap()//
                    .eq("loginName", "muhammad")//
                    .updateToSampleMap(valueMap, s -> s.equals("name"))//
                    .doUpdate();
            assert update == 1;

            // check
            AnnoUserInfoDTO tbUser2 = lambda.queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .queryForObject();
            assert tbUser2.getName().equals("abc");
            assert !tbUser2.getPassword().equals("pwd");
        }
    }

    @Test
    public void update_to_null_1() {
        try (Connection c = DsUtils.h2Conn()) {
            AnnoUserInfoDTO value = new AnnoUserInfoDTO();
            value.setName(null);
            value.setPassword(null);

            // update
            new WrapperAdapter(c).updateByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .updateToSample(value)//
                    .doUpdate();
            assert false;

        } catch (Exception e) {
            assert e.getMessage().equals("there nothing to update.");
        }

        try (Connection c = DsUtils.h2Conn()) {
            Map<String, Object> value = new HashMap<>();
            value.put("name", null);
            value.put("password", null);

            new WrapperAdapter(c).updateByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .updateToSampleMap(value)//
                    .doUpdate();
        } catch (Exception e) {
            assert e.getMessage().equals("there nothing to update.");
        }
    }

    @Test
    public void update_to_null_1_2map() {
        Map<String, Object> value = new HashMap<>();
        value.put("name", null);
        value.put("password", null);

        try (Connection c = DsUtils.h2Conn()) {
            // update
            new WrapperAdapter(c).updateByEntity(AnnoUserInfoDTO.class).asMap()//
                    .eq("loginName", "muhammad")//
                    .updateToSample(value)//
                    .doUpdate();
            assert false;

        } catch (Exception e) {
            assert e.getMessage().equals("there nothing to update.");
        }

        try (Connection c = DsUtils.h2Conn()) {
            new WrapperAdapter(c).updateByEntity(AnnoUserInfoDTO.class).asMap()//
                    .eq("loginName", "muhammad")//
                    .updateToSampleMap(value)//
                    .doUpdate();
        } catch (Exception e) {
            assert e.getMessage().equals("there nothing to update.");
        }
    }

    @Test
    public void update_to_null_2() {
        try (Connection c = DsUtils.h2Conn()) {
            AnnoUserInfoDTO value = new AnnoUserInfoDTO();
            value.setName(null);
            value.setPassword("abc");

            // update
            new WrapperAdapter(c).updateByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .updateToSample(value, s -> s.equals("name"))//
                    .doUpdate();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("there nothing to update.");
        }

        try (Connection c = DsUtils.h2Conn()) {
            Map<String, Object> value = new HashMap<>();
            value.put("name", null);
            value.put("password", "abc");

            new WrapperAdapter(c).updateByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .updateToSampleMap(value, s -> s.equals("name"))//
                    .doUpdate();
        } catch (Exception e) {
            assert e.getMessage().equals("there nothing to update.");
        }
    }

    @Test
    public void update_to_null_2_2map() {
        Map<String, Object> value = new HashMap<>();
        value.put("name", null);
        value.put("password", "abc");

        try (Connection c = DsUtils.h2Conn()) {
            // update
            new WrapperAdapter(c).updateByEntity(AnnoUserInfoDTO.class).asMap()//
                    .eq("loginName", "muhammad")//
                    .updateToSample(value, s -> s.equals("name"))//
                    .doUpdate();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("there nothing to update.");
        }

        try (Connection c = DsUtils.h2Conn()) {
            new WrapperAdapter(c).updateByEntity(AnnoUserInfoDTO.class).asMap()//
                    .eq("loginName", "muhammad")//
                    .updateToSampleMap(value, s -> s.equals("name"))//
                    .doUpdate();
        } catch (Exception e) {
            assert e.getMessage().equals("there nothing to update.");
        }
    }

    @Test
    public void updateTo_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            // update
            int update = new WrapperAdapter(c).updateByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .updateTo(AnnoUserInfoDTO::getName, "aabbcc")//
                    .doUpdate();
            assert update == 1;

            // check
            AnnoUserInfoDTO tbUser2 = new WrapperAdapter(c).queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .queryForObject();
            assert tbUser2.getName().equals("aabbcc");
        }
    }

    @Test
    public void updateTo_1_2map() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            // update
            int update = new WrapperAdapter(c).updateByEntity(AnnoUserInfoDTO.class).asMap()//
                    .eq("loginName", "muhammad")//
                    .updateTo("name", "aabbcc")//
                    .doUpdate();
            assert update == 1;

            // check
            AnnoUserInfoDTO tbUser2 = new WrapperAdapter(c).queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .queryForObject();
            assert tbUser2.getName().equals("aabbcc");
        }
    }

    @Test
    public void updateTo_2() {
        try (Connection c = DsUtils.h2Conn()) {
            // update
            new WrapperAdapter(c).updateByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .updateTo(false, AnnoUserInfoDTO::getName, "aabbcc")//
                    .doUpdate();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("there nothing to update.");
        }
    }

    @Test
    public void updateTo_2_2map() {
        try (Connection c = DsUtils.h2Conn()) {
            // update
            new WrapperAdapter(c).updateByEntity(AnnoUserInfoDTO.class).asMap()//
                    .eq("loginName", "muhammad")//
                    .updateTo(false, "loginName", "aabbcc")//
                    .doUpdate();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("there nothing to update.");
        }
    }

    @Test
    public void updateToUsingStr_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambda = new WrapperAdapter(c);
            // update
            EntityUpdateWrapper<AnnoUserInfoDTO> lambdaUpdate = lambda.updateByEntity(AnnoUserInfoDTO.class);
            int update = lambdaUpdate.eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .updateToUsingStr("name", "aabbcc")//
                    .doUpdate();
            assert update == 1;

            // check
            AnnoUserInfoDTO tbUser2 = lambda.queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .queryForObject();
            assert tbUser2.getName().equals("aabbcc");
        }
    }

    @Test
    public void updateToUsingStr_1_2map() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambda = new WrapperAdapter(c);
            // update
            MapUpdateWrapper lambdaUpdate = lambda.updateByEntity(AnnoUserInfoDTO.class).asMap();
            int update = lambdaUpdate.eq("loginName", "muhammad")//
                    .updateToUsingStr("name", "aabbcc")//
                    .doUpdate();
            assert update == 1;

            // check
            AnnoUserInfoDTO tbUser2 = lambda.queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .queryForObject();
            assert tbUser2.getName().equals("aabbcc");
        }
    }

    @Test
    public void updateToUsingStr_2() {
        try (Connection c = DsUtils.h2Conn()) {
            // update
            new WrapperAdapter(c).updateByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .updateToUsingStr(false, "name", "aabbcc")//
                    .doUpdate();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("there nothing to update.");
        }
    }

    @Test
    public void updateToUsingStr_2_2map() {
        try (Connection c = DsUtils.h2Conn()) {
            // update
            new WrapperAdapter(c).updateByEntity(AnnoUserInfoDTO.class).asMap()//
                    .eq("loginName", "muhammad")//
                    .updateToUsingStr(false, "name", "aabbcc")//
                    .doUpdate();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("there nothing to update.");
        }
    }

    @Test
    public void updateRow_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambda = new WrapperAdapter(c);

            // before
            AnnoUserInfoDTO tbUser1 = lambda.queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, beanForData1().getLoginName())//
                    .queryForObject();

            // update
            tbUser1.setName("abc");
            tbUser1.setPassword("pwd");

            EntityUpdateWrapper<AnnoUserInfoDTO> lambdaUpdate = lambda.updateByEntity(AnnoUserInfoDTO.class);
            int update = lambdaUpdate.eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .allowUpdateKey()   //
                    .updateRow(tbUser1) //
                    .doUpdate();
            assert update == 1;

            // check
            AnnoUserInfoDTO tbUser2 = lambda.queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .queryForObject();
            assert tbUser2.getName().equals("abc");
            assert tbUser2.getPassword().equals("pwd");
        }
    }

    @Test
    public void updateRow_1_2map() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambda = new WrapperAdapter(c);

            // before
            Map<String, Object> tbUser1 = lambda.queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, beanForData1().getLoginName())//
                    .queryForMap();

            // update
            tbUser1.put("name", "abc");
            tbUser1.put("password", "pwd");

            MapUpdateWrapper lambdaUpdate = lambda.updateByEntity(AnnoUserInfoDTO.class).asMap();
            int update = lambdaUpdate.eq("loginName", "muhammad")//
                    .allowUpdateKey()   //
                    .updateRow(tbUser1) //
                    .doUpdate();
            assert update == 1;

            // check
            AnnoUserInfoDTO tbUser2 = lambda.queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .queryForObject();
            assert tbUser2.getName().equals("abc");
            assert tbUser2.getPassword().equals("pwd");
        }
    }

    @Test
    public void updateRow_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambda = new WrapperAdapter(c);

            // before
            AnnoUserInfoDTO tbUser1 = lambda.queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, beanForData1().getLoginName())//
                    .queryForObject();

            // update
            tbUser1.setName("abc");
            tbUser1.setPassword("pwd");

            int update = lambda.updateByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .allowUpdateKey()   //
                    .updateRow(tbUser1, s -> s.equals("name")) //
                    .doUpdate();
            assert update == 1;

            // check
            AnnoUserInfoDTO tbUser2 = lambda.queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .queryForObject();
            assert tbUser2.getName().equals("abc");
            assert !tbUser2.getPassword().equals("pwd");
        }
    }

    @Test
    public void updateRow_2_2map() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambda = new WrapperAdapter(c);

            // before
            Map<String, Object> tbUser1 = lambda.queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, beanForData1().getLoginName())//
                    .queryForMap();

            // update
            tbUser1.put("name", "abc");
            tbUser1.put("password", "pwd");

            int update = lambda.updateByEntity(AnnoUserInfoDTO.class).asMap()//
                    .eq("loginName", "muhammad")//
                    .allowUpdateKey()   //
                    .updateRow(tbUser1, s -> s.equals("name")) //
                    .doUpdate();
            assert update == 1;

            // check
            AnnoUserInfoDTO tbUser2 = lambda.queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .queryForObject();
            assert tbUser2.getName().equals("abc");
            assert !tbUser2.getPassword().equals("pwd");
        }
    }

    @Test
    public void updateRow_3() {
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambda = new WrapperAdapter(c);

            // before
            AnnoUserInfoDTO tbUser1 = lambda.queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, beanForData1().getLoginName())//
                    .queryForObject();

            // update
            tbUser1.setName("abc");
            tbUser1.setPassword("pwd");

            lambda.updateByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .updateRow(tbUser1) //
                    .doUpdate();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("The dangerous UPDATE operation, You must call `allowUpdateKey()` to enable UPDATE PrimaryKey.");
        }
    }

    @Test
    public void updateRow_3_2map() {
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambda = new WrapperAdapter(c);

            // before
            Map<String, Object> tbUser1 = lambda.queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, beanForData1().getLoginName())//
                    .queryForMap();

            // update
            tbUser1.put("name", "abc");
            tbUser1.put("password", "pwd");

            lambda.updateByEntity(AnnoUserInfoDTO.class).asMap()//
                    .eq("loginName", "muhammad")//
                    .updateRow(tbUser1) //
                    .doUpdate();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("The dangerous UPDATE operation, You must call `allowUpdateKey()` to enable UPDATE PrimaryKey.");
        }
    }

    @Test
    public void updateRowUsingMap_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambda = new WrapperAdapter(c);

            // before
            Map<String, Object> tbUser1 = lambda.queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, beanForData1().getLoginName())//
                    .queryForMap();

            // update
            tbUser1.put("name", "abc");
            tbUser1.remove("password");

            EntityUpdateWrapper<AnnoUserInfoDTO> lambdaUpdate = lambda.updateByEntity(AnnoUserInfoDTO.class);
            int update = lambdaUpdate.eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .allowUpdateKey()   //
                    .updateRowUsingMap(tbUser1) //
                    .doUpdate();
            assert update == 1;

            // check
            AnnoUserInfoDTO tbUser2 = lambda.queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .queryForObject();
            assert tbUser2.getName().equals("abc");
            assert tbUser2.getPassword() == null;
        }
    }

    @Test
    public void updateRowUsingMap_1_2map() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambda = new WrapperAdapter(c);

            // before
            Map<String, Object> tbUser1 = lambda.queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, beanForData1().getLoginName())//
                    .queryForMap();

            // update
            tbUser1.put("name", "abc");
            tbUser1.remove("password");

            int update = lambda.updateByEntity(AnnoUserInfoDTO.class).asMap()//
                    .eq("loginName", "muhammad")//
                    .allowUpdateKey()   //
                    .updateRowUsingMap(tbUser1) //
                    .doUpdate();
            assert update == 1;

            // check
            AnnoUserInfoDTO tbUser2 = lambda.queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .queryForObject();
            assert tbUser2.getName().equals("abc");
            assert tbUser2.getPassword() == null;
        }
    }

    @Test
    public void updateRowUsingMap_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambda = new WrapperAdapter(c);

            // before
            Map<String, Object> tbUser1 = lambda.queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, beanForData1().getLoginName())//
                    .queryForMap();

            // update
            tbUser1.put("name", "abc");
            tbUser1.remove("password");

            EntityUpdateWrapper<AnnoUserInfoDTO> lambdaUpdate = lambda.updateByEntity(AnnoUserInfoDTO.class);
            int update = lambdaUpdate.eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .allowUpdateKey() //
                    .updateRowUsingMap(tbUser1, s -> s.equals("name")) //
                    .doUpdate();
            assert update == 1;

            // check
            AnnoUserInfoDTO tbUser2 = lambda.queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .queryForObject();
            assert tbUser2.getName().equals("abc");
            assert tbUser2.getPassword() != null;
        }
    }

    @Test
    public void updateRowUsingMap_2_2map() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambda = new WrapperAdapter(c);

            // before
            Map<String, Object> tbUser1 = lambda.queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, beanForData1().getLoginName())//
                    .queryForMap();

            // update
            tbUser1.put("name", "abc");
            tbUser1.remove("password");

            int update = lambda.updateByEntity(AnnoUserInfoDTO.class).asMap()//
                    .eq("loginName", "muhammad")//
                    .allowUpdateKey() //
                    .updateRowUsingMap(tbUser1, s -> s.equals("name")) //
                    .doUpdate();
            assert update == 1;

            // check
            AnnoUserInfoDTO tbUser2 = lambda.queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .queryForObject();
            assert tbUser2.getName().equals("abc");
            assert tbUser2.getPassword() != null;
        }
    }

    @Test
    public void updatePK_0_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            int update = new WrapperAdapter(c).updateByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .allowUpdateKey()//
                    .updateTo(AnnoUserInfoDTO::getUid, "123321")//
                    .doUpdate();
            assert update == 1;

            AnnoUserInfoDTO tbUser2 = new WrapperAdapter(c).queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad").queryForObject();
            assert tbUser2.getUid().equals("123321");
        }
    }

    @Test
    public void updatePK_0_1_2map() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            int update = new WrapperAdapter(c).updateByEntity(AnnoUserInfoDTO.class).asMap()//
                    .eq("loginName", "muhammad")//
                    .allowUpdateKey()//
                    .updateTo("uid", "123321")//
                    .doUpdate();
            assert update == 1;

            AnnoUserInfoDTO tbUser2 = new WrapperAdapter(c).queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad").queryForObject();
            assert tbUser2.getUid().equals("123321");
        }
    }

    @Test
    public void updatePK_0_2() {
        try (Connection c = DsUtils.h2Conn()) {
            new WrapperAdapter(c).updateByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    //.allowUpdateKey()//
                    .updateTo(AnnoUserInfoDTO::getUid, "123321")//
                    .doUpdate();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("The dangerous UPDATE operation, You must call `allowUpdateKey()` to enable UPDATE PrimaryKey.");
        }
    }

    @Test
    public void updatePK_0_2_2map() {
        try (Connection c = DsUtils.h2Conn()) {
            new WrapperAdapter(c).updateByEntity(AnnoUserInfoDTO.class).asMap()//
                    .eq("loginName", "muhammad")//
                    //.allowUpdateKey()//
                    .updateTo("uid", "123321")//
                    .doUpdate();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("The dangerous UPDATE operation, You must call `allowUpdateKey()` to enable UPDATE PrimaryKey.");
        }
    }

    @Test
    public void updatePK_1_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {

            HashMap<String, Object> valueMap = new HashMap<>();
            valueMap.put("uid", "123321");

            int update = new WrapperAdapter(c).updateByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .allowUpdateKey()//
                    .updateToSampleMap(valueMap)//
                    .doUpdate();
            assert update == 1;

            AnnoUserInfoDTO tbUser2 = new WrapperAdapter(c).queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad").queryForObject();
            assert tbUser2.getUid().equals("123321");
        }
    }

    @Test
    public void updatePK_1_1_2map() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {

            HashMap<String, Object> valueMap = new HashMap<>();
            valueMap.put("uid", "123321");

            int update = new WrapperAdapter(c).updateByEntity(AnnoUserInfoDTO.class).asMap()//
                    .eq("loginName", "muhammad")//
                    .allowUpdateKey()//
                    .updateToSampleMap(valueMap)//
                    .doUpdate();
            assert update == 1;

            AnnoUserInfoDTO tbUser2 = new WrapperAdapter(c).queryByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad").queryForObject();
            assert tbUser2.getUid().equals("123321");
        }
    }

    @Test
    public void updatePK_1_2() {
        try (Connection c = DsUtils.h2Conn()) {
            HashMap<String, Object> valueMap = new HashMap<>();
            valueMap.put("uid", "123321");

            new WrapperAdapter(c).updateByEntity(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    //.allowUpdateKey()//
                    .updateToSampleMap(valueMap)//
                    .doUpdate();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("The dangerous UPDATE operation, You must call `allowUpdateKey()` to enable UPDATE PrimaryKey.");
        }
    }

    @Test
    public void updatePK_1_2_2map() {
        try (Connection c = DsUtils.h2Conn()) {
            HashMap<String, Object> valueMap = new HashMap<>();
            valueMap.put("uid", "123321");

            new WrapperAdapter(c).updateByEntity(AnnoUserInfoDTO.class).asMap()//
                    .eq("loginName", "muhammad")//
                    //.allowUpdateKey()//
                    .updateToSampleMap(valueMap)//
                    .doUpdate();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().equals("The dangerous UPDATE operation, You must call `allowUpdateKey()` to enable UPDATE PrimaryKey.");
        }
    }
}
