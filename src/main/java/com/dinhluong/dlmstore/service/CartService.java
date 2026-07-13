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

        ProductVariant variant = productVariantRepository.findById(request.getProductVariantId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        // 🔥 Kiểm tra thêm xem sản phẩm cha có bị xóa không
        if (variant.getProduct() == null) {
            throw new RuntimeException("Sản phẩm này đã ngừng kinh doanh!");
        }

        int addQty = request.getQuantity() != null ? request.getQuantity() : 1;
        CartItem mainItem = cartItemRepository
                .findByCartIdAndProductVariantIdAndParentIdIsNull(cart.getId(), request.getProductVariantId())
                .orElse(null);

        int currentQtyInCart = (mainItem != null) ? mainItem.getQuantity() : 0;
        int totalRequestedQty = currentQtyInCart + addQty;

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

    @Transactional
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

        // 🔥 FIX 1: Lọc bỏ variant có product bị null (đã bị xóa mềm)
        List<Long> mainProductIds = variantMap.values().stream()
                .filter(v -> v.getProduct() != null)
                .map(v -> v.getProduct().getId()).toList();

        List<ProductCombo> combos = productComboRepository.findByMainProductIds(mainProductIds);

        // 🔥 FIX 2: Lọc bỏ combo có sản phẩm cha bị null
        Map<Long, List<ProductCombo>> comboMap = combos.stream()
                .filter(pc -> pc.getMainProduct() != null)
                .collect(Collectors.groupingBy(pc -> pc.getMainProduct().getId()));

        for (CartItem ci : items) {
            if (ci.getParentId() != null) continue;
            ProductVariant variant = variantMap.get(ci.getProductVariantId());

            // 🔥 FIX 3: KIỂM TRA CẢ VARIANT VÀ PRODUCT CHA
            if (variant == null || variant.getProduct() == null) {
                List<CartItem> combosToClean = cartItemRepository.findByParentId(ci.getId());
                if (!combosToClean.isEmpty()) cartItemRepository.deleteAll(combosToClean);
                cartItemRepository.delete(ci);
                continue;
            }

            CartResponse.CartItemDto dto = new CartResponse.CartItemDto();
            dto.setId(ci.getId());
            dto.setProductVariantId(variant.getId());
            dto.setSku(variant.getSku());
            dto.setName(variant.getProduct().getName()); // Sẽ không còn lỗi ở đây
            dto.setSlug(variant.getProduct().getSlug());
            dto.setImage(variant.getProduct().getThumbnailUrl());
            dto.setPrice(variant.getPrice());
            dto.setOriginalPrice(variant.getProduct().getOriginalPrice());
            dto.setColorName(variant.getColorName());
            dto.setRom(variant.getRom());
            dto.setQuantity(ci.getQuantity());
            dto.setStockQuantity(variant.getStockQuantity());

            List<ProductCombo> pcList = comboMap.getOrDefault(variant.getProduct().getId(), List.of());
            List<CartResponse.CartComboItemDto> comboDtos = pcList.stream().map(pc -> {
                CartResponse.CartComboItemDto c = new CartResponse.CartComboItemDto();
                Product rp = pc.getRelatedProduct();
                // 🔥 Bảo vệ Related Product trong Combo
                if (rp == null) return null;
                c.setId(rp.getId());
                c.setName(rp.getName());
                c.setImage(rp.getThumbnailUrl());
                c.setOriginalPrice(rp.getOriginalPrice());
                c.setPrice(rp.getDisplayPrice());
                c.setDiscountAmount(pc.getDiscountAmount());
                c.setNote(pc.getNote());
                c.setChecked(false);
                return c;
            }).filter(c -> c != null).toList();

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

        ProductVariant variant = productVariantRepository.findById(item.getProductVariantId())
                .orElseThrow(() -> new RuntimeException("Biến thể không tồn tại!"));

        // 🔥 FIX 4: Kiểm tra product cha khi update số lượng
        if (variant.getProduct() == null) {
            removeItem(cartItemId);
            throw new RuntimeException("Sản phẩm này đã ngừng kinh doanh!");
        }

        if (quantity > variant.getStockQuantity()) {
            throw new RuntimeException("Sản phẩm này chỉ còn " + variant.getStockQuantity() + " chiếc trong kho.");
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    @Transactional
    public void removeItem(Long cartItemId) {
        if (!cartItemRepository.existsById(cartItemId)) return;
        List<CartItem> childCombos = cartItemRepository.findByParentId(cartItemId);
        if (!childCombos.isEmpty()) cartItemRepository.deleteAll(childCombos);
        cartItemRepository.deleteById(cartItemId);
    }
}