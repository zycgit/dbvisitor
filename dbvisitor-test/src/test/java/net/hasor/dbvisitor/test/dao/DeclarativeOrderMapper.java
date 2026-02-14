package net.hasor.dbvisitor.test.dao;

import java.math.BigDecimal;
import java.util.List;
import net.hasor.dbvisitor.mapper.*;
import net.hasor.dbvisitor.test.model.UserOrder;
import net.hasor.dbvisitor.test.model.UserOrderDTO;

/**
 * Declarative DAO interface for user_order operations using annotations.
 * Used by transaction and session tests.
 */
@SimpleMapper
public interface DeclarativeOrderMapper {

    @Insert("INSERT INTO user_order (user_id, order_no, amount, create_time) " + "VALUES (#{userId}, #{orderNo}, #{amount}, #{createTime})")
    int insertOrder(UserOrder order);

    @Query("SELECT * FROM user_order WHERE id = #{id}")
    UserOrder selectById(@Param("id") Integer id);

    @Query("SELECT * FROM user_order WHERE user_id = #{userId}")
    List<UserOrder> selectByUserId(@Param("userId") Integer userId);

    @Query("SELECT o.id as orderId, o.order_no as orderNo, o.amount, o.create_time as createTime, " + "u.id as userId, u.name as userName, u.email as userEmail " + "FROM user_order o LEFT JOIN user_info u ON o.user_id = u.id " + "WHERE o.id = #{orderId}")
    UserOrderDTO selectOrderWithUser(@Param("orderId") Integer orderId);

    @Update("UPDATE user_order SET amount = #{amount} WHERE id = #{id}")
    int updateAmount(@Param("id") Integer id, @Param("amount") BigDecimal amount);

    @Delete("DELETE FROM user_order WHERE id = #{id}")
    int deleteById(@Param("id") Integer id);

    @Query("SELECT COUNT(*) FROM user_order")
    int countAll();
}
