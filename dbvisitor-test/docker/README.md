# dbVisitor 测试环境

本目录提供 x86 和 ARM64 两套 Docker Compose 配置，功能与端口完全一致，仅镜像因平台而异。

## 启动

```bash
# 根据平台选择对应目录
cd x86       # Intel / AMD
cd arm64     # Apple Silicon / ARM 服务器

docker compose up -d
```

- 宿主机直连时使用 `127.0.0.1` 和对外端口。
- 代理或 SSH 通道，可以使用容器内服务名 + 对内端口。

| 服务 | 容器内服务名 | Database/Service | 常规连接 | SSL（信任） | SSL（CA证书/单向） | SSL（双向） | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| MySQL | `mysql` | `devtester` | Port: `3306 (内)/2330 (外)`<br>用户: `root` / `123456` | Port: `3306 (内)/2330 (外)`<br>用户: `root` / `123456`<br>证书: 不需要 | Port: `3306 (内)/2330 (外)`<br>用户: `root` / `123456`<br>CA 证书: `certs/ca.p12`<br>CA 证书密码: 留空 | Port: `3306 (内)/2330 (外)`<br>用户: `sslclient` / `123456`<br>CA 证书: `certs/ca.p12`<br>CA 证书密码: 留空<br>客户端 KeyStore: `certs/client.p12`<br>KeyStore 密码: `123456` | 兼容文件: `ca-123456.p12`、`ca.jks`、`client.jks` |
| Oracle | `oracle` | SID: x86 `XE` / arm64 `FREE`<br>Service Name: `DEVTESTDB`<br>PDB: `DEVTESTDB` | Port: `1521 (内)/2521 (外)`<br>SID: x86 `XE` / arm64 `FREE`<br>SID 用户: `SYSTEM` / `123456`<br>Service Name/PDB: `DEVTESTDB`<br>Service Name/PDB 用户: `devtester` / `123456` | - | Port: `2484 (内)/2484 (外)`<br>SID: x86 `XE` / arm64 `FREE`<br>SID 用户: `SYSTEM` / `123456`<br>Service Name/PDB: `DEVTESTDB`<br>Service Name/PDB 用户: `devtester` / `123456`<br>CA KeyStore: `certs/ca.p12`<br>密码: 留空 | Port: `2485 (内)/2485 (外)`<br>SID: x86 `XE` / arm64 `FREE`<br>SID 用户: `SYSTEM` / `123456`<br>Service Name/PDB: `DEVTESTDB`<br>Service Name/PDB 用户: `devtester` / `123456`<br>CA KeyStore: `certs/ca.p12`<br>密码: 留空<br>客户端 KeyStore: `certs/client.p12`<br>密码: `123456` | `oracle/wallet` |
| PostgreSQL | `postgres` | `postgres` | Port: `5432 (内)/2543 (外)`<br>用户: `postgres` / `123456` | Port: `5432 (内)/2543 (外)`<br>用户: `postgres` / `123456`<br>证书: 不需要 | Port: `5432 (内)/2543 (外)`<br>用户: `postgres` / `123456`<br>CA 证书: `ca.crt` | Port: `5432 (内)/2543 (外)`<br>用户: `sslclient` / 留空<br>CA 证书: `ca.crt`<br>客户端证书: `client.crt`<br>客户端私钥: `client.pk8`<br>私钥短语: 留空 | x86 和 arm64 compose 共享同一套测试证书；启用 `wal_level=logical`，WAL 保留按测试用途限制为较小规模 |
| Redis | `redis` | - | Port: `6379 (内)/2637 (外)`<br>密码: `123456` | - | - | - | `requirepass` |
| MongoDB | `mongo` | `admin` | Port: `27017 (内)/2701 (外)`<br>用户: `root` / `123456` | - | - | - | admin 用户 |
| SQL Server | `mssql` | `devtester` | Port: `1433 (内)/2143 (外)`<br>用户: `sa` / `Share123456!` | - | - | - | `mssql/entrypoint.sh` 启动后创建 `devtester`；arm64 compose 使用 `linux/amd64` 镜像 |
| DB2 | `db2` | `DEVTEST` | Port: `50000 (内)/2500 (外)`<br>用户: `db2inst1` / `123456` | - | - | - | arm64 compose 使用 `linux/amd64` 镜像 |
| ClickHouse HTTP | `clickhouse` | `default` | Port: `8123 (内)/2812 (外)`<br>用户: `root` / `password123` | - | - | - | HTTP 端口 |
| ClickHouse Native | `clickhouse` | `default` | Port: `9000 (内)/2900 (外)`<br>用户: `root` / `password123` | - | - | - | Native 端口 |
| Elasticsearch 6 | `elasticsearch6` | - | HTTP Port: `9200 (内)/2920 (外)`<br>Transport Port: `9300 (内)/2930 (外)`<br>认证: 无 | - | - | - | `xpack.security.enabled=false`；测试 JDBC URL 使用 `127.0.0.1:2920` |
| Elasticsearch 7 | `elasticsearch7` | - | HTTP Port: `9200 (内)/2921 (外)`<br>Transport Port: `9300 (内)/2931 (外)`<br>认证: 无 | - | - | - | `xpack.security.enabled=false`；测试 JDBC URL 使用 `127.0.0.1:2921` |
| Milvus | `milvus` / `milvus_tls_gateway` | `default` | gRPC/REST: `19530 (内)/2953 (外)`<br>管理端口: `9091 (内)/2909 (外)`<br>认证: 无 | 不提供跳过校验模式 | 统一入口：`19530 (内)/2954 (外)`，SDK/REST 共用<br>CA: `certs/ca.crt`<br>JDBC: `secure=true` | 统一入口：`19531 (内)/2955 (外)`，SDK/REST 共用<br>CA: `certs/ca.crt`<br>客户端: `certs/client.crt` + `certs/client.key`<br>JDBC: `secure=true` | TLS 服务属于可选 `milvus-tls` profile；standalone 2.6.2，arm64 使用 `linux/amd64` Milvus 镜像 |

