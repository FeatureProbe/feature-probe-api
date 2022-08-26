package com.featureprobe.api.dto;

import com.featureprobe.api.base.enums.VisitFilter;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class ToggleSearchRequest extends PaginationRequest {

    private String environmentKey;

    private VisitFilter visitFilter;

    private Boolean disabled;

    private List<String> tags;

    private String keyword;

    private boolean archived;

}
