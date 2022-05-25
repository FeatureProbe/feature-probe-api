package com.featureprobe.api.controller;


import com.featureprobe.api.base.doc.CreateApiResponse;
import com.featureprobe.api.base.doc.GetApiResponse;
import com.featureprobe.api.base.enums.ResourceType;
import com.featureprobe.api.base.exception.ResourceNotFoundException;
import com.featureprobe.api.dto.EventCreateRequest;
import com.featureprobe.api.dto.SdkKeyResponse;
import com.featureprobe.api.dto.ServerResponse;
import com.featureprobe.api.service.EnvironmentService;
import com.featureprobe.api.service.EventService;
import com.featureprobe.api.service.ToggleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/server/")
@Tag(name = "Server API", description = "Provided to the server api")
@AllArgsConstructor
public class ServerController {

    private ToggleService toggleService;
    private EnvironmentService environmentService;
    private EventService eventService;

    @GetApiResponse
    @GetMapping("/sdk_keys")
    @Operation(summary = "List sdk keys", description = "Get all sdk keys.")
    public SdkKeyResponse queryAllSdkKeys() {
        return environmentService.queryAllSdkKeys();
    }

    @GetApiResponse
    @GetMapping("/toggles")
    @Operation(summary = "Fetch toggles", description = "Fetch toggle & segments by server sdk key.")
    public ServerResponse fetchToggles(@Parameter(description = "sdk key")
                                       @RequestHeader(value = "Authorization") String sdkKey) {
        return toggleService.queryServerTogglesByServerSdkKey(environmentService.getSdkServerKey(sdkKey));
    }

    @CreateApiResponse
    @PostMapping("/events")
    @Operation(summary = "Create event", description = "Create toggle event.")
    public void createEvent(
            @RequestBody @Validated List<EventCreateRequest> batchRequest,
            @Parameter(description = "sdk key")
            @RequestHeader(value = "Authorization") String sdkKey) {
        eventService.create(environmentService.getSdkServerKey(sdkKey), batchRequest);
    }

}
