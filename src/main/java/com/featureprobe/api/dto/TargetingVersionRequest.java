package com.featureprobe.api.dto;

import lombok.Data;

@Data
public class TargetingVersionRequest extends PaginationRequest{

    private Long version;

}
