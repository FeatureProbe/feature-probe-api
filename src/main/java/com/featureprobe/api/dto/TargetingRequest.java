package com.featureprobe.api.dto;

import com.featureprobe.api.model.TargetingContent;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class TargetingRequest {

    private TargetingContent content;

    private String comment;

    private Boolean disabled;
}
