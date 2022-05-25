package com.featureprobe.api.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ProjectCreateRequest {


    @NotBlank
    private String name;

    @NotBlank
    private String key;

    private String description;

}
