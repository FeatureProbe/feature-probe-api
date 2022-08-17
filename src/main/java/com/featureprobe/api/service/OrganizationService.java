package com.featureprobe.api.service;

import com.featureprobe.api.base.enums.ResourceType;
import com.featureprobe.api.base.exception.ResourceNotFoundException;
import com.featureprobe.api.dto.OrganizationMember;
import com.featureprobe.api.entity.Organization;
import com.featureprobe.api.repository.OrganizationRepository;
import com.featureprobe.api.repository.OrganizationMemberRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class OrganizationService {

    OrganizationRepository organizationRepository;

    OrganizationMemberRepository organizationMemberRepository;

    @Transactional
    public OrganizationMember queryOrganizationMember(Long organizationId, Long memberId) {
        com.featureprobe.api.entity.OrganizationMember organizationMember =
                organizationMemberRepository.findByOrganizationIdAndMemberId(organizationId, memberId)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.ORGANIZATION_MEMBER,
                        organizationId + "_" + memberId));
        Organization organization = organizationRepository.getById(organizationId);
        return new OrganizationMember(organizationId, organization.getName(), organizationMember.getRole());
    }

}
