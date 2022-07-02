# Hasor

&emsp;&emsp; 以学习、总结、分享为动力，面向生产环境而设计的 Java 应用开发框架。它的核心设计目标是提供一个简单、且必要的环境给开发者。开发者可以在此基础上快速进行软件开发。

&emsp;&emsp; 区别于其它框架的是 Hasor 有着自己一套完整的体系，无论您是一般的Web项目，还是几百人团队中复杂的分布式系统。Hasor都会给予你最有力的支持。

&emsp;&emsp; 支持的功能有(J2EE、WebMVC、Restful、RPC、Simple GraphQL、IoC、Aop、Xml Reader、Event、J2EE、Form、JDBC、数据库事务)。

----------
### 特点

- “微内核+插件” 简单、小巧、功能强大、使用简单。
- COC原则的最佳实践，‘零’配置文件。
- 合理的整体架构规划，即是小框架也是大平台。
- 各部分全部独立，按需使用，绝不臃肿。
- 提供 “数据库 + 服务” 整合查询，并提供数据整合能力。
- 体积小，无依赖。

----------
### 面向人群
* 学习者、开发者

----------
### 架构
![架构](http://files.hasor.net/uploader/20170609/155318/CC2_403A_3BD5_D581.jpg "架构")

- Core 职责是提供Bean容器、Module机制。
- Data 提供了JDBC操作、事务管理、DataQL。
- RSF 是一个完备的分布式 RPC 服务框架。
- Web 是一个吸收了百家所长的 Web MVC框架。
- Land 它类似于 Zookeeper 负责提供分布式一致性的支持。
- Center 它是 RSF 的服务治理中心。

----------
### 总体功能
01. 支持IoC、Aop、模块化（Hasor-Core）
02. 支持模版化配置文件，让您程序打包之后通吃各种环境（Hasor-Core）
03. 提供JDBC操作接口，支持 Result -> Object 映射（Hasor-Data）
04. 完备的数据库事务控制能力，支持 7 种事务传播属性（Hasor-Data）
05. DataQL 服务查询引擎，语法上参考了 GraphQL（Hasor-Data）
06. 支持传统 Web MVC 开发，也支持  restful 方式（Hasor-Web）
07. 提供Form表单验证、验证支持场景化（Hasor-Web）
08. 开放的模版渲染接口，支持各种类型的模版引擎（Hasor-Web）
09. 分布式 RPC 服务，支持容灾、负载均衡、集群（Hasor-RSF）
10. 通过 Hprose 支持多协议、跨语言的 RPC 调用（Hasor-RSF）
11. 通过服务注册中心，支持分布式服务统一治理（Hasor-RSF）
12. 提供对某一个状态提供分布式下状态一致性支持（Hasor-Land）[研发]

----------
### 发展状况

&emsp;&emsp; Hasor起源于2012年。当时尚未开源，并被应用到公司各大项目中。当时基于Guice构建，并且整合了Spring JDBC、Guice等大量三方框架。

&emsp;&emsp; 2013年9月15日，第一个Hasor版本发布。Module化的插件概念被提出，同时依赖大量减少，成为一个开源的java开发框架。

&emsp;&emsp; 2015年7月3日，1.0.0版本发布。这一年Hasor明确的发展路线，确立了“小而美的核心，大而全的生态圈”目标。

&emsp;&emsp; 2016年8月18日，2.4.3版本发布，基于2.4版本Hasor开始孕育全新的子项目 RSF。当时 Hasor 的版图还没有这么大。

&emsp;&emsp; 2017年2月21日，RSF 和 Land 被并入 Hasor 体系，同时首次公开了 Hasor 大版图的想法。同年 DataQL 问世。

----------
### 介绍
##### 特色功能:
01. 支持服务热插拔：支持服务动态发布、动态卸载
02. 支持服务分组：支持服务分组、分版本
03. 支持多种方式调用：同步、异步、回调、接口代理
04. 支持多种模式调用：RPC模式调用、Message模式调用
        &emsp;&emsp;RPC     模式: 远程调用会等待并返回执行结果。适用于一般方法。遇到耗时方法会有调用超时风险
        &emsp;&emsp;Message 模式: 远程调用当作消息投递到远程机器，不会产生等待，可以看作是一个简单的 MQ。适合于繁重的耗时方法
05. 支持点对点调用。RSF的远程调用可以点对点定向调用，也可以集群大规模部署集中提供同一个服务
06. 支持虚拟机房。通过配置虚拟机房策略可以降低跨机房远程调用
07. 支持泛化调用。简单的理解，泛化调用就是不依赖二方包，通过传入方法名，方法签名和参数值，就可以调用服务
08. 支持隐式传参。可以理解隐式传参的含义为，不需要在接口上明确声明参数。在发起调用的时传递到远端
09. 内置 Telnet 控制台，可以命令行方式直接管理机器
10. 支持 offline/online 动作

##### 扩展性:
01. 支持第三方集成，可以独立使用,也可以和 Spring、Jfinal等第三方框架整合使用
02. 支持拦截器RsfFilter，开发者可以通过扩展 Filter 实现更多需求
03. 支持自定义序列化。默认使用内置 Hessian 4.0.7 序列化库
04. 支持Telnet控制台自定义指令。通过扩展控制台指令，可以发挥更大想象空间

##### 稳定性(参数可配置):
01. 最大发并发请求数配置（默认:200）
02. 最大发起请求超限制策略设置: A-等待1秒重试、B-抛异常（默认:B-抛异常）
03. Netty线程数配置（默认: 监听请求线程数: 1，IO线程数: 8）
04. 提供者调用队列容量配置（默认: 队列容量: 4096）
05. Work线程数配置（默认: 处理调用线程数: 4）
06. 请求超时设置。支持服务提供者，服务订阅者独立配置各自的超时参数（默认 6000毫秒）
07. 双向通信。RSF会合理利用Socket连接，双向通信是指当A机器发起远程调用请求之后，RSF会建立长连接
        &emsp;&emsp;-- 如果B机器有调用A机器的需求则直接使用这个连接不会重新创建新的连接，双向通信会大量降低集群间的连接数
08. 支持优雅停机。应用停机，Center会自动通知整个集群。即便所有 Center 离线，RSF也会正确处理失效地址

##### 健壮性:
01. 每小时地址本动态备份。当所有注册中心离线，即便在没有注册中心的情况下应用程序重启，也不会导致服务找不到提供者的情况
02. 当某个地址失效之后，RSF会冻结一段时间，在这段时间内不会有请求发往这个地址
03. 支持请求、响应分别使用不同序列化规则

##### 可维护性:
01. 支持QoS流量控制。流控可以精确到：接口、方法、地址
02. 支持动态路由脚本。路由可以精确到：接口、方法、参数
03. 通过路由脚本可以轻松实现接口灰度发布

##### 安全性:
01. 支持发布服务授权
02. 支持服务订阅授权
03. 支持匿名应用

----------
### 集成
01. 支持与 Spring 集成（2016-02-16）
02. 支持与 JFinal 集成（2016-11-03）
----------
### 发展状况

&emsp;&emsp; Hasor起源于2012年。当时尚未开源，并被应用到公司个大项目中。当时基于Guice构建，并且整合了Spring JDBC、Guice等大量三方框架。

&emsp;&emsp; 2013年9月15日，第一个Hasor版本发布。Module化的插件概念被提出，同时依赖大量减少，成为一个开源的java开发框架。

&emsp;&emsp; 2015年7月3日，1.0.0版本发布。这一年Hasor明确的发展路线，确立了“小而美的核心，大而全的生态圈”目标。

&emsp;&emsp; 2016年8月18日，2.4.3版本发布，基于2.4版本Hasor开始孕育全新的子项目 RSF。当时 Hasor 的版图还没有这么大。

&emsp;&emsp; 2017年2月21日，Hasor 大版图的想法首次公开亮相，RSF被并入大版图，同时 Land 项目进入开发阶段。

----------
### 最低要求
* jdk7
* servlet 2.3

----------
### 未来版本计划支持的项目（粗略计划）
* 细节优化
- 整个 static-config.xml 的配置都可以被主配置文件进行覆盖。
- RsfCenter 数据订阅，方便不同版本Center之间数据同步。为Hasor版本升级可能存在的不兼容性考虑。

----------
### 相关连接

* WebSite：[http://www.hasor.net](http://www.hasor.net)
* Docs : [http://hasor-guide.mydoc.io/](http://hasor-guide.mydoc.io/)
* Issues：[http://git.oschina.net/teams/hasor/issues](http://git.oschina.net/teams/hasor/issues)
* Team：[http://team.oschina.net/hasor](http://team.oschina.net/hasor)
* Demo工程：[http://git.oschina.net/zycgit/hasor-example](http://git.oschina.net/zycgit/hasor-example)
* QQ群：193943114
* [![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.hasor/hasor-rsf/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.hasor/hasor-rsf)
[![Build Status](https://travis-ci.org/zycgit/rsf.svg?branch=master)](https://travis-ci.org/zycgit/rsf)
[![Build Status](https://travis-ci.org/zycgit/rsf.svg?branch=dev)](https://travis-ci.org/zycgit/rsf)
* Demo工程：[http://git.oschina.net/zycgit/hasor-example](http://git.oschina.net/zycgit/hasor-example)
* 参考手册(WiKi版)：[点这里(Click Me)](SUMMARY.md)
* Docs : [http://www.hasor.net/docs/hasor/guide/](http://www.hasor.net/docs/hasor/guide/)

----------
### QA
* Q：优秀项目千千万，为何要选择重复造轮子？
* A：Hasor 的初衷是 “学习、总结、分享”，因此拿来主义并不是 Hasor 的发展策略。
* 
* Q：Hasor每个模块项目都很庞大，为什么没有分项目？
* A：项目的分分合合做过很多次。目前最优的形态就是放到一个代码库中统一代码版本管理，同时各个项目保持相互独立。
* 
* Q：Hasor用到了哪些外部依赖？
* A：slf4j、asm、JavaCC、netty4、groovy、hessian、hprose。其中：slf4j(必选)、netty4(可选)、groovy(可选)会以依赖形式存在，其它全部内置。
* 
* Q：准备造自己的小闭环么？
* A：Hasor是开放的，它的核心只有 “net.hasor.core” 一个包，共计 177 个类，约占整体代码的 10%。其它 90% 的代码都是扩展。
* 
* Q：Hasor 功能是很好，但是我想和其它框架合用可以么？
* A：可以的，目前 Hasor 已经内置了 Spring、JFinal、Nutz 三款框架的整合。您也可以自己的实际情况进行整合。启动 Hasor 只需要一行代码，相信整合不会耗费您太多精力。

### 小备注

* mvn release:prepare -P release
* ./deploy.sh -P release
* ./build.sh && docker build -t debug . && docker run debug