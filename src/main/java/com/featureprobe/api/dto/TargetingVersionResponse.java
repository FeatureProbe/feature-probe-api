package com.featureprobe.api.dto;

import com.featureprobe.api.model.TargetingContent;
import lombok.Data;

import java.util.Date;

@Data
public class TargetingVersionResponse {

    private String projectKey;

    private String environmentKey;

    private String comment;

    private TargetingContent content;

    private Long version;

    private Date createdTime;

    private String createdBy;
}
