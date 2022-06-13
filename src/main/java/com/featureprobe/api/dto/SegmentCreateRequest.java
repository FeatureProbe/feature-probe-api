package com.featureprobe.api.dto;

import com.featureprobe.api.model.SegmentRule;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class SegmentCreateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String key;

    private String description;

    private List<SegmentRule> rules;
}
