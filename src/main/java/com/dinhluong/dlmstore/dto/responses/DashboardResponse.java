package com.dinhluong.dlmstore.dto.responses;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DashboardResponse {
    private Overview overview;
    private List<RevenueTrend> revenueTrends;
    private List<PaymentMethodStat> paymentMethods;
    private List<TopProduct> topProducts;
    private List<LowStockVariant> lowStockVariants;
    private List<TopBrand> topBrands;
    private List<ActiveVoucher> activeVouchers;
    private SupportStats supportStats;

    @Data
    @Builder
    public static class Overview {
        private BigDecimal totalRevenue;
        private Long completedOrders;
        private Long newUsers;
        private Long pendingTasks;
    }

    @Data
    @Builder
    public static class RevenueTrend {
        private String date;
        private BigDecimal revenue;
        private Long orders;
    }

    @Data
    @Builder
    public static class PaymentMethodStat {
        private String name;
        private Long value;
        private String color;
    }

    @Data
    @Builder
    public static class TopProduct {
        private Long id;
        private String name;
        private String variant;
        private Long sold;
        private BigDecimal revenue;
        private String image;
    }

    @Data
    @Builder
    public static class LowStockVariant {
        private String sku;
        private String name;
        private String variant;
        private Integer stock;
        private String image;
    }

    @Data
    @Builder
    public static class TopBrand {
        private String name;
        private BigDecimal revenue;
        private String fill;
    }

    @Data
    @Builder
    public static class ActiveVoucher {
        private String code;
        private Integer used;
        private Integer limit;
        private String expiry;
    }

    @Data
    @Builder
    public static class SupportStats {
        private Long chatbotHandled;
        private Long humanHandled;
        private Double avgRating;
    }
}