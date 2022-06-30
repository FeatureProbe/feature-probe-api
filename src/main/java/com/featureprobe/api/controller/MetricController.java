package com.featureprobe.api.controller;

import com.featureprobe.api.base.constants.MetricType;
import com.featureprobe.api.base.doc.DefaultApiResponses;
import com.featureprobe.api.base.doc.EnvironmentKeyParameter;
import com.featureprobe.api.base.doc.GetApiResponse;
import com.featureprobe.api.base.doc.ProjectKeyParameter;
import com.featureprobe.api.base.doc.ToggleKeyParameter;
import com.featureprobe.api.dto.MetricResponse;
import com.featureprobe.api.service.MetricService;
import com.featureprobe.api.validate.ResourceExistsValidate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/projects/{projectKey}/environments/{environmentKey}/toggles/{toggleKey}/metrics")
@DefaultApiResponses
@ProjectKeyParameter
@ToggleKeyParameter
@EnvironmentKeyParameter
@ResourceExistsValidate
@Tag(name = "Metric", description = "")
public class MetricController {

    private MetricService metricService;

    @GetApiResponse
    @GetMapping
    @Operation(summary = "Query Metrics", description = "")
    public MetricResponse query(@PathVariable("projectKey") String projectKey,
                                @PathVariable("environmentKey") String environmentKey,
                                @PathVariable("toggleKey") String toggleKey,
                                @RequestParam(value = "metricType", defaultValue = "VALUE") MetricType metricType,
                                @RequestParam(value = "lastHours", defaultValue = "24") int lastHours) {

        return metricService.query(projectKey, environmentKey, toggleKey, metricType, lastHours);
    }
}
