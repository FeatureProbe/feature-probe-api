package com.featureprobe.api.dto;

import com.featureprobe.api.base.enums.ApprovalTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ApprovalRecordQueryRequest extends PaginationRequest{

    private String keyword;

    @NotNull
    private String status;

    @NotNull
    private ApprovalTypeEnum type;

}
