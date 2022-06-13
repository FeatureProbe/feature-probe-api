package com.featureprobe.api.controller;

import com.featureprobe.api.base.doc.DefaultApiResponses;
import com.featureprobe.api.base.doc.GetApiResponse;
import com.featureprobe.api.dto.SegmentResponse;
import com.featureprobe.api.dto.SegmentSearchRequest;
import com.featureprobe.api.service.SegmentService;
import com.featureprobe.api.validate.ResourceExistsValidate;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/projects/{projectKey}/segments")
@DefaultApiResponses
@ResourceExistsValidate
public class SegmentController {

    private SegmentService segmentService;

    @GetMapping
    @GetApiResponse
    @Operation(summary = "List segments", description = "Get a list of all segments")
    public Page<SegmentResponse> list(@PathVariable(name = "projectKey") String projectKey,
                                      SegmentSearchRequest searchRequest) {
        return segmentService.list(projectKey, searchRequest);
    }


}
