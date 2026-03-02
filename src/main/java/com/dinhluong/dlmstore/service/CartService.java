package com.dinhluong.dlmstore.service;

import com.dinhluong.dlmstore.dto.requests.AddToCartRequest;
import com.dinhluong.dlmstore.dto.responses.CartResponse;
import com.dinhluong.dlmstore.entity.*;
import com.dinhluong.dlmstore.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private ProductVariantRepository productVariantRepository;
    @Autowired private ProductComboRepository productComboRepository;

    @Transactional
    public void addToCart(Long userId, AddToCartRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(new Cart(userId)));

        // 🔥 1. KIỂM TRA TỒN KHO TRƯỚC KHI THÊM
        ProductVariant variant = productVariantRepository.findById(request.getProductVariantId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        int addQty = request.getQuantity() != null ? request.getQuantity() : 1;

        CartItem mainItem = cartItemRepository
                .findByCartIdAndProductVariantIdAndParentIdIsNull(cart.getId(), request.getProductVariantId())
                .orElse(null);

        int currentQtyInCart = (mainItem != null) ? mainItem.getQuantity() : 0;
        int totalRequestedQty = currentQtyInCart + addQty;

        // Chặn nếu vượt tồn kho
        if (totalRequestedQty > variant.getStockQuantity()) {
            throw new RuntimeException("Sản phẩm này chỉ còn " + variant.getStockQuantity() + " chiếc trong kho.");
        }

        if (mainItem != null) {
            mainItem.setQuantity(totalRequestedQty);
            cartItemRepository.save(mainItem);
        } else {
            mainItem = new CartItem();
            mainItem.setCartId(cart.getId());
            mainItem.setProductVariantId(request.getProductVariantId());
            mainItem.setQuantity(addQty);
            mainItem.setParentId(null);
            mainItem = cartItemRepository.save(mainItem);
        }

        // Xử lý COMBO (giữ nguyên của bạn)
        if (request.getComboVariantIds() != null && !request.getComboVariantIds().isEmpty()) {
            List<CartItem> existingCombos = cartItemRepository.findByParentId(mainItem.getId());
            Set<Long> existingComboVariantIds = existingCombos.stream()
                    .map(CartItem::getProductVariantId).collect(Collectors.toSet());

            for (Long comboVariantId : request.getComboVariantIds()) {
                if (existingComboVariantIds.contains(comboVariantId)) continue;
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

        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) {
            response.setItems(dtoList);
            return response;
        }

        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        List<Long> mainVariantIds = items.stream()
                .filter(i -> i.getParentId() == null).map(CartItem::getProductVariantId).toList();

        Map<Long, ProductVariant> variantMap = productVariantRepository.findAllById(mainVariantIds)
                .stream().collect(Collectors.toMap(ProductVariant::getId, v -> v));

        List<Long> mainProductIds = variantMap.values().stream()
                .map(v -> v.getProduct().getId()).toList();

        List<ProductCombo> combos = productComboRepository.findByMainProductIds(mainProductIds);
        Map<Long, List<ProductCombo>> comboMap = combos.stream()
                .collect(Collectors.groupingBy(pc -> pc.getMainProduct().getId()));

        for (CartItem ci : items) {
            if (ci.getParentId() != null) continue; 
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
            
            // 🔥 TRẢ VỀ TỒN KHO THỰC TẾ CHO FRONTEND
            dto.setStockQuantity(variant.getStockQuantity()); 

            List<ProductCombo> pcList = comboMap.getOrDefault(variant.getProduct().getId(), List.of());
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

    @Transactional
    public void updateQuantity(Long cartItemId, Integer quantity) {
        if (quantity <= 0) {
            removeItem(cartItemId);
            return;
        }

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ"));

        // 🔥 KIỂM TRA TỒN KHO KHI UPDATE SỐ LƯỢNG TRONG GIỎ
        ProductVariant variant = productVariantRepository.findById(item.getProductVariantId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        if (quantity > variant.getStockQuantity()) {
            throw new RuntimeException("Sản phẩm này chỉ còn " + variant.getStockQuantity() + " chiếc trong kho.");
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    @Transactional
    public void removeItem(Long cartItemId) {
        if (!cartItemRepository.existsById(cartItemId)) return;
        cartItemRepository.deleteById(cartItemId);
    }
}