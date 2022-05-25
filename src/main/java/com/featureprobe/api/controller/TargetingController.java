package com.featureprobe.api.controller;

import com.featureprobe.api.base.doc.DefaultApiResponses;
import com.featureprobe.api.base.doc.EnvironmentKeyParameter;
import com.featureprobe.api.base.doc.GetApiResponse;
import com.featureprobe.api.base.doc.PatchApiResponse;
import com.featureprobe.api.base.doc.ProjectKeyParameter;
import com.featureprobe.api.base.doc.ToggleKeyParameter;
import com.featureprobe.api.validate.ResourceExistsValidate;
import com.featureprobe.api.dto.TargetingRequest;
import com.featureprobe.api.dto.TargetingResponse;
import com.featureprobe.api.service.TargetingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public TargetingRequest update(
            @PathVariable("projectKey") String projectKey,
            @PathVariable("environmentKey") String environmentKey,
            @PathVariable("toggleKey") String toggleKey,
            @RequestBody @Validated TargetingRequest targetingRequest) {
        targetingService.update(projectKey, environmentKey, toggleKey, targetingRequest);
        return targetingRequest;
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


}
