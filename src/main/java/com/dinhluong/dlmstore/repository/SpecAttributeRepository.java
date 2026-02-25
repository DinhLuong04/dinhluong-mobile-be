package com.dinhluong.dlmstore.repository;

import com.dinhluong.dlmstore.entity.SpecAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecAttributeRepository extends JpaRepository<SpecAttribute, Long> {
    List<SpecAttribute> findByGroupIdOrderBySortOrderAsc(Long groupId);
}