通过 SSH 通道访问这些数据源时，数据源 Host 使用 compose 服务名，例如 `mysql`、`postgres`、`oracle`，端口使用容器内端口。不要把数据源 Host 写成 `127.0.0.1`，因为在 SSH 转发场景下它表示 SSH Server 容器自身。

DB2 的数据库名不能超过 8 bytes，测试库使用 `DEVTEST`。如果已经用旧配置启动过 DB2，需要重建 `db2` 容器后新数据库名才会生效。

## 运行测试

与 Redis 相同，`jdbc-elastic`、`jdbc-mongo`、`jdbc-milvus` 的适配器模块仅保留离线测试；需要真实服务的命令测试和合同测试统一放在 `dbvisitor-test/src/test/java/net/hasor/dbvisitor/test/realdb` 下。适配器不配置 `realdb` 排除规则，也不注册独立的 TLS 测试任务。

在仓库根目录执行离线测试，无需启动 Docker：

```bash
./gradlew :jdbc-elastic:test :jdbc-mongo:test :jdbc-milvus:test --rerun-tasks --no-daemon
```

启动对应测试服务后，沿用统一的真库测试入口：

```bash
./runnxn.sh es6
./runnxn.sh es7
./runnxn.sh mongo
./runnxn.sh milvus
```

这些测试会创建、修改或删除测试数据，只能连接专用测试环境。`dbvisitor-test:test` 未指定 `nxn.env` 时不执行真库测试；脚本负责选择对应测试包并强制重新执行。`milvus` 包含明文/TLS/mTLS 连接测试，执行 `milvus` 或 `all` 前还需按下文启动 `milvus-tls` profile 的测试服务。

## SSL 证书说明

MySQL、Oracle、PostgreSQL 和 Milvus 共用 `docker/certs` 下的测试 CA、服务端证书和客户端证书。仅用于本地测试，禁止用于生产。数据源的连接地址、账号、SSL 模式及各模式应填写的文件只在顶部数据源表中说明；本节仅解释证书文件的内容、格式和彼此关系。

