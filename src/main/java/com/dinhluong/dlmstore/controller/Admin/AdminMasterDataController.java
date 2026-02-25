package com.dinhluong.dlmstore.controller.Admin;

import com.dinhluong.dlmstore.dto.requests.MasterDataRequest;
import com.dinhluong.dlmstore.dto.requests.SpecRequest;
import com.dinhluong.dlmstore.service.MasterDataService;
import com.dinhluong.dlmstore.service.SpecMasterService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminMasterDataController {

    private final MasterDataService masterDataService;

    // --- CATEGORY API ---
    @GetMapping("/categories")
    public ResponseEntity<?> getAllCategories() {
        return ResponseEntity.ok(masterDataService.getAllCategories());
    }

    @PostMapping("/categories")
    public ResponseEntity<?> createCategory(@RequestBody MasterDataRequest request) {
        return ResponseEntity.ok(masterDataService.saveCategory(null, request));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody MasterDataRequest request) {
        return ResponseEntity.ok(masterDataService.saveCategory(id, request));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        masterDataService.deleteCategory(id);
        return ResponseEntity.ok("Xóa danh mục thành công!");
    }

    // --- BRAND API ---
    @GetMapping("/brands")
    public ResponseEntity<?> getAllBrands() {
        return ResponseEntity.ok(masterDataService.getAllBrands());
    }

    @PostMapping("/brands")
    public ResponseEntity<?> createBrand(@RequestBody MasterDataRequest request) {
        return ResponseEntity.ok(masterDataService.saveBrand(null, request));
    }

    @PutMapping("/brands/{id}")
    public ResponseEntity<?> updateBrand(@PathVariable Long id, @RequestBody MasterDataRequest request) {
        return ResponseEntity.ok(masterDataService.saveBrand(id, request));
    }

    @DeleteMapping("/brands/{id}")
    public ResponseEntity<?> deleteBrand(@PathVariable Long id) {
        masterDataService.deleteBrand(id);
        return ResponseEntity.ok("Xóa thương hiệu thành công!");
    }

    @Autowired
    private SpecMasterService specMasterService;

    // --- SPEC GROUP API ---
    @GetMapping("/spec-groups")
    public ResponseEntity<?> getAllSpecGroups() {
        return ResponseEntity.ok(specMasterService.getAllSpecGroups());
    }

    @PostMapping("/spec-groups")
    public ResponseEntity<?> createSpecGroup(@RequestBody SpecRequest request) {
        return ResponseEntity.ok(specMasterService.saveSpecGroup(null, request));
    }

    @PutMapping("/spec-groups/{id}")
    public ResponseEntity<?> updateSpecGroup(@PathVariable Long id, @RequestBody SpecRequest request) {
        return ResponseEntity.ok(specMasterService.saveSpecGroup(id, request));
    }

    @DeleteMapping("/spec-groups/{id}")
    public ResponseEntity<?> deleteSpecGroup(@PathVariable Long id) {
        specMasterService.deleteSpecGroup(id);
        return ResponseEntity.ok("Xóa nhóm thông số thành công!");
    }

    // --- SPEC ATTRIBUTE API ---
    @PostMapping("/spec-attributes")
    public ResponseEntity<?> createSpecAttribute(@RequestBody SpecRequest request) {
        return ResponseEntity.ok(specMasterService.saveSpecAttribute(null, request));
    }

    @PutMapping("/spec-attributes/{id}")
    public ResponseEntity<?> updateSpecAttribute(@PathVariable Long id, @RequestBody SpecRequest request) {
        return ResponseEntity.ok(specMasterService.saveSpecAttribute(id, request));
    }

    @DeleteMapping("/spec-attributes/{id}")
    public ResponseEntity<?> deleteSpecAttribute(@PathVariable Long id) {
        specMasterService.deleteSpecAttribute(id);
        return ResponseEntity.ok("Xóa thuộc tính thành công!");
    }
}