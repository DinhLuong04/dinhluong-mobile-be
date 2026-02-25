package com.dinhluong.dlmstore.repository;

import com.dinhluong.dlmstore.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);
    List<Category> findByParentIsNull(); // Lấy danh sách các danh mục gốc (Level cao nhất)
}