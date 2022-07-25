package com.featureprobe.api.dto;

import com.featureprobe.api.model.TargetingContent;
import lombok.Data;

import java.util.Date;

@Data
public class TargetingVersionResponse {

    private String projectKey;

    private String environmentKey;

    private String toggleKey;

    private String comment;

    private TargetingContent content;

    private Boolean disabled;

    private Long version;

    private Date createdTime;
}
