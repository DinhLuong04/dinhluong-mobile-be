package com.dinhluong.dlmstore.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.dinhluong.dlmstore.repository.projections.UserStatsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dinhluong.dlmstore.entity.Users;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    // Tìm user theo email (login)
    Optional<Users> findByEmail(String email);

    long countByIsEnabledTrue();

    // Đếm tổng số user đang bị khóa
    long countByIsEnabledFalse();

    // Check tồn tại email (register)
    boolean existsByEmail(String email);

    Optional<Users> findByVerificationCode(String verificationCode);

    // Login kèm role (tránh LazyInitializationException)
    @Query("""
                SELECT u FROM Users u
                JOIN FETCH u.role
                WHERE u.email = :email
            """)
    Optional<Users> findByEmailWithRole(@Param("email") String email);

    // Lấy user theo provider + providerId (Google login)
    Optional<Users> findByAuthProviderAndProviderId(String authProvider, String providerId);

    // Check user active
    boolean existsByEmailAndIsEnabledTrue(String email);

    // Admin view - list user đang active
    List<Users> findAllByIsEnabledTrue();

    // Admin view - soft disable user
    @Modifying
    @Query("UPDATE Users u SET u.isEnabled = false WHERE u.id = :id")
    void disableUser(@Param("id") Long id);

    Optional<Users> findById(@Param("id") Long id);

    @Query("SELECT u FROM Users u WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "u.phone LIKE CONCAT('%', :keyword, '%')) AND " +
            "(:isEnabled IS NULL OR u.isEnabled = :isEnabled) " +
            "ORDER BY u.createdAt DESC")
    List<Users> searchAdminUsers(@Param("keyword") String keyword, @Param("isEnabled") Boolean isEnabled);

    @Query("SELECT COUNT(u) FROM Users u WHERE u.createdAt >= :startDate AND u.createdAt <= :endDate")
    long countNewUsers(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT u.email as email, u.full_name as fullName, u.phone as phone, " +
            "r.name as roleName, u.is_enabled as isEnabled, " +
            "COUNT(o.id) as totalOrders, " +
            "SUM(CASE WHEN o.status = 'DELIVERED' THEN 1 ELSE 0 END) as successOrders, " +
            "SUM(CASE WHEN o.status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelledOrders, " +
            // Đã sửa o.total_price thành o.total_amount
            "SUM(CASE WHEN o.status = 'DELIVERED' THEN o.total_amount ELSE 0 END) as totalSpent " +
            "FROM users u " +
            "JOIN roles r ON u.role_id = r.id " +
            "LEFT JOIN orders o ON u.id = o.user_id " +
            "WHERE (:keyword IS NULL OR u.email LIKE %:keyword% OR u.full_name LIKE %:keyword%) " +
            "AND (:isEnabled IS NULL OR u.is_enabled = :isEnabled) " +
            "GROUP BY u.id", nativeQuery = true)
    List<UserStatsProjection> searchUserStats(@Param("keyword") String keyword, @Param("isEnabled") Boolean isEnabled);

}