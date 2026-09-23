# dbVisitor 文档

在线文档：https://www.dbvisitor.net/

- 中文使用指南：`docs/guides/`
- JDBC 驱动手册：`docs/drivers/`
- 数据源特性：`docs/features/`
- 版本说明：`docs/releases/`
- 英文文档：`i18n/en/docusaurus-plugin-content-docs/current/`

## 本地预览

在此目录安装依赖并启动 Docusaurus。Node.js 版本须满足所安装 Docusaurus 的要求。

```sh
npm install
npm run start
```

构建中英文静态站点：

```sh
npm run build
```

本目录用于构建文档站，不是 dbVisitor Java 模块的构建入口。

## AI 文档索引

`npm run build` 通过 `@signalwire/docusaurus-plugin-llms-txt` 收集现有文档页面，再由 `plugins/llms.js` 整理项目介绍和目录。每种语言只生成一个入口：`build/llms.txt` 和 `build/en/llms.txt`；文档构建与本地开发只需要 Node.js 依赖环境。

索引先说明项目定位、架构、能力选择和第一次调用流程，再列出使用指南、驱动和数据源能力等文档。版本说明和博客位于 `Optional` 部分，链接直接指向相应语言的现有网页。页面的 `rel="describedby"` 指向当前语言的 `llms.txt`。

入口介绍复用中英文概览正文。`docusaurus.config.js` 中的 `overview` 指向 `docs/guides/overview.mdx`；插件读取对应语言文件中 `{/* llms:start */}` 与 `{/* llms:end */}` 之间的普通 Markdown。维护项目定位、架构与起步流程时修改这段正文即可，文档页面和 AI 入口会一起更新。

摘要中的链接使用实际发布地址，可写成相对于概览的路径；插件会移除 `.md`、`.mdx` 扩展名并补齐站点地址与语言前缀。文档版本来自 `plugins/projectVars.js`，使用前应核对项目依赖版本。

可以向编程助手提供以下指引：

> 先读取本项目的 `/llms.txt`，理解核心理念与适用场景，选择对应的使用指南。核对项目依赖版本，再参考文档中的调用方式与示例编写代码并编译验证。

索引随静态站点发布，不手工修改或提交生成文件。`npm run start` 用于开发文档页面；查看完整索引使用构建后的站点：

```bash
npm run build
npm run serve -- --host 127.0.0.1 --port 3000
```

## 博客编辑规范

新增、翻译和整理文章前，请先阅读 [AI 编辑约定](./AGENTS.md)。其中规定了顶部元数据格式、日期维护方式，以及中英文标题长度、措辞和锚点兼容规则；人工编辑与 AI 初稿使用同一标准。

元数据字段统一按以下顺序排列，可选字段不存在时省略。标题、描述使用双引号，列表使用行内格式；新文章不填写 `updated`：

```yaml
---
slug: mysql_stream_read
title: "MySQL 流式读取超大表"
description: "使用 dbVisitor 与 MySQL Connector/J 逐行读取大结果集，控制应用侧内存占用。"
authors: [ZhaoYongChun]
tags: [dbVisitor, JDBC, Streaming]
topics: [mapping]
language: zh-cn
---
```

## 博客资源

博客配图及文章附件放在 `blog/assets/`，不再放入 `static/`。可运行示例工程统一放在仓库的 `dbvisitor-example/`，文章链接到 GitHub/Gitee 源码，不再维护示例 ZIP 下载包：

```text
blog/
├── 2026-09-18-milvus-jdbc-vector-search.md
└── assets/
    └── 2026-09-18-milvus-jdbc-vector-search/  # 与文章文件名（不含扩展名）一致
        ├── jdbc-milvus-cn.svg               # 中文配图
        └── jdbc-milvus.svg                  # 英文配图
```

每篇文章的配图和附件集中在自己的资源目录内，中英文版本共用该目录；不再按版本号或资源类型混放。没有资源的文章无需创建空目录。中文文章使用相对路径引用；构建时由 Docusaurus 打包资源并生成访问地址：

```markdown
![示意图](./assets/2026-09-18-milvus-jdbc-vector-search/jdbc-milvus-cn.svg)
示例工程（[GitHub](https://github.com/zycgit/dbvisitor/tree/main/dbvisitor-example/blog-680) / [Gitee](https://gitee.com/zycgit/dbvisitor/tree/main/dbvisitor-example/blog-680)）
```

