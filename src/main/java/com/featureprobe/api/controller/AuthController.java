package com.featureprobe.api.controller;

import com.featureprobe.api.base.doc.DefaultApiResponses;
import com.featureprobe.api.base.doc.GetApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@DefaultApiResponses
@Tag(name = "Authentication", description = "")
@RequestMapping("/auth")
@AllArgsConstructor
@RestController
public class AuthController {

    @GetMapping("/token")
    @GetApiResponse
    @Operation(summary = "Get authentication token", description = "Query a authentication token.")
    public String getToken() {
        return "";
    }

}
