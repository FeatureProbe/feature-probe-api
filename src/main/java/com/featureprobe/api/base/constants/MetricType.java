package com.featureprobe.api.base.constants;

public enum MetricType {

    VALUE, NAME;
    
    
    public boolean isNameType() {
        return this == MetricType.NAME;
        
    }
}
