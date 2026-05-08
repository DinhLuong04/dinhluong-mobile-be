package com.dinhluong.dlmstore.repository;

import com.dinhluong.dlmstore.entity.ProductCommentImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCommentImageRepository extends JpaRepository<ProductCommentImage, Long> {

    // (Tùy chọn) Ví dụ hàm lấy tất cả ảnh/video của 1 bình luận cụ thể
    List<ProductCommentImage> findByCommentId(Long commentId);

    // (Tùy chọn) Xóa tất cả ảnh của một bình luận
    void deleteByCommentId(Long commentId);
}
