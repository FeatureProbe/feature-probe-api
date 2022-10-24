package com.featureprobe.api.cache.controller;

import com.featureprobe.api.cache.service.CacheService;
import com.featureprobe.api.cache.dto.SdkKeyResponse;
import com.featureprobe.api.cache.dto.ServerResponse;
import com.featureprobe.api.cache.service.ServerService;
import com.featureprobe.api.cache.dto.EventCreateRequest;
import com.featureprobe.api.cache.service.EventService;
import com.featureprobe.api.base.doc.CreateApiResponse;
import com.featureprobe.api.base.doc.GetApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping("/server/")
@Tag(name = "Server API", description = "Provided to the server api")
@AllArgsConstructor
public class ServerController {

    private ServerService serverService;

    private EventService eventService;

    private CacheService cacheService;

    @GetApiResponse
    @GetMapping("/sdk_keys")
    @Operation(summary = "List sdk keys", description = "Get all sdk keys.")
    public SdkKeyResponse queryAllSdkKeys(@RequestParam(value = "version", required = false) Long version,
                                          HttpServletResponse response)
            throws ExecutionException {
        SdkKeyResponse sdkKeyResponse = cacheService.queryAllSdkKeysFromCache();
        if (Objects.nonNull(version) && version >= sdkKeyResponse.getVersion()) {
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            return null;
        }
        return sdkKeyResponse;
    }

    @GetApiResponse
    @GetMapping("/toggles")
    @Operation(summary = "Fetch toggles", description = "Fetch toggle & segments by server sdk key.")
    public ServerResponse fetchToggles(@Parameter(description = "sdk key")
                                       @RequestHeader(value = "Authorization") String sdkKey,
                                       @RequestParam(value = "version", required = false) Long version,
                                       HttpServletResponse response)
            throws ExecutionException {
        ServerResponse serverResponse = cacheService.queryServerTogglesByServerSdkKeyFromCache(sdkKey);
        if (Objects.nonNull(version) && version >= serverResponse.getVersion()) {
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            return null;
        }
        return serverResponse;
    }

    @CreateApiResponse
    @PostMapping("/events")
    @Operation(summary = "Create event", description = "Create toggle event.")
    public void createEvent(
            @RequestBody @Validated List<EventCreateRequest> batchRequest,
            @Parameter(description = "sdk key")
            @RequestHeader(value = "Authorization") String sdkKey,
            @RequestHeader(value = "user-agent", required = false) String userAgent,
            @RequestHeader(value = "UA", required = false) String javascriptUserAgent) {
        if (StringUtils.isNotBlank(javascriptUserAgent)) {
            userAgent = javascriptUserAgent;
        }
        eventService.create(serverService.getSdkServerKey(sdkKey), userAgent, batchRequest);
    }

}
