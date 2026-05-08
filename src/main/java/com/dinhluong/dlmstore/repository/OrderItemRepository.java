package com.dinhluong.dlmstore.repository;

import com.dinhluong.dlmstore.entity.OrderItem;
import com.dinhluong.dlmstore.repository.projections.DashboardProjections;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

        // Lấy TẤT CẢ sản phẩm trong 1 đơn hàng
        List<OrderItem> findByOrderId(Long orderId);

        @Query(value = "SELECT pv.id as id, p.name as productName, CONCAT(pv.ram, '/', pv.rom, ' - ', pv.color_name) as variantName, "
                        +
                        "SUM(oi.quantity) as sold, SUM(oi.quantity * oi.price_at_purchase) as revenue, pv.image_url as image "
                        +
                        "FROM order_items oi " +
                        "JOIN product_variants pv ON oi.product_variant_id = pv.id " +
                        "JOIN products p ON pv.product_id = p.id " +
                        "JOIN orders o ON oi.order_id = o.id " +
                        "WHERE o.status = 'DELIVERED' AND o.created_at >= :startDate AND o.created_at <= :endDate " +
                        "GROUP BY pv.id, p.name, pv.ram, pv.rom, pv.color_name, pv.image_url " +
                        "ORDER BY sold DESC LIMIT 5", nativeQuery = true)
        List<DashboardProjections.TopProductProjection> getTopProducts(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query(value = "SELECT b.name as name, SUM(oi.quantity * oi.price_at_purchase) as revenue " +
                        "FROM order_items oi " +
                        "JOIN product_variants pv ON oi.product_variant_id = pv.id " +
                        "JOIN products p ON pv.product_id = p.id " +
                        "JOIN brands b ON p.brand_id = b.id " +
                        "JOIN orders o ON oi.order_id = o.id " +
                        "WHERE o.status = 'DELIVERED' AND o.created_at >= :startDate AND o.created_at <= :endDate " +
                        "GROUP BY b.id, b.name " +
                        "ORDER BY revenue DESC LIMIT 5", nativeQuery = true)
        List<DashboardProjections.TopBrandProjection> getTopBrands(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

}
