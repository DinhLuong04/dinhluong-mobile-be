package com.dinhluong.dlmstore.service;

import com.dinhluong.dlmstore.dto.requests.BulkStockUpdateRequest;
import com.dinhluong.dlmstore.dto.requests.ProductComboRequest;
import com.dinhluong.dlmstore.dto.requests.ProductRequest;
import com.dinhluong.dlmstore.dto.responses.ProductResponse;
import com.dinhluong.dlmstore.dto.responses.ProductVariantResponse;
import com.dinhluong.dlmstore.entity.Product;
import com.dinhluong.dlmstore.entity.ProductCombo;
import com.dinhluong.dlmstore.entity.ProductHighlightSpec;
import com.dinhluong.dlmstore.entity.ProductImage;
import com.dinhluong.dlmstore.entity.ProductSpecValue;
import com.dinhluong.dlmstore.entity.ProductVariant;
import com.dinhluong.dlmstore.entity.Enums.ProductStatus;
import com.dinhluong.dlmstore.entity.Enums.ProductType;
import com.dinhluong.dlmstore.repository.BrandRepository;
import com.dinhluong.dlmstore.repository.CategoryRepository;
import com.dinhluong.dlmstore.repository.ProductComboRepository;
import com.dinhluong.dlmstore.repository.ProductRepository;
import com.dinhluong.dlmstore.repository.ProductVariantRepository;
import com.dinhluong.dlmstore.repository.SpecAttributeRepository;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductService productService; 
    private final CloudinaryService cloudinaryService;
    private final ProductVariantRepository productVariantRepository;
   
    // [BỔ SUNG] Repository để tìm SpecAttribute khi lưu ProductSpecValue
    private final SpecAttributeRepository specAttributeRepository;

    // =========================================================
    // 1. API LẤY DANH SÁCH (CÓ PHÂN TRANG VÀ LỌC)
    // =========================================================
 @Transactional(readOnly = true)
    public Page<ProductResponse> getAdminProducts(ProductType productType, String keyword, ProductStatus status, Long brandId, Long categoryId, Pageable pageable) {
        
        // Truyền tiếp xuống Repository
        Page<Product> products = productRepository.findWithFilters(productType, keyword, status, brandId, categoryId, pageable);
        
        return products.map(p -> {
            int outOfStockCount = p.getVariants() != null ? 
                (int) p.getVariants().stream()
                    .filter(v -> v.getStockQuantity() == null || v.getStockQuantity() == 0)
                    .count() : 0;
            // [TÍNH TOÁN Ở ĐÂY] Đếm số lượng phiên bản có tồn kho <= 5
           int lowStockCount = p.getVariants() != null ? 
                (int) p.getVariants().stream()
                    .filter(v -> v.getStockQuantity() != null && v.getStockQuantity() > 0 && v.getStockQuantity() < 5)
                    .count() : 0;

            // Sau khi tính xong mới nhét vào Builder để trả về
            return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .slug(p.getSlug())
                .displayPrice(p.getDisplayPrice())
                .originalPrice(p.getOriginalPrice())
                .thumbnailUrl(p.getThumbnailUrl())
                .brandName(p.getBrand() != null ? p.getBrand().getName() : "N/A")
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : "N/A")
                .status(p.getStatus() != null ? p.getStatus().name() : "INACTIVE")
                .totalVariants(p.getVariants() != null ? p.getVariants().size() : 0)
                .totalStock(p.getTotalStock())
                .isFeatured(p.isFeatured())
                .soldQuantity(p.getSoldQuantity())
                .createdAt(p.getCreatedAt())
                .outOfStockVariantCount(outOfStockCount) // Đưa biến vừa tính vào đây
                .lowStockVariantCount(lowStockCount) // Đưa biến vừa tính vào đây
                .build();
        });
    }

    // =========================================================
    // 2. API CHUYỂN ĐỔI TRẠNG THÁI ACTIVE / INACTIVE
    // =========================================================
    @Transactional
    public void toggleProductStatus(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));
        
        if (product.getStatus() == ProductStatus.ACTIVE) {
            product.setStatus(ProductStatus.INACTIVE);
        } else {
            product.setStatus(ProductStatus.ACTIVE);
        }
        productRepository.save(product);
    }


    // =========================================================
    // 4. API LẤY CHI TIẾT SẢN PHẨM (DÙNG CHO FORM EDIT TRÊN REACT)
    // =========================================================
    @Transactional(readOnly = true)
    public ProductRequest getProductByIdForEdit(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        ProductRequest req = new ProductRequest();
        req.setId(p.getId());
        req.setName(p.getName());
        req.setSlug(p.getSlug());
        req.setProductType(p.getProductType());
        req.setCategoryId(p.getCategory() != null ? p.getCategory().getId() : null);
        req.setBrandId(p.getBrand() != null ? p.getBrand().getId() : null);
        req.setDisplayPrice(p.getDisplayPrice());
        req.setOriginalPrice(p.getOriginalPrice());
        req.setStatus(p.getStatus());
        req.setDescription(p.getDescription());
        req.setThumbnailUrl(p.getThumbnailUrl());
        req.setSpecificationsJson(p.getSpecificationsJson()); // <--- Bổ sung dòng này
        
        // --- [BỔ SUNG] Map các trường mới cho Form Edit ---
        req.setInstallmentText(p.getInstallmentText());

        // --- [BỔ SUNG] Map các trường mới cho Form Edit ---
        req.setInstallmentText(p.getInstallmentText());
        req.setHighlightFeatures(p.getHighlightFeatures());
        req.setSpecialFeatures(p.getSpecialFeatures());
        req.setOsType(p.getOsType());
        req.setScreenSize(p.getScreenSize());
        req.setScreenResolutionType(p.getScreenResolutionType());
        req.setRefreshRate(p.getRefreshRate());
        req.setBatteryCapacity(p.getBatteryCapacity());
        req.setSupport5g(p.getSupport5g());

        // Map Images
        if (p.getImages() != null && !p.getImages().isEmpty()) {
            req.setImages(p.getImages().stream().map(img -> {
                ProductRequest.ImageDTO dto = new ProductRequest.ImageDTO();
                dto.setId(img.getId());
                dto.setImageUrl(img.getImageUrl());
                dto.setSortOrder(img.getSortOrder());
                return dto;
            }).collect(Collectors.toList()));
        }

        // Map Variants
        if (p.getVariants() != null && !p.getVariants().isEmpty()) {
            req.setVariants(p.getVariants().stream().map(v -> {
                ProductRequest.VariantDTO dto = new ProductRequest.VariantDTO();
                dto.setId(v.getId());
                dto.setSku(v.getSku());
                dto.setColorName(v.getColorName());
                dto.setColorHex(v.getColorHex());
                dto.setPrice(v.getPrice());
                dto.setRam(v.getRam());
                dto.setRom(v.getRom());
                dto.setStockQuantity(v.getStockQuantity());
                dto.setIsActive(v.isActive());
                dto.setImageUrl(v.getImageUrl()); // [BỔ SUNG]
                return dto;
            }).collect(Collectors.toList()));
        }

        // Map Highlight Specs
        if (p.getHighlightSpecs() != null && !p.getHighlightSpecs().isEmpty()) {
            req.setHighlightSpecs(p.getHighlightSpecs().stream().map(h -> {
                ProductRequest.HighlightSpecDTO dto = new ProductRequest.HighlightSpecDTO();
                dto.setId(h.getId());
                dto.setLabel(h.getLabel());
                dto.setValue(h.getValue());
                dto.setIconUrl(h.getIconUrl());
                return dto;
            }).collect(Collectors.toList()));
        }

        // Map Spec Values (EAV)
        if (p.getSpecValues() != null && !p.getSpecValues().isEmpty()) {
            req.setSpecValues(p.getSpecValues().stream().map(sv -> {
                ProductRequest.SpecValueDTO dto = new ProductRequest.SpecValueDTO();
                dto.setAttributeId(sv.getAttribute().getId());
                dto.setValue(sv.getValue());
                return dto;
            }).collect(Collectors.toList()));
        }
        
        return req;
    }

    // =========================================================
    // 5. TẠO MỚI SẢN PHẨM (CREATE)
    // =========================================================
    @Transactional(rollbackFor = Exception.class)
    public Product createProduct(ProductRequest request, 
                                 MultipartFile thumbnailFile, 
                                 List<MultipartFile> galleryFiles) throws IOException {
        Product product = new Product();
        product.setImages(new ArrayList<>());
        product.setVariants(new ArrayList<>());
        product.setHighlightSpecs(new ArrayList<>());
        product.setSpecValues(new ArrayList<>());

        mapBasicProductInfo(product, request, thumbnailFile);

        // Xử lý list ảnh (Dạng link URL text)
        int currentSortOrder = 1;
        if (request.getImages() != null) {
            for (ProductRequest.ImageDTO imgDto : request.getImages()) {
                if (imgDto.getImageUrl() != null && !imgDto.getImageUrl().isEmpty()) {
                    ProductImage img = new ProductImage();
                    img.setImageUrl(imgDto.getImageUrl());
                    img.setSortOrder(imgDto.getSortOrder() != null ? imgDto.getSortOrder() : currentSortOrder++);
                    img.setProduct(product);
                    product.getImages().add(img);
                }
            }
        }

       // Xử lý upload ảnh mới từ file (ĐÃ NÂNG CẤP ĐA LUỒNG - PARALLEL STREAM)
        if (galleryFiles != null && !galleryFiles.isEmpty()) {
            // Bước 1: Bắn đồng loạt các file lên Cloudinary cùng 1 lúc
            List<String> uploadedUrls = galleryFiles.parallelStream()
                    .filter(file -> !file.isEmpty())
                    .map(file -> {
                        try {
                            return cloudinaryService.uploadFile(file);
                        } catch (IOException e) {
                            throw new RuntimeException("Lỗi trong quá trình upload ảnh đa luồng: " + e.getMessage());
                        }
                    })
                    .collect(Collectors.toList());

            // Bước 2: Chờ tất cả trả link về xong thì mới add vào Entity (giữ nguyên thứ tự)
            for (String url : uploadedUrls) {
                ProductImage img = new ProductImage();
                img.setImageUrl(url);
                img.setSortOrder(currentSortOrder++);
                img.setProduct(product);
                product.getImages().add(img);
            }
        }

        // Xử lý Variants
        if (request.getVariants() != null) {
            for (ProductRequest.VariantDTO varDto : request.getVariants()) {
                ProductVariant variant = new ProductVariant();
                mapVariantInfo(variant, varDto, request.getDisplayPrice());
                variant.setProduct(product);
                product.getVariants().add(variant);
            }
        }

        // Xử lý Highlight Specs
        if (request.getHighlightSpecs() != null) {
            for (ProductRequest.HighlightSpecDTO specDto : request.getHighlightSpecs()) {
                ProductHighlightSpec spec = new ProductHighlightSpec();
                mapHighlightSpecInfo(spec, specDto);
                spec.setProduct(product);
                product.getHighlightSpecs().add(spec);
            }
        }

        // Xử lý Spec Values (EAV)
        if (request.getSpecValues() != null) {
            for (ProductRequest.SpecValueDTO specValDto : request.getSpecValues()) {
                ProductSpecValue specVal = new ProductSpecValue();
                specVal.setValue(specValDto.getValue());
                specVal.setAttribute(specAttributeRepository.findById(specValDto.getAttributeId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy thuộc tính: " + specValDto.getAttributeId())));
                specVal.setProduct(product);
                product.getSpecValues().add(specVal);
            }
        }

        product.setSearchKeywords(productService.generateSearchKeywords(product));
        int calculatedTotalStock = product.getVariants().stream()
                .mapToInt(v -> v.getStockQuantity() != null ? v.getStockQuantity() : 0)
                .sum();
        product.setTotalStock(calculatedTotalStock);
        return productRepository.save(product);
    }

    // =========================================================
    // 6. CẬP NHẬT SẢN PHẨM (UPDATE - GIỮ NGUYÊN ID BẢNG CON)
    // =========================================================
    @Transactional(rollbackFor = Exception.class)
    public Product updateProduct(Long id, ProductRequest request, 
                                 MultipartFile thumbnailFile, 
                                 List<MultipartFile> galleryFiles) throws IOException {
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm để cập nhật"));

        mapBasicProductInfo(product, request, thumbnailFile);

        // --- 1. UPDATE VARIANTS ---
        if (request.getVariants() != null) {
            // Lấy danh sách ID Variant gửi lên (những cái giữ lại)
            List<Long> requestedVariantIds = request.getVariants().stream()
                    .map(ProductRequest.VariantDTO::getId)
                    .filter(vId -> vId != null)
                    .collect(Collectors.toList());

            // Xóa các Variant có trong DB nhưng không có trong Request (bị Admin xóa trên UI)
            product.getVariants().removeIf(existingVar -> !requestedVariantIds.contains(existingVar.getId()));

            // Thêm mới hoặc Cập nhật
            for (ProductRequest.VariantDTO varDto : request.getVariants()) {
                if (varDto.getId() != null) {
                    // Update
                    ProductVariant existingVar = product.getVariants().stream()
                            .filter(v -> v.getId().equals(varDto.getId()))
                            .findFirst().orElse(null);
                    if (existingVar != null) {
                        mapVariantInfo(existingVar, varDto, request.getDisplayPrice());
                    }
                } else {
                    // Thêm mới (Insert)
                    ProductVariant newVariant = new ProductVariant();
                    mapVariantInfo(newVariant, varDto, request.getDisplayPrice());
                    newVariant.setProduct(product);
                    product.getVariants().add(newVariant);
                }
            }
        } else {
            product.getVariants().clear(); // Nếu mảng rỗng thì xóa hết
        }

        // --- 2. UPDATE IMAGES ---
        int currentSortOrder = 1;
        if (request.getImages() != null) {
            List<Long> requestedImageIds = request.getImages().stream()
                    .map(ProductRequest.ImageDTO::getId)
                    .filter(imgId -> imgId != null)
                    .collect(Collectors.toList());

            product.getImages().removeIf(existingImg -> !requestedImageIds.contains(existingImg.getId()));

            for (ProductRequest.ImageDTO imgDto : request.getImages()) {
                if (imgDto.getId() != null) {
                    ProductImage existingImg = product.getImages().stream()
                            .filter(img -> img.getId().equals(imgDto.getId()))
                            .findFirst().orElse(null);
                    if (existingImg != null) {
                        existingImg.setImageUrl(imgDto.getImageUrl());
                        existingImg.setSortOrder(imgDto.getSortOrder() != null ? imgDto.getSortOrder() : currentSortOrder++);
                    }
                } else {
                    if (imgDto.getImageUrl() != null && !imgDto.getImageUrl().isEmpty()) {
                        ProductImage newImg = new ProductImage();
                        newImg.setImageUrl(imgDto.getImageUrl());
                        newImg.setSortOrder(imgDto.getSortOrder() != null ? imgDto.getSortOrder() : currentSortOrder++);
                        newImg.setProduct(product);
                        product.getImages().add(newImg);
                    }
                }
            }
        } else {
            product.getImages().clear();
        }

       // Xử lý upload ảnh mới từ file (ĐÃ NÂNG CẤP ĐA LUỒNG - PARALLEL STREAM)
        if (galleryFiles != null && !galleryFiles.isEmpty()) {
            // Bước 1: Bắn đồng loạt các file lên Cloudinary cùng 1 lúc
            List<String> uploadedUrls = galleryFiles.parallelStream()
                    .filter(file -> !file.isEmpty())
                    .map(file -> {
                        try {
                            return cloudinaryService.uploadFile(file);
                        } catch (IOException e) {
                            throw new RuntimeException("Lỗi trong quá trình upload ảnh đa luồng: " + e.getMessage());
                        }
                    })
                    .collect(Collectors.toList());

            // Bước 2: Chờ tất cả trả link về xong thì mới add vào Entity (giữ nguyên thứ tự)
            for (String url : uploadedUrls) {
                ProductImage img = new ProductImage();
                img.setImageUrl(url);
                img.setSortOrder(currentSortOrder++);
                img.setProduct(product);
                product.getImages().add(img);
            }
        }

        // --- 3. UPDATE HIGHLIGHT SPECS ---
        if (request.getHighlightSpecs() != null) {
            List<Long> requestedSpecIds = request.getHighlightSpecs().stream()
                    .map(ProductRequest.HighlightSpecDTO::getId)
                    .filter(specId -> specId != null)
                    .collect(Collectors.toList());

            product.getHighlightSpecs().removeIf(existingSpec -> !requestedSpecIds.contains(existingSpec.getId()));

            for (ProductRequest.HighlightSpecDTO specDto : request.getHighlightSpecs()) {
                if (specDto.getId() != null) {
                    ProductHighlightSpec existingSpec = product.getHighlightSpecs().stream()
                            .filter(s -> s.getId().equals(specDto.getId()))
                            .findFirst().orElse(null);
                    if (existingSpec != null) {
                        mapHighlightSpecInfo(existingSpec, specDto);
                    }
                } else {
                    ProductHighlightSpec newSpec = new ProductHighlightSpec();
                    mapHighlightSpecInfo(newSpec, specDto);
                    newSpec.setProduct(product);
                    product.getHighlightSpecs().add(newSpec);
                }
            }
        } else {
            product.getHighlightSpecs().clear();
        }

        // --- 4. UPDATE SPEC VALUES (EAV) ---
        // Với bảng Spec Values, vì nó mapping theo AttributeId, ta update dựa trên AttributeId thay vì ID của bản ghi
        if (request.getSpecValues() != null) {
            List<Long> requestedAttrIds = request.getSpecValues().stream()
                    .map(ProductRequest.SpecValueDTO::getAttributeId)
                    .collect(Collectors.toList());

            product.getSpecValues().removeIf(existingVal -> !requestedAttrIds.contains(existingVal.getAttribute().getId()));

            for (ProductRequest.SpecValueDTO specValDto : request.getSpecValues()) {
                ProductSpecValue existingVal = product.getSpecValues().stream()
                        .filter(v -> v.getAttribute().getId().equals(specValDto.getAttributeId()))
                        .findFirst().orElse(null);

                if (existingVal != null) {
                    existingVal.setValue(specValDto.getValue()); // Update giá trị
                } else {
                    ProductSpecValue newVal = new ProductSpecValue();
                    newVal.setValue(specValDto.getValue());
                    newVal.setAttribute(specAttributeRepository.findById(specValDto.getAttributeId())
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy thuộc tính: " + specValDto.getAttributeId())));
                    newVal.setProduct(product);
                    product.getSpecValues().add(newVal);
                }
            }
        } else {
            product.getSpecValues().clear();
        }

        product.setSearchKeywords(productService.generateSearchKeywords(product));
        int calculatedTotalStock = product.getVariants().stream()
                .mapToInt(v -> v.getStockQuantity() != null ? v.getStockQuantity() : 0)
                .sum();
        product.setTotalStock(calculatedTotalStock);
        return productRepository.save(product); // Gọi 1 lần save() là Hibernate sẽ lo toàn bộ Update/Insert/Delete phía dưới
    }

   private void mapBasicProductInfo(Product product, ProductRequest request, MultipartFile thumbnailFile) throws IOException {
        product.setName(request.getName());
        product.setSlug(request.getSlug());
        product.setProductType(request.getProductType());
        product.setDisplayPrice(request.getDisplayPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setStatus(request.getStatus() != null ? request.getStatus() : ProductStatus.ACTIVE);
        product.setDescription(request.getDescription());
        product.setSpecificationsJson(request.getSpecificationsJson());

        // --- [BỔ SUNG] Lưu các trường mới vào Database ---
        product.setInstallmentText(request.getInstallmentText());
        // --- [BỔ SUNG] Lưu các trường mới vào Database ---
        product.setInstallmentText(request.getInstallmentText());
        product.setHighlightFeatures(request.getHighlightFeatures());
        product.setSpecialFeatures(request.getSpecialFeatures());
        product.setOsType(request.getOsType());
        product.setScreenSize(request.getScreenSize());
        product.setScreenResolutionType(request.getScreenResolutionType());
        product.setRefreshRate(request.getRefreshRate());
        product.setBatteryCapacity(request.getBatteryCapacity());
        product.setSupport5g(request.getSupport5g() != null ? request.getSupport5g() : false); // Tránh null pointer

        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            product.setThumbnailUrl(cloudinaryService.uploadFile(thumbnailFile));
        } else if (request.getThumbnailUrl() != null) {
            product.setThumbnailUrl(request.getThumbnailUrl());
        }

        if (request.getCategoryId() != null) {
            product.setCategory(categoryRepository.findById(request.getCategoryId()).orElse(null));
        }
        if (request.getBrandId() != null) {
            product.setBrand(brandRepository.findById(request.getBrandId()).orElse(null));
        }
    }

    private void mapVariantInfo(ProductVariant variant, ProductRequest.VariantDTO dto, java.math.BigDecimal defaultPrice) {
        variant.setSku(dto.getSku());
        variant.setColorName(dto.getColorName());
        variant.setColorHex(dto.getColorHex());
        variant.setPrice(dto.getPrice() != null ? dto.getPrice() : defaultPrice);
        variant.setRam(dto.getRam());
        variant.setRom(dto.getRom());
        variant.setStockQuantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : 0);
        variant.setActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        variant.setImageUrl(dto.getImageUrl()); // [BỔ SUNG] Lưu URL ảnh của biến thể
    }
    private void mapHighlightSpecInfo(ProductHighlightSpec spec, ProductRequest.HighlightSpecDTO dto) {
        spec.setLabel(dto.getLabel());
        spec.setValue(dto.getValue());
        spec.setIconUrl(dto.getIconUrl());
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));
        
        // Cạm bẫy: Đổi tên slug để tránh lỗi Unique khi tạo lại sản phẩm trùng tên
        String timestamp = String.valueOf(System.currentTimeMillis());
        product.setSlug(product.getSlug() + "-deleted-" + timestamp);
        
        // Đổi luôn SKU của các biến thể để không bị trùng SKU
        if (product.getVariants() != null) {
            for (ProductVariant variant : product.getVariants()) {
                if (variant.getSku() != null) {
                    variant.setSku(variant.getSku() + "-del-" + timestamp);
                }
            }
        }

        // Gọi lệnh xóa (Hibernate sẽ tự động kích hoạt @SQLDelete để UPDATE is_deleted = true)
        productRepository.delete(product); 
    }


    public String uploadSingleImage(MultipartFile file) throws IOException {
        return cloudinaryService.uploadFile(file);
    }


    // =========================================================
    // API BẬT/TẮT SẢN PHẨM NỔI BẬT (GHIM TRANG CHỦ)
    // =========================================================
    @Transactional
    public void toggleProductFeatured(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));
        
        // Logic: Nếu đang muốn BẬT ghim, kiểm tra xem đã quá 10 cái chưa
        if (!product.isFeatured()) {
            long featuredCount = productRepository.countByIsFeaturedTrue();
            if (featuredCount >= 10) {
                throw new RuntimeException("Chỉ được ghim tối đa 10 sản phẩm lên trang chủ!");
            }
        }

        product.setFeatured(!product.isFeatured());
        productRepository.save(product);
    }

    public List<ProductVariantResponse> getProductVariants(Long id) {
       
        // Viết logic trực tiếp ở đây hoặc chuyển vào Service (ở đây mình viết gọn để bạn dễ copy)
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
            
            List<ProductVariantResponse> variants = product.getVariants().stream()
                .map(v -> ProductVariantResponse.builder()
                    .id(v.getId())
                    .sku(v.getSku())
                    .colorName(v.getColorName())
                    .colorHex(v.getColorHex())
                    .ram(v.getRam())
                    .rom(v.getRom())
                    .price(v.getPrice())
                    .stockQuantity(v.getStockQuantity())
                    .imageUrl(v.getImageUrl())
                    .build())
                .collect(Collectors.toList());
                return variants;
    }

    @Transactional
    public void updateBulkVariantStock(BulkStockUpdateRequest request) {
        // 1. Lặp qua danh sách và cập nhật từng biến thể
        for (BulkStockUpdateRequest.StockItem item : request.getStocks()) {
            ProductVariant variant = productVariantRepository.findById(item.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Biến thể không tồn tại"));
            variant.setStockQuantity(item.getStockQuantity());
            productVariantRepository.save(variant);
        }

        // 2. Tính lại Tổng tồn kho cho Sản phẩm cha (Chỉ cần làm 1 lần)
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
                
        int newTotalStock = product.getVariants().stream()
                .mapToInt(v -> v.getStockQuantity() != null ? v.getStockQuantity() : 0)
                .sum();
        
        product.setTotalStock(newTotalStock);
        productRepository.save(product);
    }

    
    
}