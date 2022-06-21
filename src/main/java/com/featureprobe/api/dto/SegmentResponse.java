package com.featureprobe.api.dto;

import com.featureprobe.api.model.SegmentRuleModel;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class SegmentResponse {
    private String name;

    private String key;

    private List<SegmentRuleModel> rules;

    private String description;

    private String projectKey;

    private Date createdTime;

    private String createdBy;

    private Date modifiedTime;

    private String modifiedBy;
}
