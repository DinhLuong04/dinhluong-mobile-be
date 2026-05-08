package com.dinhluong.dlmstore.controller.Admin;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminViewController {

    @GetMapping("/admin/login")
    public String adminLoginPage() {
        // Trả về file admin-login.html trong thư mục src/main/resources/templates/
        return "admin-login"; 
    }
}
