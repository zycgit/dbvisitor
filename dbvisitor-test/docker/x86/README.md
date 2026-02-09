# dbVisitor 测试环境 (x86)

## 启动

```bash
docker compose up -d
```

所有服务（含 PostgreSQL pgvector 扩展初始化、Milvus 向量数据库）一次性拉起，无需额外操作。

## 服务列表

| 服务 | 镜像 | 端口 | 凭证 |
|------|------|------|------|
| PostgreSQL 16 + pgvector | pgvector/pgvector:pg16 | 15432 | postgres / 123456 |
| MySQL 8.0 | mysql:8.0.22 | 13306 | root / 123456 |
| Redis 7.2 | redis:7.2.3 | 16379 | 密码 123456 |
| MongoDB 6.0 | mongo:6.0 | 17017 | root / 123456 |
| Elasticsearch 6.8 | elasticsearch:6.8.3 | 19200 / 19300 | - |
| Kibana 6.8 | kibana:6.8.3 | 15601 | - |
| Elasticsearch 7.17 | elasticsearch:7.17.27 | 19201 / 19301 | - |
| Kibana 7.17 | kibana:7.17.27 | 15602 | - |
| OpenSearch 2.11 | opensearch:2.11.1 | 19202 / 19302 | - |
| OpenSearch Dashboards | opensearch-dashboards:2.11.1 | 15603 | - |
| Milvus 2.5 | milvusdb/milvus:v2.5.5 | 19530 (gRPC) / 19091 (HTTP) | - |
| MinIO (Milvus 存储) | minio | 19001 / 19002 | minioadmin / minioadmin |

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
