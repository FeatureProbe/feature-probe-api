package com.featureprobe.api.dto;

import com.featureprobe.api.model.AccessEventPoint;
import com.featureprobe.api.model.VariationAccessCounter;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MetricResponse {

    private List<AccessEventPoint> metrics;
    private List<VariationAccessCounter> summary;

}
