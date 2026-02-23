package com.dinhluong.dlmstore.dto.requests;
import lombok.Data;
import java.util.List;

@Data
public class AddToCartRequest {
    private Long productVariantId; // ID phiên bản sản phẩm chính (VD: iPhone 15 256GB Xanh)
    private Integer quantity;      // Số lượng (thường là 1)
    private List<Long> comboVariantIds; // Danh sách ID các phiên bản phụ kiện mua kèm (Rỗng nếu mua lẻ)
}