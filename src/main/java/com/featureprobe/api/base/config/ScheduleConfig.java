package com.featureprobe.api.base.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ComponentScan(basePackageClasses = {
    com.featureprobe.api.util.SdkVersionUtil.class,
})
public class ScheduleConfig {
}
