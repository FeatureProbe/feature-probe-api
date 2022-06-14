package com.featureprobe.api.model;

import lombok.Data;

import java.util.List;

@Data
public class SegmentRule {

    private String subject;

    private String predicate;

    private List<String> objects;

}
