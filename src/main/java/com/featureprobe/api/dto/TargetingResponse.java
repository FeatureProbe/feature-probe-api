package com.featureprobe.api.dto;

import com.featureprobe.api.model.TargetingContent;
import lombok.Data;

import java.util.Date;

@Data
public class TargetingResponse {

    private Boolean disabled;

    private TargetingContent content;

    private Long version;

    private Date modifiedTime;

    private String modifiedBy;

}
