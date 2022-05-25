package com.featureprobe.api.dto;

import com.featureprobe.api.model.AccessSummary;
import lombok.Data;

@Data
public class EventCreateRequest {

    AccessSummary access;
}
