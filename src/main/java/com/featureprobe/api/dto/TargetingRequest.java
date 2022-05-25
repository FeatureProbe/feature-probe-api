package com.featureprobe.api.dto;

import com.featureprobe.api.model.TargetingContent;
import lombok.Data;

@Data
public class TargetingRequest {

    private TargetingContent content;

    private Boolean disabled;
}
