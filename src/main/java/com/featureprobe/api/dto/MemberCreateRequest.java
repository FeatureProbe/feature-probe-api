package com.featureprobe.api.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class MemberCreateRequest {

    @NotNull
    private List<String> accounts;

    @NotBlank
    private String password;

}
