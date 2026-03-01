package com.dinhluong.dlmstore.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.dinhluong.dlmstore.dto.requests.ProductComboRequest;
import com.dinhluong.dlmstore.dto.responses.AdminComboResponse;
import com.dinhluong.dlmstore.entity.Product;
import com.dinhluong.dlmstore.entity.ProductCombo;
import com.dinhluong.dlmstore.repository.ProductComboRepository;
import com.dinhluong.dlmstore.repository.ProductRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class AdminComboService {
     private final ProductComboRepository comboRepository;
        private final ProductRepository productRepository;
    
    public List<AdminComboResponse> getCombosByMainProduct(Long mainProductId) {
        List<ProductCombo> combos = comboRepository.findByMainProductId(mainProductId);
        
        return combos.stream().map(combo -> AdminComboResponse.builder()
                .id(combo.getId())
                .mainProductId(combo.getMainProduct().getId())
                .relatedProductId(combo.getRelatedProduct().getId())
                .relatedProductName(combo.getRelatedProduct().getName()) // Lấy tên phụ kiện
                .relatedProductThumbnail(combo.getRelatedProduct().getThumbnailUrl())
                .discountAmount(combo.getDiscountAmount())
                .note(combo.getNote())
                .build()
        ).collect(Collectors.toList());
    }

    // 2. Thêm mới Combo
    @Transactional
    public ProductCombo createCombo(ProductComboRequest request) {
        Product mainProduct = productRepository.findById(request.getMainProductId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm chính không tồn tại"));
                
        Product relatedProduct = productRepository.findById(request.getRelatedProductId())
                .orElseThrow(() -> new RuntimeException("Phụ kiện không tồn tại"));

        // Kiểm tra xem combo này đã tồn tại chưa (tránh add trùng 1 phụ kiện 2 lần)
        boolean exists = comboRepository.existsByMainProductIdAndRelatedProductId(
                request.getMainProductId(), request.getRelatedProductId());
        if (exists) {
            throw new RuntimeException("Phụ kiện này đã có trong combo của sản phẩm!");
        }

        ProductCombo combo = new ProductCombo();
        combo.setMainProduct(mainProduct);
        combo.setRelatedProduct(relatedProduct);
        combo.setDiscountAmount(request.getDiscountAmount());
        combo.setNote(request.getNote());

        return comboRepository.save(combo);
    }

    // 3. Xóa Combo
    @Transactional
    public void deleteCombo(Long comboId) {
        if (!comboRepository.existsById(comboId)) {
            throw new RuntimeException("Không tìm thấy Combo");
        }
        comboRepository.deleteById(comboId);
    }
}