| 文件 | 内容与格式 | 密码 | 含义及等效关系 |
| --- | --- | --- | --- |
| `certs/ca.crt` | CA 证书，PEM | - | 自签名根证书 `CloudDM Test CA`；是本套测试证书链的信任锚 |
| `certs/ca.key` | CA 私钥，PEM | - | 用于签发测试用服务端、客户端证书；仅供生成测试证书，不应配置到数据源客户端 |
| `certs/ca.p12` | CA 证书，PKCS#12 TrustStore | 空密码 | 与 `ca.crt` 包含同一张 CA 证书，仅封装格式不同 |
| `certs/ca-123456.p12` | CA 证书，PKCS#12 TrustStore | `123456` | 与 `ca.crt`、`ca.p12` 包含同一张 CA 证书，仅密码和封装形式不同 |
| `certs/ca.jks` | CA 证书，JKS TrustStore | `123456` | 与 `ca.crt`、`ca.p12` 包含同一张 CA 证书，仅 Java KeyStore 格式不同 |
| `certs/server.crt` | 服务端证书，PEM | - | 身份为 `localhost`，由 `CloudDM Test CA` 签发，供 MySQL、PostgreSQL 以及 Oracle listener 向客户端证明服务端身份 |
| `certs/server.key` | 服务端私钥，PEM | - | 与 `server.crt` 配对；仅供服务端使用，不应上传为 CA 或客户端私钥 |
| `certs/client.crt` | 客户端证书，PEM | - | 身份为 `sslclient`，由 `CloudDM Test CA` 签发，供双向 SSL 中客户端向服务端证明身份 |
| `certs/client.key` | 客户端私钥，PEM | - | 与 `client.crt` 配对，适合接受 PEM 私钥的客户端 |
| `certs/client.pk8` | 客户端私钥，PKCS#8 DER | - | 与 `client.key` 是同一把客户端私钥，仅编码和格式不同，适合 PostgreSQL JDBC/CloudDM |
| `certs/client.p12` | 客户端证书及私钥，PKCS#12 KeyStore | `123456` | 将 `client.crt`、对应私钥及证书链封装为一个文件，适合 MySQL、Oracle JDBC |
| `certs/client.jks` | 客户端证书及私钥，JKS KeyStore | `123456` | 与 `client.p12` 表示同一个客户端身份，仅 KeyStore 格式不同 |
| `oracle/wallet/ewallet.p12` | Oracle listener wallet，PKCS#12 | wallet 内部管理 | 使用 `server.crt`、`server.key` 和 CA 证书生成的 listener wallet，容器启动时挂载；不是数据源客户端 KeyStore |
| `oracle/wallet/cwallet.sso` | Oracle auto-login wallet | 无需交互输入 | `ewallet.p12` 对应的自动登录文件，供 Oracle listener 启动时直接读取；不是独立的一套证书 |

本测试环境没有中间 CA，因此证书链只有两层：

```text
服务端：server.crt (CN=localhost)  -> ca.crt (CN=CloudDM Test CA，自签名根 CA)
客户端：client.crt (CN=sslclient) -> ca.crt (CN=CloudDM Test CA，自签名根 CA)
```

`ca.crt`、`ca.p12`、`ca-123456.p12`、`ca.jks` 表示同一个信任锚；`client.crt + client.key`、`client.crt + client.pk8`、`client.p12`、`client.jks` 表示同一个客户端身份。它们可以按数据源或驱动支持的格式择一使用，不需要同时配置。

## Milvus TLS 测试

默认 `docker compose up -d` 不启动 TLS 服务；原明文 Milvus 的配置和端口保持不变。在 `x86` 或 `arm64` 目录中按需启动：

```bash
docker compose --profile milvus-tls up -d milvus_tls_gateway milvus_plain
docker compose --profile milvus-tls ps milvus_tls_gateway milvus_tls milvus_mtls milvus_plain
```

