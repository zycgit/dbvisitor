///*
// * Copyright 2008-2009 the original author or authors.
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
//package net.example.db.config;
//import net.hasor.cobble.loader.ResourceLoader.MatchType;
//import net.hasor.cobble.loader.ResourceLoader.ScanEvent;
//import net.hasor.cobble.loader.providers.ClassPathResourceLoader;
//import net.hasor.db.dal.repository.DalRegistry;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.Configuration;
//
//import javax.annotation.Resource;
//import javax.sql.DataSource;
//import java.io.IOException;
//import java.net.URL;
//import java.util.List;
//
///**
// * @author 赵永春 (zyc@hasor.net)
// * @version : 2021-01-02
// */
//@Configuration(proxyBeanMethods = false)
//public class MapperConfig {
//    @Resource(name = "metadataDs")
//    private DataSource         metadataDs = null;
//    @Resource(name = "dataDs1")
//    private DataSource         dataDs1    = null;
//    @Resource(name = "dataDs2")
//    private DataSource         dataDs2    = null;
//    @Resource(name = "dataDs3")
//    private DataSource         dataDs3    = null;
//    @Resource
//    private ApplicationContext applicationContext;
//
//    public void data1Store() throws IOException {
//
//        ClassPathResourceLoader resourceLoader = new ClassPathResourceLoader();
//        List<URL> objectList = resourceLoader.scanResources(MatchType.Prefix, ScanEvent::getResource, new String[] { "/mapper/" });
//        DalRegistry dalRegistry = new DalRegistry();
//        dalRegistry.loadMapper(objectList.get(0));
//    }
//}