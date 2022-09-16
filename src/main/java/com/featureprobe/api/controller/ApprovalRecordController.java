package com.featureprobe.api.controller;

import com.featureprobe.api.base.doc.DefaultApiResponses;
import com.featureprobe.api.dto.ApprovalRecordQueryRequest;
import com.featureprobe.api.dto.ApprovalRecordResponse;
import com.featureprobe.api.service.ApprovalRecordService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@DefaultApiResponses
@Tag(name = "Approval record", description = "Using the approval API, you can query approval record")
@RequestMapping("/approvalRecords")
@AllArgsConstructor
@RestController
public class ApprovalRecordController {

    private ApprovalRecordService approvalRecordService;

    @GetMapping
    public Page<ApprovalRecordResponse> list(@Validated ApprovalRecordQueryRequest queryRequest) {
        return approvalRecordService.list(queryRequest);
    }

}
