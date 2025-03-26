///*
// * Copyright 2015-2022 the original author or authors.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package net.hasor.dbvisitor.session.dto;
//import net.hasor.dbvisitor.mapper.Param;
//import net.hasor.dbvisitor.mapper.Query;
//import net.hasor.dbvisitor.mapper.SimpleMapper;
//
//import java.util.List;
//import java.util.Map;
//
///**
// * @author 赵永春 (zyc@hasor.net)
// * @version 2013-12-10
// */
//@SimpleMapper
//public interface MixMapperByAnno {
//    @Query(value = "select * from console_job where aac = #{abc}")
//    List<UserInfo> selectList(String abc);
//
//    @Query(value = "select * from t_blog where title = #{title} and content = #{content}")
//    UserInfo selectOne(String title, String content);
//
//    @Query(value = "select * from alert_detail where alert_detail.event_type in @{in, arg0}")
//    List<UserInfo> testForeach(List<String> eventTypes);
//
//    @Query(value = "select * from project_info where status = 2 @{and, owner_id = :ownerID} @{and, owner_type = ownerType} order by name asc")
//    List<UserInfo> testIf(String ownerID, String ownerType);
//}
////
////
////    @Test
////    public void defaultMethodTest() throws Exception {
////        try (Connection con = DsUtils.mysqlConn()) {
////            MapperRegistry dalRegistry = new MapperRegistry(MappingOptions.buildNew().mapUnderscoreToCamelCase(true));
////            DalSession dalSession = new DalSession(con, dalRegistry);
////
////            dalSession.getRegistry().loadMapper(Mapper3Dal.class);
////            Mapper3Dal dalExecute = dalSession.createMapper(Mapper3Dal.class);
////
////            BoundSql boundSql = dalExecute.testBind("12345678");
////
////            assert boundSql.getSqlString().equals("SELECT * FROM user_info WHERE user_name = ?");
////            assert boundSql.getArgs()[0].equals("12345678");
////        }
////    }
////
////
////<select>
////    param1 = #{abc}
////    <!-- 参数化 -->
////    param2 = #{futures, mode = out,
////                typeHandler=net.hasor.dbvisitor.types.handler.BlobAsBytesTypeHandler,
////                javaType=net.hasor.test.dto.user_info,
////                jdbcType=INT}
////    <!-- SQL 注入 -->
////    param3 = ${orderBy}
////    <!-- 执行规则( @{<规则名>, <启用规则的条件参数>, 规则内容 ) -->
////    param4 = @{and, owner_type = :futures }
////</select>