# dbVisitor 测试环境

本目录提供 x86 和 ARM64 两套 Docker Compose 配置，功能与端口完全一致，仅镜像因平台而异。

## 启动

```bash
# 根据平台选择对应目录
cd x86       # Intel / AMD
cd arm64     # Apple Silicon / ARM 服务器

docker compose up -d
```

所有服务（含 PostgreSQL pgvector 扩展初始化、Milvus 向量数据库）一次性拉起，无需额外操作。

## 服务列表

| 服务 | 服务名 | 端口 | 凭证 |
|------|--------|------|------|
| PostgreSQL 16 + pgvector | postgres | 15432 | postgres / 123456 |
| MySQL | mysql | 13306 | root / 123456 |
| Redis 7.2 | redis_7_2_3 | 16379 | 密码 123456 |
| MongoDB 6.0 | mongo_6_0 | 17017 | root / 123456 |
| Elasticsearch 6.8 | es_6_8_3 | 19200 / 19300 | - |
| Kibana 6.8 | kibana_6_8_3 | 15601 | - |
| Elasticsearch 7.17 | es_7_17_27 | 19201 / 19301 | - |
| Kibana 7.17 | kibana_7_17_27 | 15602 | - |
| OpenSearch 2.11 | opensearch_2_11_1 | 19202 / 19302 | - |
| OpenSearch Dashboards | opensearch_dashboards_2_11_1 | 15603 | - |
| Milvus 2.5 | milvus | 19530 (gRPC) / 19091 (HTTP) | - |
| MinIO (Milvus 存储) | milvus-minio | 19001 / 19002 | minioadmin / minioadmin |

## 平台差异

| 服务 | x86 镜像 | ARM64 镜像 | 说明 |
|------|----------|-----------|------|
| MySQL | mysql:8.0.22 | mysql:8.4 | 8.0.22 无 ARM64 镜像 |
| Elasticsearch 6.8 / Kibana 6.8 | 正常可用 | **已注释** | 无官方 ARM64 镜像，如需 ES 6.x 请使用 x86 环境 |

其余服务镜像在两个平台上完全相同。

## PostgreSQL 向量扩展

容器首次启动时通过 `postgres-init/01-init-vector.sql` 自动执行 `CREATE EXTENSION IF NOT EXISTS vector`。

```sql
-- 验证
SELECT extname, extversion FROM pg_extension WHERE extname = 'vector';
```

## 常用命令

```bash
# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f postgres

# 停止所有
docker compose down

# 重建（清除数据）
docker compose down -v && docker compose up -d
```
