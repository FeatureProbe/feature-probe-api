package com.featureprobe.api.model;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

@Data
public class BaseRule {

    private List<ConditionValue> conditions;

    public boolean isNotEmptyConditions() {
        return CollectionUtils.isNotEmpty(conditions);
    }
}
