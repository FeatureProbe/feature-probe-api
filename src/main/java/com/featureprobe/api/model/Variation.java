package com.featureprobe.api.model;

import lombok.Data;

@Data
public class Variation {

    private String value;

    private String name;

    private String description;

    public interface ValueConverter<T> {
        T convert(String value);
    }
}
