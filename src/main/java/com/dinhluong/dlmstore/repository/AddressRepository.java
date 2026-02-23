package com.dinhluong.dlmstore.repository;

import com.dinhluong.dlmstore.entity.Address;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserId(Long userId);
    
    // Query để reset tất cả địa chỉ của user về false trước khi set địa chỉ mới làm mặc định
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId")
    void resetDefaultAddressForUser(Long userId);
}

