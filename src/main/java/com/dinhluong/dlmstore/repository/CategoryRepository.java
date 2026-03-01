package com.dinhluong.dlmstore.repository;

import com.dinhluong.dlmstore.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
   // Lấy các danh mục gốc (không có cha)
    List<Category> findByParentIsNull();
    boolean existsBySlug(String slug);
}
