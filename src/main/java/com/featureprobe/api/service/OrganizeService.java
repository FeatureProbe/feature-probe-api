package com.featureprobe.api.service;

import com.featureprobe.api.base.enums.ResourceType;
import com.featureprobe.api.base.exception.ResourceNotFoundException;
import com.featureprobe.api.dto.UserOrganize;
import com.featureprobe.api.entity.Organize;
import com.featureprobe.api.entity.OrganizeUser;
import com.featureprobe.api.repository.OrganizeRepository;
import com.featureprobe.api.repository.OrganizeUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@AllArgsConstructor
@Service
public class OrganizeService {

    OrganizeRepository organizeRepository;

    OrganizeUserRepository organizeUserRepository;

    @Transactional
    public UserOrganize queryUserOrganize(Long organizeId, Long userId) {
        OrganizeUser organizeUser = organizeUserRepository.findByOrganizeIdAndUserId(organizeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.ORGANIZE_USER,
                        organizeId + "_" + userId));
        Organize organize = organizeRepository.getById(organizeId);
        return new UserOrganize(organizeId, organize.getName(), organizeUser.getRole());
    }

}
