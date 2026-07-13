package com.dinhluong.dlmstore.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import com.dinhluong.dlmstore.convert.ComboItemListConverter;
import com.dinhluong.dlmstore.dto.responses.ComboItemDetail;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "order_id")
    private Long orderId;
    @Column(name = "product_variant_id")
    private Long productVariantId;
    private Integer quantity;
    @Column(name = "product_name")
    private String productName; // Lưu tên sản phẩm lúc mua

    @Column(name = "product_image")
    private String productImage; // Lưu ảnh sản phẩm lúc mua
    @Column(name = "slug")
    private String slug;
    @Column(name = "variant_name")
    private String variantName;
    @Column(name = "price_at_purchase", precision = 15, scale = 2)
    private BigDecimal priceAtPurchase;
    @Column(name = "combo_items", columnDefinition = "TEXT")
    @Convert(converter = ComboItemListConverter.class)
    private List<ComboItemDetail> comboItems;
}