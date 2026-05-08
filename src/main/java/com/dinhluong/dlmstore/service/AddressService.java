package com.dinhluong.dlmstore.service;

import com.dinhluong.dlmstore.entity.Address;
import com.dinhluong.dlmstore.entity.Users;
import com.dinhluong.dlmstore.repository.AddressRepository;
import com.dinhluong.dlmstore.repository.UserRepository; // Giả định bạn đã có repository này
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    // Lấy danh sách địa chỉ của user
    public List<Address> getUserAddresses(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    // Thêm địa chỉ mới
    @Transactional
    public Address addAddress(Long userId, Address newAddress) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

        // Nếu đây là địa chỉ đầu tiên hoặc được tick chọn làm mặc định
        if (newAddress.getIsDefault() != null && newAddress.getIsDefault()) {
            addressRepository.resetDefaultAddressForUser(userId);
        } else {
            // Nếu chưa có isDefault thì mặc định gán là false
            newAddress.setIsDefault(false);
        }

        newAddress.setUser(user);
        return addressRepository.save(newAddress);
    }

    // Đặt địa chỉ làm mặc định
    @Transactional
    public void setDefaultAddress(Long userId, Long addressId) {
        // Reset toàn bộ địa chỉ của user này về false
        addressRepository.resetDefaultAddressForUser(userId);

        // Tìm địa chỉ cần set và đổi thành true
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ với ID: " + addressId));

        // Kiểm tra xem địa chỉ này có đúng là của user đang yêu cầu không (Bảo mật)
        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền thay đổi địa chỉ này");
        }

        address.setIsDefault(true);
        addressRepository.save(address);
    }

    @Transactional
    public Address updateAddress(Long userId, Long addressId, Address addressDetails) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ với ID: " + addressId));

        // Kiểm tra bảo mật: Chỉ cho phép sửa địa chỉ của chính mình
        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền cập nhật địa chỉ này");
        }

        // Cập nhật các trường thông tin
        address.setStreet(addressDetails.getStreet());
        address.setCity(addressDetails.getCity());
        address.setProvince(addressDetails.getProvince());
        address.setCountry(addressDetails.getCountry());

        // Xử lý nếu người dùng tick chọn địa chỉ này làm mặc định trong lúc cập nhật
        if (addressDetails.getIsDefault() != null && addressDetails.getIsDefault()) {
            addressRepository.resetDefaultAddressForUser(userId);
            address.setIsDefault(true);
        } else if (addressDetails.getIsDefault() != null) {
            address.setIsDefault(addressDetails.getIsDefault());
        }

        return addressRepository.save(address);
    }

    // Xóa địa chỉ
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ với ID: " + addressId));

        // Kiểm tra bảo mật
        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xóa địa chỉ này");
        }

        addressRepository.delete(address);
    }
}