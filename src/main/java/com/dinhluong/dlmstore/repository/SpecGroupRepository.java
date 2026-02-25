package com.dinhluong.dlmstore.repository;

import com.dinhluong.dlmstore.entity.SpecGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecGroupRepository extends JpaRepository<SpecGroup, Long> {
    // Sắp xếp theo thứ tự sort_order tăng dần
    List<SpecGroup> findAllByOrderBySortOrderAsc();
}