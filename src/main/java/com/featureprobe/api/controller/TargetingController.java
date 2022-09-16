package com.featureprobe.api.controller;

import com.featureprobe.api.base.doc.CreateApiResponse;
import com.featureprobe.api.base.doc.DefaultApiResponses;
import com.featureprobe.api.base.doc.EnvironmentKeyParameter;
import com.featureprobe.api.base.doc.GetApiResponse;
import com.featureprobe.api.base.doc.PatchApiResponse;
import com.featureprobe.api.base.doc.ProjectKeyParameter;
import com.featureprobe.api.base.doc.ToggleKeyParameter;
import com.featureprobe.api.base.enums.ResponseCodeEnum;
import com.featureprobe.api.dto.AfterTargetingVersionResponse;
import com.featureprobe.api.dto.BaseResponse;
import com.featureprobe.api.dto.TargetingDiffResponse;
import com.featureprobe.api.dto.TargetingRequest;
import com.featureprobe.api.dto.TargetingResponse;
import com.featureprobe.api.dto.TargetingVersionRequest;
import com.featureprobe.api.dto.TargetingVersionResponse;
import com.featureprobe.api.dto.UpdateApprovalStatusRequest;
import com.featureprobe.api.service.TargetingService;
import com.featureprobe.api.validate.ResourceExistsValidate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Targeting", description = "The user targeting rules")
@RequestMapping("/projects/{projectKey}/environments/{environmentKey}/toggles/{toggleKey}/targeting")
@ProjectKeyParameter
@EnvironmentKeyParameter
@ToggleKeyParameter
@DefaultApiResponses
@AllArgsConstructor
@RestController
@ResourceExistsValidate
public class TargetingController {

    private TargetingService targetingService;

    @PatchApiResponse
    @PatchMapping
    @Operation(summary = "Update targeting", description = "Update targeting.")
    public TargetingResponse update(
            @PathVariable("projectKey") String projectKey,
            @PathVariable("environmentKey") String environmentKey,
            @PathVariable("toggleKey") String toggleKey,
            @RequestBody @Validated TargetingRequest targetingRequest) {
        return targetingService.update(projectKey, environmentKey, toggleKey, targetingRequest);
    }

    @PatchMapping("/sketch/publish")
    @CreateApiResponse
    @Operation(summary = "Publish targeting sketch", description = "Publish targeting sketch.")
    public TargetingResponse publishSketch(@PathVariable("projectKey") String projectKey,
                                           @PathVariable("environmentKey") String environmentKey,
                                           @PathVariable("toggleKey") String toggleKey) {
        return targetingService.publishSketch(projectKey, environmentKey, toggleKey);
    }

    @PatchMapping("/sketch/cancel")
    @CreateApiResponse
    @Operation(summary = "Cancel targeting sketch", description = "Cancel targeting sketch.")
    public BaseResponse cancelSketch(@PathVariable("projectKey") String projectKey,
                                           @PathVariable("environmentKey") String environmentKey,
                                           @PathVariable("toggleKey") String toggleKey) {
        targetingService.cancelSketch(projectKey, environmentKey, toggleKey);
        return new BaseResponse(ResponseCodeEnum.SUCCESS);
    }


    @PatchApiResponse
    @PatchMapping("/approvalStatus")
    @Operation(summary = "Update targeting approval status", description = "Update targeting approval status.")
    public BaseResponse updateApprovalStatus(@PathVariable("projectKey") String projectKey,
                                               @PathVariable("environmentKey") String environmentKey,
                                               @PathVariable("toggleKey") String toggleKey,
                                               @RequestBody @Validated UpdateApprovalStatusRequest updateRequest) {
        targetingService.updateApprovalStatus(projectKey, environmentKey, toggleKey, updateRequest);
        return new BaseResponse(ResponseCodeEnum.SUCCESS);
    }

    @GetApiResponse
    @GetMapping
    @Operation(summary = "Get targeting", description = "Get a single targeting by toggle key in the environment.")
    public TargetingResponse query(
            @PathVariable("projectKey") String projectKey,
            @PathVariable("environmentKey") String environmentKey,
            @PathVariable("toggleKey") String toggleKey) {
        return targetingService.queryByKey(projectKey, environmentKey, toggleKey);
    }

    @GetApiResponse
    @GetMapping("/versions")
    @Operation(summary = "Get targeting versions", description = "Get targeting version history.")
    public Page<TargetingVersionResponse> versions(
            @PathVariable("projectKey") String projectKey,
            @PathVariable("environmentKey") String environmentKey,
            @PathVariable("toggleKey") String toggleKey,
            TargetingVersionRequest targetingVersionRequest) {
        return targetingService.queryVersions(projectKey, environmentKey, toggleKey, targetingVersionRequest);
    }

    @GetApiResponse
    @GetMapping("/versions/{version}")
    @Operation(summary = "Get all targeting versions larger than the version。",
            description = "Get all targeting versions larger than the version.")
    public AfterTargetingVersionResponse allAfterVersions(@PathVariable("projectKey") String projectKey,
                                                          @PathVariable("environmentKey") String environmentKey,
                                                          @PathVariable("toggleKey") String toggleKey,
                                                          @PathVariable("version") Long version) {
        return targetingService.queryAfterVersion(projectKey, environmentKey, toggleKey, version);
    }

    @GetApiResponse
    @GetMapping("/diff")
    @Operation(summary = "Get targeting diff.", description = "Get targeting diff.")
    public TargetingDiffResponse diff(@PathVariable("projectKey") String projectKey,
                                      @PathVariable("environmentKey") String environmentKey,
                                      @PathVariable("toggleKey") String toggleKey) {
        return targetingService.diff(projectKey, environmentKey, toggleKey);
    }

}
