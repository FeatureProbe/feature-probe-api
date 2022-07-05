package com.featureprobe.api.model;

import com.featureprobe.api.mapper.JsonMapper;
import com.featureprobe.api.util.DateTimeTranslateUtil;
import com.featureprobe.sdk.server.model.Condition;
import com.featureprobe.sdk.server.model.ConditionType;
import com.featureprobe.sdk.server.model.Segment;
import com.featureprobe.sdk.server.model.SegmentRule;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ServerSegmentBuilder {

    private static final String DATETIME_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ssXXX";

    private Segment segment;

    private List<SegmentRuleModel> segmentRuleModels;

    public ServerSegmentBuilder builder() {
        this.segment = new Segment();
        return this;
    }

    public ServerSegmentBuilder uniqueId(String uniqueId) {
        this.segment.setUniqueId(uniqueId);
        return this;
    }

    public ServerSegmentBuilder version(Long version) {
        this.segment.setVersion(version);
        return this;
    }

    public ServerSegmentBuilder rules(String rules) {
        this.segmentRuleModels = JsonMapper.toListObject(rules, SegmentRuleModel.class);
        return this;
    }

    public Segment build() {
        this.setRules();
        return this.segment;
    }

    private void setRules() {
        if (CollectionUtils.isEmpty(segmentRuleModels)) {
            segment.setRules(Collections.emptyList());
            return;
        }
        List<SegmentRule> rules = segmentRuleModels.stream().map(segmentRuleModel ->
                        segmentRuleModel.toSegmentRule()).collect(Collectors.toList());
        rules.forEach(rule -> rule.getConditions().forEach(condition -> {
            if (condition.getType() == ConditionType.DATETIME){
                convertDatetimeToUnix(condition);
            }
        }));
        segment.setRules(rules);
    }

    private void convertDatetimeToUnix(Condition condition) {
        condition.setObjects(condition.getObjects().stream().map(datetime ->
                DateTimeTranslateUtil.translateUnix(datetime, DATETIME_FORMAT_PATTERN)).collect(Collectors.toList()));
    }

}