英文文章仍保存在 `i18n/en/docusaurus-plugin-content-blog/`，通过 `../../../blog/assets/` 引用同一份资源。不要为下载链接加 `pathname://`，否则会跳过文件打包。JSX 图片使用 `src={require('@site/blog/assets/2026-01-06-new-generation-dbvisitor/double.png').default}`。

`assets` 已排除在博客文章扫描之外，其中的 Markdown 附件不会出现在文章列表或订阅中。6.8.0 系列示例维护在 `../dbvisitor-example/blog-680/`；修改源码后无需重新打包 ZIP。作者头像仍由 `authors.yml` 管理；站点共用资源继续放在 `static/`。发布文章前，需先将对应示例提交并推送到链接所指的源码分支。

## 博客专栏

### 发布时间与更新时间

发布时间统一取文件名的 `YYYY-MM-DD` 前缀，front matter 不再填写顶层 `date`。不要为了更新文章而修改文件名日期。新文章在首发前的编写、校对和反复打磨不设置 `updated`，即使跨天也只保留发布时间。仅在文章正式发布后发生实质性修订时，在元数据最后填写单行更新时间：

```yaml
updated: 2026-09-17
```

文章标题区在发布与更新日期不同时分别显示“发布”和“更新”；同一天只显示发布时间，按页面使用的 UTC 日历日期判断，不比较时分秒。月份、日期均补足两位；归档和专栏仍按原始发布时间排序。仅校验通过、排版或重新构建不自动生成更新时间。中英文内容同步修订时，分别设置对应文章的 `updated`。

在 `blog/topics.yml` 定义专栏，顺序即页面展示顺序：

```yaml
vectors:
  label: 向量数据库实战
  description: 从向量读写开始，逐步加入业务检索与数据导入。
```

在文章 front matter 中声明归属，使用配置中的专栏 ID：

```yaml
authors: [ZhaoYongChun]
tags: [Milvus, JDBC]
topics: [vectors]
```

一篇文章可以属于多个专栏，例如 `topics: [vectors, apis]`。省略 `topics` 或写成 `topics: []` 时，不进入任何专栏，但仍出现在“全部”和“最新”中。专栏与标签互不影响。

专栏内按发布日期倒序排列，首页卡片最多展示 6 篇，详情页展示全部；没有文章的专栏不显示。专栏 ID 拼错会在构建时指出具体文章。

英文名称和简介放在 `i18n/en/docusaurus-plugin-content-blog/topics.yml`，使用相同 ID；缺少某个专栏的翻译时回退到中文定义。英文文章也在自己的 front matter 中声明 `topics`。新增文章只需标记归属，不用再维护集中式文章清单。

## 博客作者链接

作者信息在 `blog/authors.yml` 配置，英文对应 `i18n/en/docusaurus-plugin-content-blog/authors.yml`。文章详情页的头像和姓名共用链接：配置 `page: true` 时进入作者文章页，否则使用作者 `url`，最后回退到 `email`。作者资料中的 `socials` 可配置社交网站入口。

```yaml
ZhaoYongChun:
  name: ZhaoYongChun
  url: https://gitee.com/zycgit
```

若不使用作者文章页、希望点击后写邮件，可将 `page` 设为 `false` 并把 `url` 改为 `mailto:zyc@hasor.net`，或不配置 `url`、只配置 `email: zyc@hasor.net`。这会打开读者的邮件客户端，不会自动发送邮件。

## 文档版本变量

`plugins/projectVars.js` 统一维护版本：`docsVersion` 用于使用指南及通用依赖示例，`jdbcDriverVersion` 用于四个非关系型 JDBC 驱动的依赖示例和 `alone` 下载链接，`developmentVersion` 用于开发版本入口，`lastReleaseVer`、`lastReleaseTime` 用于最新发布信息。首页也从此文件读取版本。

Markdown/MDX 正文、行内代码、代码块和链接可使用 `@project.docsVersion@`、`@project.jdbcDriverVersion@`、`@project.lastReleaseVer@`、`@project.lastReleaseTime@`。构建后显示和复制的内容均为实际值；变量名拼错会使构建失败。

