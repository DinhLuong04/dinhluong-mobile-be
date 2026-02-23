package com.dinhluong.dlmstore.controller;

import com.dinhluong.dlmstore.dto.ApiResponse;
import com.dinhluong.dlmstore.entity.Address;
import com.dinhluong.dlmstore.service.AddressService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.dinhluong.dlmstore.security.CustomUserPrincipal; 
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Address>>> getMyAddresses(
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {
        
        List<Address> addresses = addressService.getUserAddresses(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách địa chỉ thành công", addresses));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Address>> addAddress(
            @AuthenticationPrincipal CustomUserPrincipal currentUser, 
            @RequestBody Address address) {
        
        Address savedAddress = addressService.addAddress(currentUser.getId(), address);
        return ResponseEntity.ok(ApiResponse.success("Thêm địa chỉ thành công", savedAddress));
    }

    @PutMapping("/{id}/default")
    public ResponseEntity<ApiResponse<String>> setDefaultAddress(
            @AuthenticationPrincipal CustomUserPrincipal currentUser, 
            @PathVariable Long id) {
        
        addressService.setDefaultAddress(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật địa chỉ mặc định thành công", null));
    }
}