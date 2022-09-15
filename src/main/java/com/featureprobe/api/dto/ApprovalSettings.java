package com.featureprobe.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApprovalSettings {

    private String environmentKey;

    private Boolean enable;

    private List<String> reviewers;

}