```xml
<version>@project.docsVersion@</version>
```

修改后重新构建，本地预览需重启。历史版本、兼容性最低版本及第三方依赖版本保持固定，不随当前版本变化。此配置不修改 Java 工程或文档站 package.json 的版本。

## 部署到 OSS 并刷新 CDN

两站点使用同一配置结构和发布流程，但各自的 `deploy_site.py` 固定绑定 `dbvisitor` 或 `hasor`，不会按工作目录猜测目标，也不会选择配置中的第一个站点。

### 配置

将任一工程的 `oss-config.sample.json` 复制到 `~/.hasor-docs-deploy.json`，填写真实配置。两个样例内容相同，都包含两个站点；Bucket 名称只是占位示例。

- `credentials`：按名称管理 AccessKey。站点通过 `credential` 引用，可共享凭据，也可使用不同凭据。
- `sites.<站点>.endpoint`、`bucketName`：OSS 上传目标。
- `prefix`：Bucket 内的目录，空字符串表示根目录；例如 `docs/hasor/`。上传保留该前缀下的中英文相对路径。
- `cdnUrl`：该站点要刷新的完整目录地址；可选 `cdnRegionId` 默认 `cn-hangzhou`。

需要显式指定其他配置文件时，设置环境变量，不存在或无效时直接报错，不回退：

```bash
HASOR_DOCS_DEPLOY_CONFIG=/absolute/path/docs-deploy.json ./deploy.sh
```

旧的 `~/.dbvisitor-oss-config.json`、`~/.hasor-oss-config.json` 和工程内 `oss-config.json` 不再自动读取。请把旧配置中的凭据迁入 `credentials`，上传目标及 CDN 地址迁入对应 `sites` 节点。脚本不会自动迁移或删除旧文件。配置含密钥，不要提交到 Git；建议设置 `chmod 600 ~/.hasor-docs-deploy.json`。

### 目标隔离

优先使用不同 Bucket。共享 Bucket 时，两站点必须配置互不重叠的前缀；根目录与任意子目录、同一目录及父子目录组合都会被拒绝。同域名的 CDN 刷新目录也不能重叠，避免刷新另一个站点。校验覆盖配置中的所有站点。

请确认 CDN 回源规则指向对应 Bucket/前缀，且 Docusaurus 的 `url`、`baseUrl` 与实际访问地址匹配；脚本不能验证云端回源配置。没有回源路径改写时，OSS 前缀与 CDN URL 路径应对应；部署到子路径还需同步调整 Docusaurus 的 `baseUrl`。

### 执行发布

需要 Node.js 20+、npm、Python 3（含 venv 和 pip），支持 Linux/macOS。首次使用或前端依赖更新后，在本目录执行 `npm ci`，随后执行：

```bash
./deploy.sh
```

脚本通过 `.deploy-venv` 安装 Python 依赖，再依次构建中英文站点、上传 `build/`、提交 CDN 目录刷新。任一步失败即返回非零状态；构建或上传失败不会继续刷新 CDN。一次完整发布只读取一次配置，上传和刷新使用同一份目标信息。

发布锁覆盖构建、上传和刷新，锁文件在 `~/.cache/hasor-docs-deploy/`。同一台机器、同一用户下，相同站点跨工作副本也不能同时发布；不同站点可以独立发布。锁在退出时释放，不要删除使用中的锁文件。多机器或不同系统用户的并发发布需要在 CI 中另行串行化。

上传覆盖目标前缀下的同名文件，不删除远端旧文件，不是原子发布。部分上传成功后失败，需修复原因并重新执行；CDN 刷新请求被接受也不代表所有节点已完成刷新。凭据需具有目标上传及 CDN 刷新权限。

脚本可从任意工作目录调用。虚拟环境准备好后，也可以在本目录单独执行：

```bash
# 只构建和上传
.deploy-venv/bin/python deploy_to_oss.py

# 只刷新当前站点 CDN
.deploy-venv/bin/python refresh_website_cdn.py
```

两个独立入口同样使用配置校验和站点锁。`npm run deploy` 是 Docusaurus 自带的部署命令，不等同于 OSS/CDN 的 `./deploy.sh`。
