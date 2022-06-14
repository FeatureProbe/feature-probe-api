package com.featureprobe.api.dto;

import lombok.Data;

@Data
public class ToggleSegmentResponse {

    private String name;

    private String key;

    private String environment;

    private Boolean disabled;

    private String description;

}
