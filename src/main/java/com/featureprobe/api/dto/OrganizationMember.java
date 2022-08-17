package com.featureprobe.api.dto;
import com.featureprobe.api.base.enums.OrganizationRoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrganizationMember {

    private Long organizationId;

    private String organizationName;

    private OrganizationRoleEnum role;

    public String getRoleName() {
        return role.name();
    }
}
