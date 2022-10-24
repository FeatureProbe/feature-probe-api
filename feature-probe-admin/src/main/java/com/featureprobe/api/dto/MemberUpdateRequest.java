package com.featureprobe.api.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class MemberUpdateRequest {

    @NotBlank
    private String account;

    @NotBlank
    private String password;

}
