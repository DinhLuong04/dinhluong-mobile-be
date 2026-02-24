package com.dinhluong.dlmstore.repository;

import java.util.List;
import java.util.Optional;

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
}