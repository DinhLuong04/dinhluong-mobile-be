package com.dinhluong.dlmstore.dto.responses;



import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CartResponse {
    
    // Tùy chọn: Bạn có thể trả về tổng tiền luôn từ Backend nếu muốn
    // private BigDecimal cartTotal;
    
    // Danh sách các sản phẩm chính trong giỏ hàng
    private List<CartItemDto> items;

    // --- CÁC CLASS CON BÊN TRONG ---

    @Data
    public static class CartItemDto {
        // --- Thông tin định danh ---
        private Long id; 
        private Long productVariantId;             // ID của bảng cart_items (cartItemId)
        private String sku;           // Mã SKU của biến thể

        // --- Thông tin hiển thị ---
        private String name;          // Tên sản phẩm
        private String slug;          // Slug để FE làm link
        private String image;         // Link ảnh (Lấy theo biến thể hoặc ảnh mặc định của SP)

        // --- Thông tin giá & Biến thể ---
        private BigDecimal price;         // Giá bán hiện tại
        private BigDecimal originalPrice; // Giá gốc (nếu có giảm giá)
        private String colorName;     // Màu sắc (VD: Xám Titan)
        private String rom;           // Dung lượng (VD: 256 GB)

        // --- Trạng thái (UI State) ---
        private Integer quantity;     // Số lượng
        private Boolean checked = true; // Mặc định trả về true để FE tự động tích chọn
        private Integer stockQuantity;
        // --- Dữ liệu lồng nhau ---
        private List<CartComboItemDto> combos; // Danh sách phụ kiện đi kèm
    }

    @Data
    public static class CartComboItemDto {
        private Long id;              // ID của bảng cart_items (dòng phụ kiện)
        private String name;          // Tên phụ kiện
        private String image;         // Ảnh phụ kiện
        private BigDecimal price;         // Giá phụ kiện mua kèm
        private BigDecimal originalPrice; // Giá gốc phụ kiện (nếu có)
        private Boolean checked =false;
        private BigDecimal discountAmount; // Số tiền được giảm
        private Integer stockQuantity;
        private String note;    // Mặc định tự động tích
    }
}