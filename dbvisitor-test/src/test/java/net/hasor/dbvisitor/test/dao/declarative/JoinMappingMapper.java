package net.hasor.dbvisitor.test.dao.declarative;

import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.test.model.UserOrderDTO;

/**
 * Mapper for JOIN query result mapping tests.
 * Covers: INNER JOIN to DTO, INNER JOIN to Map, LEFT JOIN with nulls,
 * column alias mapping, one-to-many, aggregate JOINs.
 */
@SimpleMapper
public interface JoinMappingMapper {

    /** INNER JOIN → DTO (flat mapping) */
    @Query("SELECT o.id AS orderId, o.order_no AS orderNo, o.amount, o.create_time AS createTime, " +//
            "u.id AS userId, u.name AS userName, u.email AS userEmail " +//
            "FROM user_order o INNER JOIN user_info u ON o.user_id = u.id " +//
            "WHERE u.id = #{userId} ORDER BY o.id ASC")
    List<UserOrderDTO> selectUserOrderDTO(@Param("userId") Integer userId);

    /** INNER JOIN → Map (dynamic column names) */
    @Query("SELECT o.id AS order_id, o.order_no, o.amount, " +//
            "u.id AS user_id, u.name AS user_name, u.email AS user_email " +//
            "FROM user_order o INNER JOIN user_info u ON o.user_id = u.id " +//
            "WHERE u.id = #{userId} ORDER BY o.id ASC")
    List<Map<String, Object>> selectUserOrderMap(@Param("userId") Integer userId);

    /** INNER JOIN with column aliases → DTO (partial fields) */
    @Query("SELECT o.id AS orderId, o.order_no AS orderNo, " +//
            "u.id AS userId, u.name AS userName " +//
            "FROM user_order o INNER JOIN user_info u ON o.user_id = u.id " +//
            "WHERE u.id = #{userId} ORDER BY o.id ASC")
    List<UserOrderDTO> selectUserOrderWithAlias(@Param("userId") Integer userId);

    /** LEFT JOIN → Map (right side may be null) */
    @Query("SELECT u.id AS user_id, u.name AS user_name, " +//
            "o.id AS order_id, o.order_no " +//
            "FROM user_info u LEFT JOIN user_order o ON u.id = o.user_id " +//
            "WHERE u.id = #{userId}")
    List<Map<String, Object>> selectUserOrderLeftJoin(@Param("userId") Integer userId);

    /** LEFT JOIN → DTO (right side may have null fields) */
    @Query("SELECT u.id AS userId, u.name AS userName, u.email AS userEmail, " +//
            "o.id AS orderId, o.order_no AS orderNo, o.amount " +//
            "FROM user_info u LEFT JOIN user_order o ON u.id = o.user_id " +//
            "WHERE u.id = #{userId}")
    List<UserOrderDTO> selectUserOrderLeftJoinDTO(@Param("userId") Integer userId);

    /** JOIN with aggregation (GROUP BY) */
    @Query("SELECT u.id AS user_id, u.name AS user_name, " +//
            "COUNT(o.id) AS order_count, COALESCE(SUM(o.amount), 0) AS total_amount " +//
            "FROM user_info u LEFT JOIN user_order o ON u.id = o.user_id " +//
            "WHERE u.id = #{userId} GROUP BY u.id, u.name")
    Map<String, Object> selectUserOrderAggregate(@Param("userId") Integer userId);

    /** Mixed data types from JOIN */
    @Query("SELECT u.age, u.name, o.amount, o.order_no " +//
            "FROM user_info u INNER JOIN user_order o ON u.id = o.user_id " +//
            "WHERE u.id = #{userId}")
    List<Map<String, Object>> selectUserOrderMixedTypes(@Param("userId") Integer userId);
}
