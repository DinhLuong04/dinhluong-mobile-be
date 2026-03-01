package com.dinhluong.dlmstore.controller.Admin;

import com.dinhluong.dlmstore.dto.ApiResponse;
import com.dinhluong.dlmstore.dto.requests.BulkStockUpdateRequest;
import com.dinhluong.dlmstore.dto.requests.ProductRequest;
import com.dinhluong.dlmstore.dto.responses.ProductResponse;
import com.dinhluong.dlmstore.dto.responses.ProductVariantResponse;
import com.dinhluong.dlmstore.entity.Product;
import com.dinhluong.dlmstore.entity.Enums.ProductStatus;
import com.dinhluong.dlmstore.entity.Enums.ProductType;
import com.dinhluong.dlmstore.service.AdminProductService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;

    @GetMapping
    public ResponseEntity<?> getProducts(
            @RequestParam(required = false) ProductType productType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) Long brandId,     // <--- Hứng brandId
            @RequestParam(required = false) Long categoryId,  // <--- Hứng categoryId
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        
        Pageable pageable = PageRequest.of(page, size);
        
        // <-- TRUYỀN THÊM productType VÀO HÀM (Cho khớp với Service đã sửa ở bước trước)
        Page<ProductResponse> products = adminProductService.getAdminProducts(productType, keyword, status, brandId, categoryId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Thành công", products));
    }

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<String>> toggleStatus(@PathVariable Long id) {
        try {
            adminProductService.toggleProductStatus(id);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable Long id) {
        try {
            adminProductService.deleteProduct(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa sản phẩm thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        ProductRequest product = adminProductService.getProductByIdForEdit(id);
        return ResponseEntity.ok(ApiResponse.success("Thành công", product));
    }

    private final ObjectMapper objectMapper; // Dùng để chuyển String -> ProductRequest

   // TẠO MỚI SẢN PHẨM (MULTIPART)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(
            @RequestPart("data") String dataJson, 
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestPart(value = "gallery", required = false) List<MultipartFile> gallery) {
        
        try {
            ProductRequest request = objectMapper.readValue(dataJson, ProductRequest.class);
            request.setId(null); 
            
            // Đã sửa 'saveProduct' thành 'createProduct' cho khớp với Service
            Product savedProduct = adminProductService.createProduct(request, thumbnail, gallery);
            return ResponseEntity.ok(ApiResponse.success("Thêm mới thành công", savedProduct.getId()));
        } catch (Exception e) {
            // SỬA Ở ĐÂY: Nếu ApiResponse nhận 2 tham số (int status, String message)
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Lỗi: " + e.getMessage()));
            
            // LƯU Ý: Nếu ApiResponse.error() của bạn chỉ nhận 1 chuỗi String, hãy dùng dòng này thay thế:
            // return ResponseEntity.badRequest().body(ApiResponse.error("400 - Lỗi: " + e.getMessage()));
        }
    }

    // CẬP NHẬT SẢN PHẨM (MULTIPART)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestPart("data") String dataJson,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestPart(value = "gallery", required = false) List<MultipartFile> gallery) {
        
        try {
            ProductRequest request = objectMapper.readValue(dataJson, ProductRequest.class);
            request.setId(id);
            
            // Đã bổ sung biến 'id' vào hàm updateProduct cho khớp với Service
            Product savedProduct = adminProductService.updateProduct(id, request, thumbnail, gallery);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật thành công", savedProduct.getId()));
        } catch (Exception e) {
            // SỬA Ở ĐÂY: Thêm mã lỗi 400 vào trước chuỗi message
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Lỗi: " + e.getMessage()));
            
            // Hoặc tương tự như trên nếu method chỉ nhận String: 
            // return ResponseEntity.badRequest().body(ApiResponse.error("400 - Lỗi: " + e.getMessage()));
        }
    }

    // BẬT / TẮT NỔI BẬT
    @PutMapping("/{id}/toggle-featured")
    public ResponseEntity<?> toggleFeatured(@PathVariable Long id) {
        try {
            adminProductService.toggleProductFeatured(id);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái nổi bật thành công", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Lỗi: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/variants")
    public ResponseEntity<?> getProductVariants(@PathVariable Long id) {
        try {
            Object variants = adminProductService.getProductVariants(id);

            return ResponseEntity.ok(ApiResponse.success("Lấy biến thể thành công", variants));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    // CẬP NHẬT TỒN KHO HÀNG LOẠT
    @PutMapping("/variants/bulk-stock")
    public ResponseEntity<?> updateBulkStock(@RequestBody BulkStockUpdateRequest request) {
        try {
            adminProductService.updateBulkVariantStock(request);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật kho hàng loạt thành công", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Lỗi: " + e.getMessage()));
        }
    }


    

    
}