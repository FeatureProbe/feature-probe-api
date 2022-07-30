package com.featureprobe.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.featureprobe.api.base.doc.DefaultApiResponses;
import com.featureprobe.api.util.SdkVersionUtil;

import lombok.AllArgsConstructor;

@RequestMapping("/misc")
@RestController
@DefaultApiResponses
@AllArgsConstructor
public class MiscController {

    @GetMapping("/sdk/{key}")
    public String querySdkVersion(@PathVariable("key") String key) {
        return String.format("\"%s\"", SdkVersionUtil.latestVersions.get(key));
    }

}
