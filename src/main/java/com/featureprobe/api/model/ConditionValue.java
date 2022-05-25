package com.featureprobe.api.model;

import com.featureprobe.sdk.server.model.Condition;
import com.featureprobe.sdk.server.model.ConditionType;
import lombok.Data;

import java.util.List;

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
        condition.setPredicate(predicate);
        condition.setObjects(objects);

        return condition;
    }
}
