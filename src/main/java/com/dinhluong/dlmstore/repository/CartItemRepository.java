package com.dinhluong.dlmstore.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dinhluong.dlmstore.entity.CartItem;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // Tìm các sản phẩm trong giỏ hàng theo cart_id
    List<CartItem> findByCartId(Long cartId);

    // Tìm SP chính đã tồn tại trong cart
    Optional<CartItem> findByCartIdAndProductVariantIdAndParentIdIsNull(
            Long cartId,
            Long productVariantId);

    // Tìm combo theo parent
    List<CartItem> findByParentId(Long parentId);
}
