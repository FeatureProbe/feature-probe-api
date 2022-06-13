package com.featureprobe.api.dto;

import com.featureprobe.api.model.SegmentRule;
import com.featureprobe.sdk.server.model.Rule;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class SegmentUpdateRequest {

    @NotBlank
    private String name;

    private String description;

    private List<SegmentRule> rules;

}
