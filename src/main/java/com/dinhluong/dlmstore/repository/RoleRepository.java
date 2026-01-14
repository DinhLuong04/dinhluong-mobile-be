package com.dinhluong.dlmstore.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.dinhluong.dlmstore.entity.Roles;
@Repository
public interface RoleRepository extends JpaRepository<Roles, Long> {

    Optional<Roles> findByName(String name);

    boolean existsByName(String name);
    
}
