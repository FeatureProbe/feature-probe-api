package com.featureprobe.api.base.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("config")
@Component
@Data
public class ConfigProperties {

    private String secret;

}
