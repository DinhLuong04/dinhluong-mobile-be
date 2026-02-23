package com.dinhluong.dlmstore.entity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.api.client.util.DateTime;

import jakarta.persistence.*;
@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "email", unique = true)
    private String email;
    @Column(name = "password")
    private String password;
    @Column(name = "full_name")
    private String fullName;
    @Column(name = "phone")
    private String phone;
    @Column(name = "avatar_url")
    private String avatarUrl;
    @Column(name = "auth_provider")
    private String authProvider;
    @Column(name = "provider_id")
    private String providerId;
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Roles role;
    @Column(name = "is_enabled")
    private Boolean isEnabled;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "verification_code")
    private String verificationCode; 

    @Column(name = "reset_password_token")
    private String resetPasswordToken; 

    @Column(name = "token_expiry_date")
    private LocalDateTime tokenExpiryDate;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // Tránh lỗi infinite recursion khi parse JSON
    private List<Address> addresses;
}
