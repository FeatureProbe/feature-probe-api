package com.featureprobe.api.dto;

import lombok.Data;

@Data
public class PaginationRequest {

    private Integer pageIndex = 0;

    private Integer pageSize = 10;

    private String sortBy;
}
