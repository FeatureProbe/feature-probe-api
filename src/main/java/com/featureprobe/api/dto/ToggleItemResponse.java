package com.featureprobe.api.dto;

import lombok.Data;
import java.util.Date;
import java.util.Set;

@Data
public class ToggleItemResponse {

    private String name;

    private String key;

    private String returnType;

    private String desc;

    private Set<String> tags;

    private Boolean disabled;

    private Date visitedTime;

    private Date modifiedTime;

    private String modifiedBy;
}
