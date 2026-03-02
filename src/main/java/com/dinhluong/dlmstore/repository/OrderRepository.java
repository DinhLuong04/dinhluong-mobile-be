package com.dinhluong.dlmstore.repository;

import com.dinhluong.dlmstore.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
       // Hàm này để sau này làm API: Lấy danh sách đơn hàng của 1 user
       List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

       // Lấy đơn hàng theo trạng thái
       List<Order> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, Order.OrderStatus status);

       // Lấy 5 đơn hàng gần nhất (Cho màn Overview)
       List<Order> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);

       // Tính tổng tiền các đơn hàng ĐÃ GIAO THÀNH CÔNG (để tính hạng thành viên)
       @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.userId = :userId AND o.status = 'DELIVERED'")
       BigDecimal getTotalSpentByUserId(@Param("userId") Long userId);

       // Đếm số lượng đơn hàng thành công
       Integer countByUserIdAndStatus(Long userId, Order.OrderStatus status);

       @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END " +
                     "FROM Order o " +
                     "JOIN OrderItem oi ON o.id = oi.orderId " +
                     "JOIN ProductVariant pv ON oi.productVariantId = pv.id " +
                     "WHERE o.userId = :userId " +
                     "AND pv.product.id = :productId " +
                     "AND o.status = 'DELIVERED'")
       boolean hasUserPurchasedProduct(@Param("userId") Long userId, @Param("productId") Long productId);

       @Query("SELECT o FROM Order o WHERE " +
                     "(:status IS NULL OR o.status = :status) AND " +
                     "(:keyword IS NULL OR :keyword = '' OR LOWER(o.receiverName) LIKE LOWER(CONCAT('%', :keyword, '%')) "
                     +
                     "OR o.receiverPhone LIKE CONCAT('%', :keyword, '%') " +
                     "OR CAST(o.id AS string) LIKE CONCAT('%', :keyword, '%')) " +
                     "ORDER BY o.createdAt DESC")
       List<Order> searchAdminOrders(@Param("status") Order.OrderStatus status, @Param("keyword") String keyword);

       // Đếm tổng số đơn hàng của 1 user
       long countByUserId(Long userId);

       @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.userId = :userId AND o.status = :status")
       BigDecimal getTotalSpentByUserIdAndStatus(@Param("userId") Long userId,
                     @Param("status") Order.OrderStatus status);

       @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'DELIVERED' AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
       BigDecimal sumRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

       @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'DELIVERED' AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
       long countCompletedOrders(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

       @Query("SELECT o FROM Order o WHERE o.status = 'DELIVERED' AND o.createdAt >= :startDate AND o.createdAt <= :endDate ORDER BY o.createdAt ASC")
       List<Order> findDeliveredOrdersForTrends(@Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate);
}