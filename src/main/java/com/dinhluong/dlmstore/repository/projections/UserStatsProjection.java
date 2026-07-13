package com.dinhluong.dlmstore.repository.projections;

public interface UserStatsProjection {
    String getEmail();
    String getFullName();
    String getPhone();
    String getRoleName();
    Boolean getIsEnabled();
    Long getTotalOrders();
    Long getSuccessOrders();
    Long getCancelledOrders();
    Double getTotalSpent();
}
