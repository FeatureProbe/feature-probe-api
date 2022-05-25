package com.featureprobe.api.model;

import lombok.Data;

import java.util.List;

@Data
public class BaseRule {

    private List<ConditionValue> conditions;
}
