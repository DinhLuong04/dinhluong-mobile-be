package com.dinhluong.dlmstore.service;

import com.dinhluong.dlmstore.dto.requests.SpecRequest;
import com.dinhluong.dlmstore.dto.responses.SpecAttributeResponse;
import com.dinhluong.dlmstore.dto.responses.SpecGroupResponse;
import com.dinhluong.dlmstore.entity.SpecAttribute;
import com.dinhluong.dlmstore.entity.SpecGroup;
import com.dinhluong.dlmstore.repository.SpecAttributeRepository;
import com.dinhluong.dlmstore.repository.SpecGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpecMasterService {

    private final SpecGroupRepository specGroupRepository;
    private final SpecAttributeRepository specAttributeRepository;

    // ==========================================
    // 1. SPEC GROUP (NHÓM THÔNG SỐ)
    // ==========================================

    // Lấy toàn bộ Group (Kèm theo danh sách Attribute bên trong)
    public List<SpecGroupResponse> getAllSpecGroups() {
        return specGroupRepository.findAllByOrderBySortOrderAsc().stream()
                .map(this::mapToGroupResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SpecGroupResponse saveSpecGroup(Long id, SpecRequest request) {
        SpecGroup group = (id != null)
                ? specGroupRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy Nhóm thông số"))
                : new SpecGroup();

        group.setName(request.getName());
        group.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 1);

        SpecGroup savedGroup = specGroupRepository.save(group);
        return mapToGroupResponse(savedGroup);
    }

    @Transactional
    public void deleteSpecGroup(Long id) {
        specGroupRepository.deleteById(id);
    }

    // ==========================================
    // 2. SPEC ATTRIBUTE (THUỘC TÍNH)
    // ==========================================

    @Transactional
    public SpecAttributeResponse saveSpecAttribute(Long id, SpecRequest request) {
        SpecAttribute attribute = (id != null)
                ? specAttributeRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy Thuộc tính"))
                : new SpecAttribute();

        attribute.setName(request.getName());
        attribute.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 1);
        attribute.setDataType(request.getDataType() != null ? request.getDataType() : "TEXT");

        // Set quan hệ với Group
        if (request.getGroupId() != null) {
            SpecGroup group = specGroupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Nhóm thông số cha"));
            attribute.setGroup(group);
        } else if (id == null) {
            throw new RuntimeException("Phải chọn Nhóm thông số (Group ID) khi tạo Thuộc tính mới");
        }

        SpecAttribute savedAttribute = specAttributeRepository.save(attribute);
        return mapToAttributeResponse(savedAttribute);
    }

    @Transactional
    public void deleteSpecAttribute(Long id) {
        specAttributeRepository.deleteById(id);
    }

    // ==========================================
    // MAPPING FUNCTIONS
    // ==========================================

    private SpecGroupResponse mapToGroupResponse(SpecGroup group) {
        List<SpecAttributeResponse> attributeResponses = null;
        if (group.getAttributes() != null && !group.getAttributes().isEmpty()) {
            attributeResponses = group.getAttributes().stream()
                    .map(this::mapToAttributeResponse)
                    .collect(Collectors.toList());
        }

        return SpecGroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .sortOrder(group.getSortOrder())
                .attributes(attributeResponses)
                .build();
    }

    private SpecAttributeResponse mapToAttributeResponse(SpecAttribute attribute) {
        return SpecAttributeResponse.builder()
                .id(attribute.getId())
                .name(attribute.getName())
                .sortOrder(attribute.getSortOrder())
                .dataType(attribute.getDataType())
                .groupId(attribute.getGroup() != null ? attribute.getGroup().getId() : null)
                .groupName(attribute.getGroup() != null ? attribute.getGroup().getName() : null)
                .build();
    }
}