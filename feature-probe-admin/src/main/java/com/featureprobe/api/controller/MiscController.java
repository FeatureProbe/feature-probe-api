package com.featureprobe.api.controller;

import com.featureprobe.api.component.SdkVersionScheduler;
import com.featureprobe.api.base.doc.DefaultApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/misc")
@RestController
@DefaultApiResponses
public class MiscController {

    @GetMapping("/sdk/{key}")
    public String querySdkVersion(@PathVariable("key") String key) {
        return String.format("\"%s\"", SdkVersionScheduler.latestVersions.get(key));
    }

}
