package com.dinhluong.dlmstore.entity;



import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

import com.dinhluong.dlmstore.convert.ComboItemListConverter;
import com.dinhluong.dlmstore.convert.JsonNodeConverter;
import com.dinhluong.dlmstore.dto.responses.ComboItemDetail;
import com.fasterxml.jackson.databind.JsonNode;

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

    @Column(name = "price_at_purchase", precision = 15, scale = 2)
    private BigDecimal priceAtPurchase;

    @Column(name = "combo_items", columnDefinition = "TEXT")
    @Convert(converter = ComboItemListConverter.class)
    private List<ComboItemDetail> comboItems;
}