package com.featureprobe.api.controller;

import com.featureprobe.api.base.doc.CreateApiResponse;
import com.featureprobe.api.base.doc.DefaultApiResponses;
import com.featureprobe.api.base.doc.GetApiResponse;
import com.featureprobe.api.base.doc.PatchApiResponse;
import com.featureprobe.api.base.enums.ResponseCodeEnum;
import com.featureprobe.api.base.enums.ValidateTypeEnum;
import com.featureprobe.api.dto.BaseResponse;
import com.featureprobe.api.dto.PaginationRequest;
import com.featureprobe.api.dto.SegmentCreateRequest;
import com.featureprobe.api.dto.SegmentResponse;
import com.featureprobe.api.dto.SegmentUpdateRequest;
import com.featureprobe.api.dto.ToggleSegmentResponse;
import com.featureprobe.api.service.SegmentService;
import com.featureprobe.api.base.doc.GetApiResponse;
import com.featureprobe.api.dto.SegmentResponse;
import com.featureprobe.api.dto.SegmentSearchRequest;
import com.featureprobe.api.service.SegmentService;
import com.featureprobe.api.validate.ResourceExistsValidate;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @CreateApiResponse
    @PostMapping
    @Operation(summary = "Create segment", description = "Create a new segment.")
    public SegmentResponse create(@PathVariable(name = "projectKey") String projectKey,
                                  @RequestBody @Validated SegmentCreateRequest createRequest) {
        return segmentService.create(projectKey, createRequest);
    }

    @PatchApiResponse
    @PatchMapping("/{segmentKey}")
    @Operation(summary = "Update segment", description = "Update a segment.")
    public SegmentResponse update(@PathVariable(name = "projectKey") String projectKey,
                                  @PathVariable(name = "segmentKey") String segmentKey,
                                  @RequestBody @Validated SegmentUpdateRequest segmentUpdateRequest) {
        return segmentService.update(projectKey, segmentKey, segmentUpdateRequest);
    }

    @DefaultApiResponses
    @DeleteMapping("/{segmentKey}")
    @Operation(summary = "Delete segment", description = "Delete a segment.")
    public SegmentResponse delete(@PathVariable(name = "projectKey") String projectKey,
                                  @PathVariable(name = "segmentKey") String segmentKey) {
        return segmentService.delete(projectKey, segmentKey);
    }


    @GetMapping("/{segmentKey}/toggles")
    @GetApiResponse
    @Operation(summary = "List of toggles using segment", description = "List of toggles using segment")
    public Page<ToggleSegmentResponse> usingToggles(@PathVariable(name = "projectKey") String projectKey,
                                                    @PathVariable(name = "segmentKey") String segmentKey,
                                                    PaginationRequest paginationRequest) {
        return segmentService.usingToggles(projectKey, segmentKey, paginationRequest);
    }

    @GetApiResponse
    @GetMapping("/{segmentKey}")
    @Operation(summary = "Get segment", description = "Get a single segment by key.")
    public SegmentResponse query(@PathVariable(name = "projectKey") String projectKey,
                                 @PathVariable(name = "segmentKey") String segmentKey) {
        return segmentService.queryByKey(projectKey, segmentKey);
    }

    @GetMapping("/exists")
    @GetApiResponse
    @Operation(summary = "Check segment exist", description = "Check segment exist")
    public BaseResponse exists(@PathVariable("projectKey") String projectKey,
                               @RequestParam ValidateTypeEnum type,
                               @RequestParam String value) {
        segmentService.validateExists(projectKey, type, value);
        return new BaseResponse(ResponseCodeEnum.SUCCESS);
    }
}
