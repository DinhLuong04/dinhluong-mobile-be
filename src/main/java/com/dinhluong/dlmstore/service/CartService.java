package com.dinhluong.dlmstore.service;

import com.dinhluong.dlmstore.dto.requests.AddToCartRequest;
import com.dinhluong.dlmstore.dto.responses.CartResponse;
import com.dinhluong.dlmstore.entity.Cart;
import com.dinhluong.dlmstore.entity.CartItem;
import com.dinhluong.dlmstore.entity.Product;
import com.dinhluong.dlmstore.entity.ProductCombo;
import com.dinhluong.dlmstore.entity.ProductVariant;
import com.dinhluong.dlmstore.repository.CartRepository;
import com.dinhluong.dlmstore.repository.ProductComboRepository;
import com.dinhluong.dlmstore.repository.CartItemRepository;
import com.dinhluong.dlmstore.repository.ProductVariantRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private ProductComboRepository productComboRepository;

    // =========================================================================
    // 1. THÊM VÀO GIỎ HÀNG
    // =========================================================================
    @Transactional
    public void addToCart(Long userId, AddToCartRequest request) {

        // 1. Lấy hoặc tạo cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(new Cart(userId)));

        int addQty = request.getQuantity() != null ? request.getQuantity() : 1;

        // 2. Kiểm tra SP chính đã tồn tại chưa
        CartItem mainItem = cartItemRepository
                .findByCartIdAndProductVariantIdAndParentIdIsNull(
                        cart.getId(),
                        request.getProductVariantId())
                .orElse(null);

        if (mainItem != null) {
            // 👉 ĐÃ TỒN TẠI → TĂNG SỐ LƯỢNG
            mainItem.setQuantity(mainItem.getQuantity() + addQty);
            cartItemRepository.save(mainItem);

        } else {
            // 👉 CHƯA TỒN TẠI → TẠO MỚI
            mainItem = new CartItem();
            mainItem.setCartId(cart.getId());
            mainItem.setProductVariantId(request.getProductVariantId());
            mainItem.setQuantity(addQty);
            mainItem.setParentId(null);

            mainItem = cartItemRepository.save(mainItem);
        }

        // 3. Xử lý COMBO (chỉ add nếu FE gửi lên)
        if (request.getComboVariantIds() != null
                && !request.getComboVariantIds().isEmpty()) {

            // Lấy danh sách combo hiện có
            List<CartItem> existingCombos = cartItemRepository.findByParentId(mainItem.getId());

            Set<Long> existingComboVariantIds = existingCombos.stream()
                    .map(CartItem::getProductVariantId)
                    .collect(Collectors.toSet());

            for (Long comboVariantId : request.getComboVariantIds()) {

                // Nếu combo đã tồn tại → bỏ qua (không nhân đôi)
                if (existingComboVariantIds.contains(comboVariantId)) {
                    continue;
                }

                CartItem comboItem = new CartItem();
                comboItem.setCartId(cart.getId());
                comboItem.setProductVariantId(comboVariantId);
                comboItem.setQuantity(1);
                comboItem.setParentId(mainItem.getId());

                cartItemRepository.save(comboItem);
            }
        }
    }

    public CartResponse getCartByUserId(Long userId) {

        CartResponse response = new CartResponse();
        List<CartResponse.CartItemDto> dtoList = new ArrayList<>();

        // 1. Lấy cart
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) {
            response.setItems(dtoList);
            return response;
        }

        // 2. Lấy list item trong giỏ
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        // 3. Lấy danh sách productVariant ID của SP chính
        List<Long> mainVariantIds = items.stream()
                .filter(i -> i.getParentId() == null)
                .map(CartItem::getProductVariantId)
                .toList();

        // 4. Lấy sản phẩm để suy ra product_id
        Map<Long, ProductVariant> variantMap = productVariantRepository
                .findAllById(mainVariantIds)
                .stream()
                .collect(Collectors.toMap(ProductVariant::getId, v -> v));

        // 5. Lấy danh sách productId của SP chính
        List<Long> mainProductIds = variantMap.values()
                .stream()
                .map(v -> v.getProduct().getId())
                .toList();

        // 6. 🔥 QUERY COMBO TỪ product_combos
        List<ProductCombo> combos = productComboRepository.findByMainProductIds(mainProductIds);

        // Convert sang map để truy nhanh
        Map<Long, List<ProductCombo>> comboMap = combos.stream()
                .collect(Collectors.groupingBy(pc -> pc.getMainProduct().getId()));

        // 7. Build DTO
        for (CartItem ci : items) {
            if (ci.getParentId() != null)
                continue; // skip phụ kiện

            ProductVariant variant = variantMap.get(ci.getProductVariantId());

            CartResponse.CartItemDto dto = new CartResponse.CartItemDto();
            dto.setId(ci.getId());
            dto.setProductVariantId(variant.getId());
            dto.setSku(variant.getSku());
            dto.setName(variant.getProduct().getName());
            dto.setSlug(variant.getProduct().getSlug());
            dto.setImage(variant.getImageUrl());
            dto.setPrice(variant.getPrice());
            dto.setOriginalPrice(variant.getProduct().getOriginalPrice());
            dto.setColorName(variant.getColorName());
            dto.setRom(variant.getRom());
            dto.setQuantity(ci.getQuantity());

            // 8. 🔥 LẤY COMBO CHO SẢN PHẨM NÀY
            List<ProductCombo> pcList = comboMap.getOrDefault(
                    variant.getProduct().getId(),
                    List.of());

            List<CartResponse.CartComboItemDto> comboDtos = pcList.stream().map(pc -> {
                CartResponse.CartComboItemDto c = new CartResponse.CartComboItemDto();

                Product rp = pc.getRelatedProduct();
                c.setId(rp.getId());
                c.setName(rp.getName());
                c.setImage(rp.getThumbnailUrl());
                c.setOriginalPrice(rp.getOriginalPrice());
                c.setPrice(rp.getDisplayPrice());
                c.setDiscountAmount(pc.getDiscountAmount());
                c.setNote(pc.getNote());
                c.setChecked(false);

                return c;
            }).toList();

            dto.setCombos(comboDtos);
            dtoList.add(dto);
        }

        response.setItems(dtoList);
        return response;
    }

    // =========================================================================
    // 3. CẬP NHẬT SỐ LƯỢNG SẢN PHẨM
    // =========================================================================
    @Transactional
    public void updateQuantity(Long cartItemId, Integer quantity) {
        if (quantity <= 0) {
            removeItem(cartItemId);
            return;
        }

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ"));

        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    // =========================================================================
    // 4. XÓA SẢN PHẨM (XÓA KÉO THEO CẢ COMBO)
    // =========================================================================
    @Transactional
    public void removeItem(Long cartItemId) {
        boolean exists = cartItemRepository.existsById(cartItemId);
        if (!exists)
            return;

        // Sau đó xóa chính nó
        cartItemRepository.deleteById(cartItemId);
    }
}