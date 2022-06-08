package com.featureprobe.api.controller;

import com.featureprobe.api.base.doc.CreateApiResponse;
import com.featureprobe.api.base.doc.DefaultApiResponses;
import com.featureprobe.api.base.doc.GetApiResponse;
import com.featureprobe.api.base.doc.PatchApiResponse;
import com.featureprobe.api.base.enums.ResponseCodeEnum;
import com.featureprobe.api.dto.BaseResponse;
import com.featureprobe.api.dto.EnvironmentCreateRequest;
import com.featureprobe.api.dto.EnvironmentResponse;
import com.featureprobe.api.dto.EnvironmentUpdateRequest;
import com.featureprobe.api.service.EnvironmentService;
import com.featureprobe.api.validate.ResourceExistsValidate;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/projects/{projectKey}/environments")
@DefaultApiResponses
@ResourceExistsValidate
public class EnvironmentController {

    private EnvironmentService environmentService;

    @PostMapping
    @CreateApiResponse
    @Operation(summary = "Create environment", description = "Create a new environment.")
    public EnvironmentResponse create(@PathVariable("projectKey") String projectKey,
                                      @RequestBody @Validated EnvironmentCreateRequest createRequest) {
        return environmentService.create(projectKey, createRequest);
    }

    @PatchMapping("/{environmentKey}")
    @PatchApiResponse
    @Operation(summary = "Update environment", description = "Update a environment.")
    public EnvironmentResponse update(@PathVariable("projectKey") String projectKey,
                                      @PathVariable("environmentKey") String environmentKey,
                                      @RequestBody @Validated EnvironmentUpdateRequest updateRequest) {
        return environmentService.update(projectKey, environmentKey, updateRequest);
    }

    @GetMapping("/checkKey")
    @GetApiResponse
    @Operation(summary = "Check environment key", description = "Check environment exist by key")
    public BaseResponse checkKey(@PathVariable("projectKey") String projectKey,
                                 @RequestParam String key) {
        environmentService.checkKey(projectKey, key);
        return new BaseResponse(ResponseCodeEnum.SUCCESS);
    }

    @GetMapping("/checkName")
    @GetApiResponse
    @Operation(summary = "Check environment name", description = "Check environment exist by name")
    public BaseResponse checkName(@PathVariable("projectKey") String projectKey,
                                  @RequestParam String name) {
        environmentService.checkName(projectKey, name);
        return new BaseResponse(ResponseCodeEnum.SUCCESS);
    }



}
