package com.dinhluong.dlmstore.repository.projections;

import java.math.BigDecimal;

public interface DashboardProjections {
    interface TopProductProjection {
        Long getId();

        String getProductName();

        String getVariantName();

        Long getSold();

        BigDecimal getRevenue();

        String getImage();
    }

    interface TopBrandProjection {
        String getName();

        BigDecimal getRevenue();
    }

    interface PaymentMethodProjection {
        String getMethod();

        Long getMethodCount();
    }

    interface CancellationProjection {
        String getReason();
        Long getCount();
    }

    // Cho hiệu suất kinh doanh
    interface PerformanceProjection {
        Long getTotalOrders();
        Long getCompletedCount();
        Long getReturnedCount();
        BigDecimal getLostRevenue();
    }
}