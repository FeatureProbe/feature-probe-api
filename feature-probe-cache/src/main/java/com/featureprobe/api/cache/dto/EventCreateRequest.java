package com.featureprobe.api.cache.dto;

import com.featureprobe.api.cache.model.AccessSummary;
import lombok.Data;

@Data
public class EventCreateRequest {

    AccessSummary access;
}
