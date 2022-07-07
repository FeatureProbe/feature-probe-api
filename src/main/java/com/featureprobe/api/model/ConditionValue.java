package com.featureprobe.api.model;

import com.featureprobe.sdk.server.model.Condition;
import com.featureprobe.sdk.server.model.ConditionType;
import com.featureprobe.sdk.server.model.PredicateType;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

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
        condition.setPredicate(PredicateType.forValue(predicate));
        condition.setObjects(objects);
        return condition;
    }

    public boolean isSegmentType() {
        return StringUtils.equals(ConditionType.SEGMENT.toValue(), type);
    }

    public boolean isNumberType() {
        return StringUtils.equals(ConditionType.NUMBER.toValue(), type);
    }

    public boolean isDatetimeType() {
        return StringUtils.equals(ConditionType.DATETIME.toValue(), type);
    }

    public boolean isSemVerType() {
        return StringUtils.equals(ConditionType.SEMVER.toValue(), type);
    }

}
