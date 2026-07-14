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
| Milvus | `milvus` | `default` | gRPC Port: `19530 (内)/2953 (外)`<br>HTTP/metrics Port: `9091 (内)/2909 (外)`<br>认证: 无 | - | - | - | standalone 模式；arm64 compose 使用 `linux/amd64` 镜像 |

通过 SSH 通道访问这些数据源时，数据源 Host 使用 compose 服务名，例如 `mysql`、`postgres`、`oracle`，端口使用容器内端口。不要把数据源 Host 写成 `127.0.0.1`，因为在 SSH 转发场景下它表示 SSH Server 容器自身。

DB2 的数据库名不能超过 8 bytes，测试库使用 `DEVTEST`。如果已经用旧配置启动过 DB2，需要重建 `db2` 容器后新数据库名才会生效。

## SSL 证书说明

MySQL、Oracle 和 PostgreSQL 共用 `docker/certs` 下的测试 CA、服务端证书和客户端证书。数据源的连接地址、账号、SSL 模式及各模式应填写的文件只在顶部数据源表中说明；本节仅解释证书文件的内容、格式和彼此关系。

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
