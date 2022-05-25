package com.featureprobe.api.dto;

import com.featureprobe.api.model.Variation;
import lombok.Data;

import java.util.List;


@Data
public class ToggleUpdateRequest {

    private String name;

    private String desc;

    private String[] tags;

    private Boolean clientAvailability;

    private List<Variation> variations;

    private Integer disabledServe;
}
