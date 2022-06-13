package com.featureprobe.api.dto;


import lombok.Data;

import java.util.Date;

@Data
public class SegmentResponse {
    private String name;

    private String key;

    private String description;

    private String projectKey;

    private Date createdTime;

    private String createdBy;

    private Date modifiedTime;

    private String modifiedBy;
}
