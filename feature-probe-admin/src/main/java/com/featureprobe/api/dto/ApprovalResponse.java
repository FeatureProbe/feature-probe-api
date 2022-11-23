package com.featureprobe.api.dto;

import com.featureprobe.api.base.enums.ApprovalStatusEnum;
import lombok.Data;

import java.util.Date;

@Data
public class ApprovalResponse {

    private ApprovalStatusEnum status;

    private String viewers;

    private String approvalBy;

    private Date approvalDate;

}
