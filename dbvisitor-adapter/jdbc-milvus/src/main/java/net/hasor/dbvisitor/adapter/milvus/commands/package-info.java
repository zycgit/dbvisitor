/**
 * SQL command execution grouped by responsibility.
 * <ul>
 *   <li>{@code query}: SELECT, COUNT, hybrid request construction and lazy result cursors.</li>
 *   <li>{@code write}: INSERT, UPSERT, UPDATE, DELETE and paged row mutations.</li>
 *   <li>{@code schema}: database, collection, partition, index and alias definitions.</li>
 *   <li>{@code admin}: users, permissions, loading, release, flush and loading progress.</li>
 *   <li>{@code imports}: import submission, bounded waiting and job inspection.</li>
 * </ul>
 * <p>Helpers used by only one group stay in that group and package-private. This
 * package contains only shared SQL binding, expressions, retry policy and JDBC
 * response support. Reusable value mappings belong to {@code milvus.mapping};
 * SDK/HTTP connection management belongs to {@code milvus.transport}.</p>
 * <p>Public command entry points and shared helpers enable cross-package calls
 * inside the adapter; they are not driver extension points. JDBC entry points,
 * connection keys and {@code CustomMilvus} retain their original package names.</p>
 */
package net.hasor.dbvisitor.adapter.milvus.commands;
