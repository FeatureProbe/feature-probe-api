package com.featureprobe.api.controller;

import com.featureprobe.api.base.doc.CreateApiResponse;
import com.featureprobe.api.base.doc.DefaultApiResponses;
import com.featureprobe.api.base.doc.GetApiResponse;
import com.featureprobe.api.base.doc.PatchApiResponse;
import com.featureprobe.api.base.doc.ProjectKeyParameter;
import com.featureprobe.api.base.doc.ToggleKeyParameter;
import com.featureprobe.api.base.enums.ResponseCodeEnum;
import com.featureprobe.api.dto.BaseResponse;
import com.featureprobe.api.dto.ToggleCreateRequest;
import com.featureprobe.api.dto.ToggleItemResponse;
import com.featureprobe.api.dto.ToggleResponse;
import com.featureprobe.api.dto.ToggleSearchRequest;
import com.featureprobe.api.dto.ToggleUpdateRequest;
import com.featureprobe.api.validate.ResourceExistsValidate;
import com.featureprobe.api.service.ToggleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
@Tag(name = "Toggle", description = "The toggle is a collection of configurations")
@RequestMapping("/projects/{projectKey}/toggles")
@ProjectKeyParameter
@ToggleKeyParameter
@DefaultApiResponses
@AllArgsConstructor
@ResourceExistsValidate
@RestController
public class ToggleController {

    private ToggleService toggleService;

    @GetMapping
    @GetApiResponse
    @Operation(summary = "List toggles", description = "Get a list of all toggles in the project.")
    public Page<ToggleItemResponse> list(
            @PathVariable(name = "projectKey") String projectKey,
            @Validated ToggleSearchRequest filter) {
        return toggleService.query(projectKey, filter);
    }

    @CreateApiResponse
    @PostMapping
    @Operation(summary = "Create toggle", description = "Create a new toggle.")
    public ToggleResponse create(
            @PathVariable(name = "projectKey") String projectKey,
            @RequestBody @Validated ToggleCreateRequest toggleCreateRequest) {
        return toggleService.create(projectKey, toggleCreateRequest);
    }

    @PatchApiResponse
    @PatchMapping("/{toggleKey}")
    @Operation(summary = "Update toggle", description = "Update a toggle.")
    public ToggleResponse update(@PathVariable(name = "projectKey") String projectKey,
                                 @PathVariable(name = "toggleKey") String toggleKey,
                                 @RequestBody @Validated ToggleUpdateRequest toggleUpdateRequest) {
        return toggleService.update(projectKey, toggleKey, toggleUpdateRequest);
    }

    @GetApiResponse
    @GetMapping("/{toggleKey}")
    @Operation(summary = "Get toggle", description = "Get a single toggle by key.")
    public ToggleResponse query(@PathVariable(name = "projectKey") String projectKey,
                                @PathVariable(name = "toggleKey") String toggleKey) {
        return toggleService.queryByKey(projectKey, toggleKey);
    }

    @GetMapping("/checkKey")
    @GetApiResponse
    @Operation(summary = "Check toggle key", description = "Check toggle exist by key")
    public BaseResponse checkKey(@PathVariable("projectKey") String projectKey,
                                 @RequestParam String key){
        toggleService.checkKey(projectKey, key);
        return new BaseResponse(ResponseCodeEnum.SUCCESS);
    }

    @GetMapping("/checkName")
    @GetApiResponse
    @Operation(summary = "Check toggle name", description = "Check toggle exist by name")
    public BaseResponse checkName(@PathVariable("projectKey") String projectKey,
                                 @RequestParam String name){
        toggleService.checkName(projectKey, name);
        return new BaseResponse(ResponseCodeEnum.SUCCESS);
    }

}
