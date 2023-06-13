---
id: with-spring
sidebar_position: 1
title: Spring 整合
description: dbVisitor ORM 工具和 Spring 整合使用。
---
# 与 Spring 集成

经典的 Spring 工程需要引入下面这个包：

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>dbvisitor-spring</artifactId>
    <version>5.3.0</version>
</dependency>
```

这里提供一个地址可以获取 Demo 工程 [Spring Demos工程](https://gitee.com/zycgit/dbvisitor/tree/main/dbvisitor-example/spring/)

## 基于 Spring 注解化

Spring 3.0 之后引入了 @Configuration、@Bean 等注解正式开启了注解化时代。dbVisitor 支持 Spring 3.0 的注解化，具体步骤如下：

- 首先在 Spring 的 XML 配置文件的 `context:component-scan` 的扫描路径里面增加一个配置类。
- 然后配置类上增加 @MapperScan 注解并且确定 Mapper 接口的包路径即可。

```xml
@Configuration
@MapperScan("com.example.demo.dao")
public class AppConfig {
    ...
}
```

:::tip
默认情况下 dbVisitor 在创建 Mapper 接口对象时如果没有指定 `DalSession` 将会自动创建它，并同时使用 默认 DataSource 作为它的数据源。
也可以在 XML 文件中事先声明。
:::

## 基于 Spring XML 配置

Spring 2.x 时代需要通过引入三个 Spring Bean 配置，其中第三个 Bean 则是具体的 Mapper 接口。

```xml
<!-- DalRegistry -->
<bean id="dalRegistry" class="net.hasor.dbvisitor.spring.support.DalRegistryBean">
    <!-- 必选-->
    <property name="mapperResources" value="classpath*:dbvisitor/mapper/*Mapper.xml"/>
    <!-- 可选-->
    <!--    <property name="mapperInterfaces" value=""/>-->
    <!--    <property name="autoMapping" value="true | false"/>-->
    <!--    <property name="camelCase" value="true | false"/>-->
    <!--    <property name="caseInsensitive" value="true | false"/>-->
    <!--    <property name="useDelimited" value="true | false"/>-->
    <!--    <property name="dialectName" value="mysql"/>-->
</bean>

<!-- DalSession -->
<bean id="dalSession" class="net.hasor.dbvisitor.spring.support.DalSessionBean">
    <!-- 必选-->
    <property name="dalRegistry" ref="dalRegistry"/>
    <property name="dataSource" ref="dataSource"/>
    <!-- 可选-->
    <!--    <property name="dsAdapterClass" value="xxxx"/>-->
</bean>
<!-- userMapper -->
<bean id="userMapper" class="net.hasor.dbvisitor.spring.support.DalMapperBean">
    <property name="dalSession" ref="dalSession"/>
    <property name="mapperInterface" value="com.example.demo.dao.UserMapper"/>
</bean>
```

如果为每个 Mapper 单独添加 XML 配置较为繁琐，那么可以通过 `MapperScannerConfigurer` 来扫描注册。

```xml
<!-- Mapper Scanner -->
<bean class="net.hasor.dbvisitor.spring.mapper.MapperScannerConfigurer">
    <property name="basePackage" value="com.example.demo.dao"/>
    <property name="dalSession" ref="dalSession"/>
</bean>
```

:::tip
由于 dbVisitor 最低依赖 JDK8，早期项目需要检查自身和环境是否满足这个要素，否则会出现不兼容情况。
:::

## Mapper 的映射方式

Mapper 接口和映射文件关联方式有两种，满足其中一种即可。

- 通过 Mapper 文件的 namespace 属性来关联
- 通过 Mapper 接口上标记 `@RefMapper("...")` 注解来关联

在 [Spring Demos工程](https://gitee.com/zycgit/dbvisitor/tree/main/dbvisitor-example/spring/) 使用的是第一种方式。
