package com.dinhluong.dlmstore.config;

public class AppConstants {
    public static final String[] USER_APIS = {
            "/api/users/**"
    };
    public static final String[] ADMIN_APIS = {
            "/api/admin/**"
    };

    public static final String[] PUBLIC_APIS = {
            "/api/auth/**",
            "/api/public/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/oauth2/**",
            "/login/oauth2/**",
            "/ws/**"
    };
}
