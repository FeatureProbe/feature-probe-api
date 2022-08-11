package com.featureprobe.api.dto;
import com.featureprobe.api.base.enums.OrganizeRoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserOrganize {

    private Long organizeId;

    private String organizeName;

    private OrganizeRoleEnum role;

}
