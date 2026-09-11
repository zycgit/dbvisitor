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

### 离线验证

不使用真实凭据、不访问 OSS/CDN：

```bash
python3 -m unittest discover -s tests -v
```
