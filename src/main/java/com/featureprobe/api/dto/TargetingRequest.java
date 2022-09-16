package com.featureprobe.api.dto;

import com.featureprobe.api.model.TargetingContent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TargetingRequest {

    private TargetingContent content;

    private String comment;

    private Boolean disabled;

    private List<String> reviewers;
}