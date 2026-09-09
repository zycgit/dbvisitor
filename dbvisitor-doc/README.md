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