- 网关自动启动并等待两个 TLS 后端健康。三个测试实例各自拥有独立数据，不复用默认 `milvus` 服务的数据目录；`milvus_plain` 提供独立的明文对照入口 `127.0.0.1:2956`，SDK/REST 原生共用，不经过网关。
- TLS 后端配置来自 `milvus/tls.yaml`、`milvus/mtls.yaml`，只读挂载为 `user.yaml`；`tlsMode=1` 为单向，`tlsMode=2` 为双向。`DEPLOY_MODE=STANDALONE` 显式指定内嵌 etcd 的部署模式。
- Milvus 2.6.2 原生 TLS 要求 gRPC 与 REST 在容器内分别监听 19530 和 8080。`milvus_tls_gateway` 使用 `milvus/tls-ingress.yaml`，按 TLS ClientHello 的 ALPN 将 HTTP/2 转发到 gRPC，HTTP/1.1 转发到 REST；驱动的 REST 请求固定使用 HTTP/1.1。该入口不适用于需要 HTTP/2 REST 的其他客户端。
- 透传网关不解密流量、不挂载证书或私钥；服务端证书与客户端身份仍由客户端和 Milvus 端到端校验。对外每个实例仅一个端口：TLS `2954`，mTLS `2955`，均只绑定 `127.0.0.1`。JDBC URL 填该端口即可，无额外 REST 参数。两个 TLS 后端的 8080、19530 和管理端口 9091 均不映射到宿主机；Compose 健康检查在容器内执行。
- 服务端仅挂载 CA 证书、服务端证书和私钥，不挂载 CA 私钥或客户端身份。PEM 客户端私钥使用 `client.key`，不是 DER 格式的 `client.pk8`。
- 证书包含 localhost 和 127.0.0.1。经容器服务名或 SSH 转发连接时配置 `serverName=localhost`，保持服务端身份校验。
- Cloud 无需增加本地模拟容器；使用真实集群端点、token 和网络权限验证。JDBC 参数及 Cloud 示例见 [dbvisitor-doc 连接参数与 TLS](../../dbvisitor-doc/docs/drivers/milvus/params.md#tls)。

等待测试服务健康、网关启动后，在 dbvisitor 根目录执行：

```bash
./runnxn.sh milvus --no-daemon --offline
```

该命令使用 `dbvisitor-test` 的普通 `test` 任务，执行 `realdb/milvus` 包中的全部测试，包含 `MilvusTlsConnectionTest`；等价于 `./gradlew :dbvisitor-test:test -Pnxn.env=milvus --rerun-tasks --no-daemon --offline`。其中连接测试使用上述固定本机端口，验证明文/TLS/mTLS 下同端口的 JDBC 建表、写入和 Import 任务列表，以及错误名称、缺少客户端证书、不可信证书和对 TLS 入口发送明文被拒绝。连接测试只创建随机命名的测试集合，并在测试结束时删除；服务缺失直接失败，不跳过。`./runnxn.sh all` 同样包含这些测试。普通 `:jdbc-milvus:test` 另含不依赖 Docker 的本地 TLS 协议测试；仅修改共用证书时需加 `--rerun-tasks` 重新检查。

在对应平台目录中停止可选服务：

```bash
docker compose --profile milvus-tls stop milvus_tls_gateway milvus_tls milvus_mtls milvus_plain
```

配置依据：[Milvus TLS 部署说明](https://milvus.io/docs/tls.md)、[Milvus 2.6.2 端口监听实现](https://github.com/milvus-io/milvus/blob/v2.6.2/internal/distributed/proxy/listener_manager.go)、[Envoy TLS Inspector](https://www.envoyproxy.io/docs/envoy/latest/configuration/listeners/listener_filters/tls_inspector)。

## SSH Server

SSH Server 用于验证密码、私钥、私钥加密码短语，以及 SSH 端口转发。

| 配置项 | 值 |
| --- | --- |
| 宿主机 Host | `127.0.0.1` |
| 宿主机 Port | `2022` |
| 容器服务名 | `ssh_server` |
| 容器内 Port | `22` |
| 用户名 | `sshuser` |
| 密码 | `123456` |
| 私钥 | `ssh/id_rsa` |
| 带密码短语私钥 | `ssh/id_rsa_passphrase` |
| 密码短语 | `passphrase123` |

本地命令验证：

```bash
ssh -p 2022 sshuser@127.0.0.1
ssh -i docker/ssh/id_rsa -p 2022 sshuser@127.0.0.1
ssh -i docker/ssh/id_rsa_passphrase -p 2022 sshuser@127.0.0.1
```

SSH Server 已通过 `docker/ssh/10-enable-tcp-forwarding.sh` 设置 `Port 22` 和 `AllowTcpForwarding yes`，可以验证端口转发：

```bash
ssh -N -L 13306:mysql:3306 -p 2022 sshuser@127.0.0.1
mysql -h 127.0.0.1 -P 13306 -uroot -p123456
```

这里 `mysql:3306` 是 SSH Server 容器所在 compose 网络内的目标地址。

## 代理服务

代理服务使用 `3proxy/3proxy:latest` 镜像，配置文件挂载到 `/etc/3proxy/3proxy.cfg`。当前提供两组代理服务：`proxy` 无认证，`proxy_auth` 使用账号密码认证。

| 服务 | 认证 | 代理类型 | 宿主机地址 | 容器网络地址 | 用户名 | 密码 |
| --- | --- | --- | --- | --- | --- | --- |
| `proxy` | 无 | HTTP | `127.0.0.1:2312` | `proxy:3128` | - | - |
| `proxy` | 无 | SOCKS4/SOCKS5 | `127.0.0.1:2108` | `proxy:1080` | - | - |
| `proxy_auth` | 账号密码 | HTTP | `127.0.0.1:2313` | `proxy_auth:3128` | proxyuser | 123456 |
| `proxy_auth` | 账号密码 | SOCKS5 | `127.0.0.1:2109` | `proxy_auth:1080` | proxyuser | 123456 |

从宿主机上的 CloudDM/Sidecar 通过代理访问 SSH Server 时，CloudDM/Sidecar 先连接宿主机暴露的代理端口，再由代理容器连接 SSH Server。链路是 `127.0.0.1:2312/2108 -> ssh_server:22 -> 数据源服务名:容器内端口`。为了明确验证是否经过代理，SSH Host 使用容器网络内的 SSH 服务地址：

| 配置项 | 值 |
| --- | --- |
| SSH Host | `ssh_server` |
| SSH Port | `22` |
| 代理类型 | HTTP、SOCKS4 或 SOCKS5 |
| 代理主机 | `127.0.0.1` |
| 代理端口 | 无认证：HTTP 使用 `2312`，SOCKS4/SOCKS5 使用 `2108`；账号密码认证：HTTP 使用 `2313`，SOCKS5 使用 `2109` |
| 代理认证 | 按上表选择无认证或账号密码 |

### 通过代理和 SSH 访问 MySQL

该例子用于验证完整链路：`CloudDM/Sidecar -> 127.0.0.1:2108 -> proxy:1080 -> ssh_server:22 -> mysql:3306`。

SSH 通道配置：

| 配置项 | 值 |
| --- | --- |
| SSH Host | `ssh_server` |
| SSH Port | `22` |
| 用户名 | `sshuser` |
| 认证方式 | 密码 |
| 密码 | `123456` |
| 代理类型 | SOCKS5 |
| 代理主机 | `127.0.0.1` |
| 代理端口 | `2108` |
| 代理认证 | 无 |

数据源配置：

| 配置项 | 值 |
| --- | --- |
| 数据源 Host | `mysql` |
| 数据源 Port | `3306` |
| 用户名 | `root` |
| 密码 | `123456` |
| Database | `devtester` |

如果要验证带账号密码的代理，只需要把代理配置改为：

| 配置项 | 值 |
| --- | --- |
| 代理类型 | SOCKS5 |
| 代理主机 | `127.0.0.1` |
| 代理端口 | `2109` |
| 代理认证 | 账号密码 |
| 代理用户名 | `proxyuser` |
| 代理密码 | `123456` |
