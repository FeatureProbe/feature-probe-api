package com.featureprobe.api.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class ToggleSearchRequest extends PaginationRequest {

    @NotBlank
    private String environmentKey;

    private Boolean isVisited;

    private Boolean disabled;

    private List<String> tags;

    private String keyword;

}
