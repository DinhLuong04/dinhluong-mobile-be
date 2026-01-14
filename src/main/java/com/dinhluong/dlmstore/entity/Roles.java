package com.dinhluong.dlmstore.entity;

import lombok.*;

import java.util.Set;

import jakarta.persistence.*;
@Entity
@Table(name = "roles")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Roles {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "role")
    private Set<Users> users;
}
