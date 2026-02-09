-- PostgreSQL pgvector 扩展初始化（容器首次启动时自动执行）
-- 该脚本在 POSTGRES_DB 指定的库上下文中执行，即 postgres 库
CREATE EXTENSION IF NOT EXISTS vector;
