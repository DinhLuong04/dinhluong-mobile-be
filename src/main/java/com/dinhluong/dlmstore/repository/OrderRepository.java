package com.dinhluong.dlmstore.repository;

import com.dinhluong.dlmstore.entity.Enums.OrderStatus;
import com.dinhluong.dlmstore.entity.Order;

import com.dinhluong.dlmstore.repository.projections.DashboardProjections;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

       // 1. Truy vấn đếm số lượng đơn hàng theo từng trạng thái
       @Query("SELECT o.status, COUNT(o.id) FROM Order o GROUP BY o.status")
       List<Object[]> countOrdersByStatus();

       // 2. Cập nhật trạng thái hàng loạt (Bulk Update)
       @Modifying
       @Query("UPDATE Order o SET o.status = :status WHERE o.id IN :ids")
       int updateStatusBatch(@Param("ids") List<Long> ids, @Param("status") Order.OrderStatus status);

       // Query 1: Thống kê lý do hủy đơn
    @Query("SELECT o.reason AS reason, COUNT(o.id) AS count " +
           "FROM Order o " +
           "WHERE o.status = 'CANCELLED' " +
           "AND o.createdAt BETWEEN :start AND :end " +
           "AND o.reason IS NOT NULL " +
           "GROUP BY o.reason ORDER BY count DESC")
    List<DashboardProjections.CancellationProjection> countByReason(
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Query 2: Lấy dữ liệu tổng hợp để tính hiệu suất (Performance)
    @Query("SELECT COUNT(o.id) AS totalOrders, " +
           "SUM(CASE WHEN o.status = 'DELIVERED' THEN 1 ELSE 0 END) AS completedCount, " +
           "SUM(CASE WHEN o.status = 'RETURNED' THEN 1 ELSE 0 END) AS returnedCount, " +
           "SUM(CASE WHEN o.status IN ('CANCELLED', 'RETURNED') THEN o.totalAmount ELSE 0 END) AS lostRevenue " +
           "FROM Order o " +
           "WHERE o.createdAt BETWEEN :start AND :end")
    DashboardProjections.PerformanceProjection getPerformanceData(
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

       @Query("SELECT DISTINCT o FROM Order o, OrderItem oi, ProductVariant pv " +
               "WHERE o.id = oi.orderId " +
               "AND oi.productVariantId = pv.id " +
               "AND pv.product.id = :productId " +
               "AND o.status IN (:statuses)")
       List<Order> findUnfinishedOrdersByProductId(
               @Param("productId") Long productId,
               @Param("statuses") List<OrderStatus> statuses
       );
}