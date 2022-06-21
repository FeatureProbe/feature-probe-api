package com.featureprobe.api.model;

import com.featureprobe.sdk.server.model.Condition;
import com.featureprobe.sdk.server.model.ConditionType;
import com.featureprobe.sdk.server.model.PredicateType;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class ConditionValue {

    private String type;
    private String subject;

    private String predicate;
    private List<String> objects;

    public Condition toCondition() {
        Condition condition = new Condition();
        condition.setType(ConditionType.forValue(type));
        condition.setSubject(subject);
        condition.setPredicate(PredicateType.forValue(predicate));
        condition.setObjects(objects);
        return condition;
    }
}
