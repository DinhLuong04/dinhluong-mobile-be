package com.dinhluong.dlmstore.service;

import com.dinhluong.dlmstore.dto.responses.ProductCardResponse;

import com.dinhluong.dlmstore.dto.responses.ProductDetailResponse;
import com.dinhluong.dlmstore.entity.Product;
import com.dinhluong.dlmstore.entity.ProductVariant;
import com.dinhluong.dlmstore.entity.Enums.OsType;
import com.dinhluong.dlmstore.entity.Enums.ProductStatus;
import com.dinhluong.dlmstore.mapper.ProductMapper;
import com.dinhluong.dlmstore.repository.ProductRepository;
import com.dinhluong.dlmstore.utils.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {


    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public Page<ProductCardResponse> getAllProducts(
            String categorySlug,
            List<String> brands,
            List<String> osTypes,
            List<String> roms,
            List<String> rams,
            List<String> networks,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer minBattery, Integer maxBattery,
            Double minScreenSize, Double maxScreenSize,
            Integer minRefreshRate, Integer maxRefreshRate,
            String search,
            Pageable pageable) {

        Specification<Product> spec = Specification.where(null);
        if (StringUtils.hasText(categorySlug)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("slug"), categorySlug));
        }
        if (StringUtils.hasText(search)) {
            String keyword = search.trim().toLowerCase();
            spec = spec.and((root, query, cb) -> {
                var predicateName = cb.like(cb.lower(root.get("name")), "%" + keyword + "%");
                var predicateKeywords = cb.like(cb.lower(root.get("searchKeywords")), "%" + keyword + "%");
                return cb.or(predicateName, predicateKeywords);
            });
        }

        if (brands != null && !brands.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("brand").get("name").in(brands));
        }

        if (osTypes != null && !osTypes.isEmpty()) {
            List<OsType> osEnumList = osTypes.stream()
                    .map(s -> {
                        try {
                            return OsType.valueOf(s.toUpperCase());
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());

            if (!osEnumList.isEmpty()) {
                spec = spec.and((root, query, cb) -> root.get("osType").in(osEnumList));
            }
        }

        if (roms != null && !roms.isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                var predicates = roms.stream()
                        .map(rom -> cb.like(root.get("availableRoms").as(String.class), "%" + rom + "%"))
                        .toArray(jakarta.persistence.criteria.Predicate[]::new);
                return cb.or(predicates);
            });
        }

        if (rams != null && !rams.isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                var predicates = rams.stream()
                        .map(ram -> cb.like(root.get("availableRams").as(String.class), "%" + ram + "%"))
                        .toArray(jakarta.persistence.criteria.Predicate[]::new);
                return cb.or(predicates);
            });
        }

        if (networks != null && !networks.isEmpty()) {
            if (networks.contains("5G")) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("support5g"), true));
            }
        }
        if (minPrice != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("displayPrice"), minPrice));
        }
        if (maxPrice != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("displayPrice"), maxPrice));
        }
        if (minBattery != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("batteryCapacity"), minBattery));
        }
        if (maxBattery != null) { // <-- Logic mới
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("batteryCapacity"), maxBattery));
        }

        if (minScreenSize != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("screenSize"), minScreenSize));
        }
        if (maxScreenSize != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("screenSize"), maxScreenSize));
        }

        if (minRefreshRate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("refreshRate"), minRefreshRate));
        }
        if (maxRefreshRate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("refreshRate"), maxRefreshRate));
        }

        spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), ProductStatus.ACTIVE));
        spec = spec.and((root, query, cb) -> cb.equal(root.get("isDeleted"), false));

        return productRepository.findAll(spec, pageable).map(productMapper::toCardResponse);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlugAndStatusAndIsDeletedFalse(slug, ProductStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại hoặc đã ngừng kinh doanh: " + slug));
        return productMapper.toDetailResponse(product);
    }

    public List<ProductDetailResponse> getProductsBySlugs(List<String> slugs) {
        List<Product> products = productRepository.findBySlugInAndStatusAndIsDeletedFalse(slugs, ProductStatus.ACTIVE);
        return products.stream()
                .map(productMapper::toDetailResponse)
                .collect(Collectors.toList());
    }

   // TẠO TỪ KHÓA TÌM KIẾM (SEARCH KEYWORDS GENERATOR)
   public String generateSearchKeywords(Product product) {

       // Chốt chặn 1: Nhỡ truyền vào một object null thì return luôn
       if (product == null) {
           return "";
       }

       Set<String> keywords = new HashSet<>();

       // Chốt chặn 2: Xử lý an toàn cho Name
       String safeName = product.getName() != null ? product.getName() : "";
       String nameUnsigned = StringUtils.unAccent(safeName);
       if (nameUnsigned == null) {
           nameUnsigned = ""; // Đảm bảo nameUnsigned không bao giờ null để không bị lỗi .contains() ở dưới
       }

       if (StringUtils.hasText(safeName)) {
           keywords.add(safeName);
           keywords.add(nameUnsigned);
       }

       if (product.getSlug() != null) {
           keywords.add(product.getSlug().replace("-", " "));
       }

       // Chốt chặn 3: Xử lý an toàn cho Brand
       String brandName = "";
       if (product.getBrand() != null && product.getBrand().getName() != null) {
           String unAccentBrand = StringUtils.unAccent(product.getBrand().getName());
           if (unAccentBrand != null) {
               brandName = unAccentBrand.toLowerCase();
               keywords.add(brandName);
           }
       }

       // Chốt chặn 4: Xử lý an toàn cho Category
       if (product.getCategory() != null && product.getCategory().getName() != null) {
           String unAccentCat = StringUtils.unAccent(product.getCategory().getName());
           if (unAccentCat != null) {
               keywords.add(unAccentCat);
           }
       }

       // NHÓM BIẾN THỂ (MÀU SẮC, RAM, ROM)
       if (product.getVariants() != null && !product.getVariants().isEmpty()) {
           for (ProductVariant v : product.getVariants()) {
               if (StringUtils.hasText(v.getColorName())) {
                   String color = StringUtils.unAccent(v.getColorName());
                   if (color != null) {
                       keywords.add(color);
                       for (String sub : color.split(" "))
                           keywords.add(sub);
                   }
               }
               if (StringUtils.hasText(v.getRom())) {
                   String rom = v.getRom().toLowerCase().replace(" ", "");
                   keywords.add(rom);
                   keywords.add(rom.replace("gb", ""));

                   // THÊM LOGIC: BỘ NHỚ LỚN
                   if (rom.contains("512") || rom.contains("1tb")) {
                       keywords.add("bo nho lon dung luong cao luu tru nhieu");
                   }
               }
               if (StringUtils.hasText(v.getRam())) {
                   keywords.add(v.getRam().toLowerCase().replace(" ", ""));
               }
           }
       }

       // QUÉT SÂU JSON THÔNG SỐ & SPECIAL FEATURES
       StringBuilder specsBuilder = new StringBuilder();

       if (StringUtils.hasText(product.getSpecialFeatures())) {
           String safeSpecial = StringUtils.unAccent(product.getSpecialFeatures());
           if (safeSpecial != null) specsBuilder.append(safeSpecial).append(" ");
       }

       if (StringUtils.hasText(product.getHighlightFeatures())) {
           String safeHighlight = StringUtils.unAccent(product.getHighlightFeatures());
           if (safeHighlight != null) specsBuilder.append(safeHighlight).append(" ");
       }

       // Duyệt cây JSON Specifications (Phần này bạn đã làm cực kỳ an toàn rồi, giữ nguyên)
       JsonNode jsonSpec = product.getSpecificationsJson();
       if (jsonSpec != null && jsonSpec.isArray()) {
           for (JsonNode group : jsonSpec) {
               JsonNode items = group.get("items");
               if (items != null && items.isArray()) {
                   for (JsonNode item : items) {
                       if (item.has("value")) {
                           String rawVal = item.get("value").asText();
                           if (rawVal != null) {
                               String unAccentVal = StringUtils.unAccent(rawVal);
                               if (unAccentVal != null) {
                                   String val = unAccentVal.toLowerCase();

                                   // Check Chipset
                                   if (val.contains("snapdragon") || val.contains("dimensity") ||
                                           val.contains("helio") || val.contains("exynos") ||
                                           val.contains("apple a") || val.contains("bionic")) {
                                       specsBuilder.append(val).append(" ");
                                   }

                                   // Check Pin & Sạc
                                   if (val.contains("mah")) {
                                       specsBuilder.append(val).append(" ");
                                       // FIX BUG: Chỉ pin 5000mAh trở lên mới tính là pin trâu
                                       if (val.contains("5000") || val.contains("5100") || val.contains("5500") || val.contains("6000")) {
                                           keywords.add("pin trau dung lau");
                                       }
                                   }
                                   if (val.contains("sac nhanh")) {
                                       specsBuilder.append(val).append(" ");
                                       keywords.add("sac nhanh sieu toc");
                                   }

                                   // Check Màn hình
                                   if (val.contains("hz") || val.contains("oled") || val.contains("amoled")) {
                                       specsBuilder.append(val).append(" ");
                                   }

                                   // Check Camera (THÊM LOGIC CHỤP ẢNH ĐẸP)
                                   if (val.contains("mp") && !val.contains("mpeg")) {
                                       specsBuilder.append(val).append(" ");
                                       keywords.add("chup anh dep camera ngon quay phim vlog selfie");
                                   }

                                   // Check Chống nước (THÊM LOGIC IP68)
                                   if (val.contains("ip68") || val.contains("ip67")) {
                                       specsBuilder.append(val).append(" ");
                                       keywords.add("chong nuoc di mua");
                                   }
                               }
                           }
                       }
                   }
               }
           }
       }

       String fullSpecs = specsBuilder.toString();
       if (fullSpecs.contains("snapdragon"))
           keywords.add("snapdragon chip snap");
       if (fullSpecs.contains("dimensity"))
           keywords.add("dimensity");
       if (fullSpecs.contains("apple a"))
           keywords.add("chip a");
       if (fullSpecs.contains("5g"))
           keywords.add("5g");
       if (fullSpecs.contains("120") && fullSpecs.contains("hz"))
           keywords.add("120hz");
       if (fullSpecs.contains("gap") || fullSpecs.contains("fold"))
           keywords.add("gap man hinh gap");
       if (fullSpecs.contains("gaming") || fullSpecs.contains("game"))
           keywords.add("gaming choi game");

       // LOGIC THEO HÃNG (Lúc này nameUnsigned chắc chắn không null nên không bao giờ crash)
       switch (brandName) {
           case "iphone":
           case "apple":
               keywords.add("ip tao nha tao");
               keywords.add(nameUnsigned.replace("iphone", "ip"));
               if (nameUnsigned.contains("pro max"))
                   keywords.add("prm prom promax");
               if (nameUnsigned.contains("plus"))
                   keywords.add("plu");
               break;

           case "samsung":
               keywords.add("ss sam galaxu");
               if (nameUnsigned.contains("ultra"))
                   keywords.add("s25u s24u s23u ultra u");
               if (nameUnsigned.contains("fold"))
                   keywords.add("zfold fold gap");
               if (nameUnsigned.contains("flip"))
                   keywords.add("zflip flip gap");
               if (nameUnsigned.contains("fe"))
                   keywords.add("fan edition");
               if (nameUnsigned.contains("galaxy a"))
                   keywords.add(nameUnsigned.replace("galaxy a", "a"));
               break;

           case "oppo":
               keywords.add("opo son tung");
               if (nameUnsigned.contains("reno"))
                   keywords.add("r" + nameUnsigned.replaceAll("[^0-9]", ""));
               if (nameUnsigned.contains("find"))
                   keywords.add("fin");
               break;

           case "xiaomi":
               keywords.add("mi xiao mi my");
               if (nameUnsigned.contains("redmi"))
                   keywords.add("remi redmi");
               if (nameUnsigned.contains("note"))
                   keywords.add("not");
               if (nameUnsigned.contains("poco"))
                   keywords.add("poco phone");
               break;

           case "realme":
               keywords.add("real me riu mi");
               break;
           case "vivo":
               keywords.add("bi bo");
               break;
           case "honor":
               keywords.add("honour ho no");
               break;
           case "tecno":
               keywords.add("techno tekno");
               break;

           case "redmagic":
           case "zte nubia":
               keywords.add("gaming game choi game may game nubia");
               break;

           case "nokia":
           case "mobell":
           case "masstel":
           case "itel":
           case "inoi":
           case "benco":
               if (product.getDisplayPrice() != null
                       && product.getDisplayPrice().compareTo(new BigDecimal(1500000)) < 0) {
                   keywords.add("cuc gach phim bam nguoi gia loa to");
               }
               break;

           case "viettel":
               keywords.add("vt sim song");
               break;
       }

       // PHÂN KHÚC GIÁ
       if (product.getDisplayPrice() != null) {
           double price = product.getDisplayPrice().doubleValue();
           if (price < 3000000) {
               keywords.add("gia re sinh vien phu huynh");
           } else if (price > 20000000) {
               keywords.add("cao cap flagship xin sang chanh");
           } else if (price >= 3000000 && price <= 7000000) {
               keywords.add("tam trung");
           }
       }

       return String.join(" ", keywords).toLowerCase().trim();
   }

    // BATCH JOB - CẬP NHẬT KEYWORD HÀNG LOẠT

    @Transactional
    public void updateAllProductKeywords() {
        List<Product> products = productRepository.findAll();
        int count = 0;

        System.out.println(">>> START JOB: Updating Search Keywords for " + products.size() + " products...");

        for (Product p : products) {
            try {
                String keywords = generateSearchKeywords(p);
                p.setSearchKeywords(keywords);
                count++;
            } catch (Exception e) {
                System.err.println("Error generating keyword for Product ID " + p.getId());
            }
        }

        productRepository.saveAll(products);
        System.out.println("✅ [AUTO-JOB] SUCCESS! Updated " + count + " products.");
    }

    // Lấy danh sách sản phẩm Nổi Bật (Hot Sale)
    public List<ProductCardResponse> getFeaturedProducts(int limit) {
        // Dùng Pageable để giới hạn số lượng lấy ra (VD: limit = 10)
        Pageable pageable = PageRequest.of(0, limit);
        
        List<Product> featuredProducts = productRepository.findFeaturedProducts(pageable);
        
        // Giả định bạn đã có sẵn hàm mapToProductCardResponse trong Service này
        return featuredProducts.stream()
                .map(productMapper::toCardResponse)
                .collect(Collectors.toList());
    }


    
}